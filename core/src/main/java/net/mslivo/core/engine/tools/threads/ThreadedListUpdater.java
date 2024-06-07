package net.mslivo.core.engine.tools.threads;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threadpool that can be used to iterate over multiple parts of a list at once
 */
public class ThreadedListUpdater<T> {
    private final ExecutorService threadPool;
    private final ItemUpdater<T>[] lThreadPoolUpdaters;
    private final int cpuCount;
    private final ArrayDeque<Worker> tasks;
    private final List<T> updateObjects;
    private final ArrayDeque<Worker> freeWorkerPool;

    private int objectsSizeLast;
    private ItemUpdater<T> currentUpdater;

    public ThreadedListUpdater(List<T> updateObjects, ItemUpdater itemUpdater) {
        this(updateObjects, new ItemUpdater[]{itemUpdater}, Executors.newVirtualThreadPerTaskExecutor());
    }

    public ThreadedListUpdater(List<T> updateObjects, ItemUpdater itemUpdater, ExecutorService executorService) {
        this(updateObjects, new ItemUpdater[]{itemUpdater}, executorService);
    }

    public ThreadedListUpdater(List<T> updateObjects, ItemUpdater[] itemUpdaters, ExecutorService executorService) {
        this.lThreadPoolUpdaters = itemUpdaters != null ? itemUpdaters : new ItemUpdater[]{};
        this.currentUpdater = null;
        this.updateObjects = updateObjects;
        this.cpuCount = Runtime.getRuntime().availableProcessors();
        this.threadPool = executorService;
        this.tasks = new ArrayDeque<>();
        this.freeWorkerPool = new ArrayDeque<>();
        this.objectsSizeLast = -1;
    }

    private Worker getNextWorker(int fromIndex, int toIndex) {
        if (freeWorkerPool.size() > 0) {
            Worker worker = freeWorkerPool.pop();
            worker.fromIndex = fromIndex;
            worker.toIndex = toIndex;
            return worker;
        } else {
            return new Worker(fromIndex, toIndex);
        }
    }

    public void update() {
        if (updateObjects.size() == 0) return;

        if (updateObjects.size() != objectsSizeLast) {
            // Resize Tasks
            int objectsPerWorker = updateObjects.size()/cpuCount;
            int rest = updateObjects.size()%cpuCount;
            int sizeMinus1 = updateObjects.size()-1;
            if (tasks.size() > 0) {
                freeWorkerPool.addAll(tasks);
                tasks.clear();
            }
            if (updateObjects.size() > (objectsPerWorker+rest)) {
                for (int i = 0; i < updateObjects.size(); i += objectsPerWorker) {
                    int fromIndex = i;
                    int toIndex = i+(objectsPerWorker - 1) + (i == sizeMinus1 ? rest : 0);
                    tasks.add(getNextWorker(fromIndex, toIndex));
                }
            }else{
                tasks.add(getNextWorker(0, (sizeMinus1)));
            }
            objectsSizeLast = updateObjects.size();
        }

        if (tasks.size() > 1) {
            try {
                for(int i=0;i<lThreadPoolUpdaters.length;i++) {
                    this.currentUpdater = lThreadPoolUpdaters[i];
                    this.currentUpdater.beforeUpdate();
                    threadPool.invokeAll(tasks);
                    this.currentUpdater.afterUpdate();
                }
            } catch (InterruptedException e) {
            }
        } else {
            for(int i=0;i<lThreadPoolUpdaters.length;i++) {
                this.currentUpdater = lThreadPoolUpdaters[i];
                this.currentUpdater.beforeUpdate();
                tasks.getFirst().call();
                this.currentUpdater.afterUpdate();
            }
        }

    }


    class Worker implements Callable<Object> {

        private int fromIndex, toIndex;

        public Worker(int fromIndex, int toIndex) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        @Override
        public Object call() {
            for (int i = fromIndex; i <= toIndex; i++) {
                ThreadedListUpdater.this.currentUpdater.updateFromThread(ThreadedListUpdater.this.updateObjects.get(i), i);
            }
            return null;
        }
    }

    public void shutdown() {
        this.threadPool.shutdown();
    }

    public List<T> getUpdateObjects() {
        return updateObjects;
    }


    public interface ItemUpdater<T> {

        default void beforeUpdate(){}
        default void afterUpdate(){}

        void updateFromThread(T object, int index);
    }
}
