package net.mslivo.core.engine.tools.concurrency.lists;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threadpool that can be used to iterate over subsets of lists
 */
public class LThreadUpdater<T> {
    private final ExecutorService threadPool;
    private final LItemUpdater<T> lThreadPoolUpdater;
    private final int cpuCount;
    private final ArrayDeque<Worker> tasks;
    private int taskSizeLast;
    private final List<T> updateObjects;
    private final ArrayDeque<Worker> freeWorkerPool;

    public LThreadUpdater(List<T> updateObjects, LItemUpdater LItemUpdater) {
        this(updateObjects, LItemUpdater, Executors.newVirtualThreadPerTaskExecutor());
    }

    public LThreadUpdater(List<T> updateObjects, LItemUpdater LItemUpdater, ExecutorService executorService) {
        this.lThreadPoolUpdater = LItemUpdater;
        this.updateObjects = updateObjects;
        this.cpuCount = Runtime.getRuntime().availableProcessors();
        this.threadPool = executorService;
        this.tasks = new ArrayDeque<>();
        this.freeWorkerPool = new ArrayDeque<>();
        this.taskSizeLast = -1;
    }

    private Worker getNextWorker(List<T> objects, int fromIndex, int toIndex) {
        if (freeWorkerPool.size() > 0) {
            Worker worker = freeWorkerPool.pop();
            worker.objects = objects;
            worker.fromIndex = fromIndex;
            worker.toIndex = toIndex;
            return worker;
        } else {
            return new Worker(objects, fromIndex, toIndex);
        }
    }

    public void update() {
        if (updateObjects.size() == 0) return;

        if (updateObjects.size() != taskSizeLast) {
            int objectsPerWorker = updateObjects.size()/cpuCount;
            int rest = updateObjects.size()%cpuCount;
            if (tasks.size() > 0) {
                freeWorkerPool.addAll(tasks);
                tasks.clear();
            }
            if (updateObjects.size() > objectsPerWorker) {
                for (int i = 0; i < updateObjects.size(); i = i + objectsPerWorker) {
                    int fromIndex = i;
                    int toIndex = i+(objectsPerWorker - 1) + (i == (updateObjects.size()-1) ? rest : 0);
                    tasks.add(getNextWorker(updateObjects, fromIndex, toIndex));
                }
            }else{
                tasks.add(getNextWorker(this.updateObjects, 0, (this.updateObjects.size() - 1)));
            }

        }

        if (tasks.size() > 1) {
            try {
                threadPool.invokeAll(tasks);
            } catch (InterruptedException e) {
            }
        } else {
            tasks.getFirst().call();
        }

    }


    class Worker implements Callable<Object> {

        private int fromIndex, toIndex;

        private List<T> objects;

        public Worker(List<T> objects, int fromIndex, int toIndex) {
            this.objects = objects;
            this.fromIndex = fromIndex;
            this.toIndex = Math.min(toIndex, (objects.size() - 1));
        }

        @Override
        public Object call() {
            for (int i = fromIndex; i <= toIndex; i++) {
                lThreadPoolUpdater.updateFromThread(objects.get(i), i);
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

}
