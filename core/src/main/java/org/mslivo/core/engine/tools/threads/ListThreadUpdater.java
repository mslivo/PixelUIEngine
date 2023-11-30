package org.mslivo.core.engine.tools.threads;

import org.mslivo.core.engine.tools.Tools;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threadpool that can be used to iterate over subsets of lists
 */
public class ListThreadUpdater {
    private ExecutorService threadPool;
    private final ListUpdater lThreadPoolUpdater;
    private final int objectsPerWorker;
    private final ArrayList<Worker> tasks;
    private int taskSizeLast;
    private final List updateObjects;
    private ArrayDeque<Worker> freeWorkerPool;

    public ListThreadUpdater(List updateObjects, ListUpdater listUpdater, int objectsPerWorker, ThreadPoolAlgorithm threadPoolAlgorithm) {
        this(updateObjects, listUpdater, objectsPerWorker, threadPoolAlgorithm, 5);
    }

    public ListThreadUpdater(List updateObjects, ListUpdater listUpdater, int objectsPerWorker, ThreadPoolAlgorithm threadPoolAlgorithm, int fixedThreadCount) {
        this.lThreadPoolUpdater = listUpdater;
        this.updateObjects = updateObjects;
        this.objectsPerWorker = Tools.Calc.lowerBounds(objectsPerWorker, 1);
        switch (threadPoolAlgorithm) {
            case FIXED -> this.threadPool = Executors.newFixedThreadPool(Tools.Calc.lowerBounds(fixedThreadCount, 1));
            case CACHED -> this.threadPool = Executors.newCachedThreadPool();
            case WORKSTEALING -> this.threadPool = Executors.newWorkStealingPool();
        }
        this.tasks = new ArrayList<>();
        this.freeWorkerPool = new ArrayDeque<>();
        this.taskSizeLast = -1;
    }

    private Worker getNextWorker(List objects, int fromIndex, int toIndex) {
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
            if (tasks.size() > 0) {
                freeWorkerPool.addAll(tasks);
                tasks.clear();
            }
            if (updateObjects.size() > objectsPerWorker) {
                for (int i = 0; i < updateObjects.size(); i = i + objectsPerWorker) {
                    tasks.add(getNextWorker(updateObjects, i, i + (objectsPerWorker - 1)));
                }
            }else{
                tasks.add(getNextWorker(this.updateObjects, 0, (objectsPerWorker - 1)));
            }

            taskSizeLast = updateObjects.size();
        }

        if(tasks.size() > 1){
            try {
                threadPool.invokeAll(tasks);
            } catch (InterruptedException e) {
            }
        }else{
            tasks.get(0).call();
        }

    }


    class Worker implements Callable<Object> {

        private int fromIndex, toIndex;

        private List objects;

        public Worker(List objects, int fromIndex, int toIndex) {
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

}
