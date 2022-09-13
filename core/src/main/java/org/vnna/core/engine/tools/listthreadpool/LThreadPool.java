package org.vnna.core.engine.tools.listthreadpool;

import org.vnna.core.engine.tools.Tools;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threadpool that can be used to iterate over subsets of lists
 */
public class LThreadPool {

    private ExecutorService threadPool;

    private LThreadPoolUpdater lThreadPoolUpdater;

    private int objectsPerThread;

    private ArrayDeque<Worker> tasks;

    private List updateObjects;

    public LThreadPool(List updateObjects, LThreadPoolUpdater lThreadPoolUpdater, int objectsPerThread, ThreadPoolAlgorithm threadPoolAlgorithm) {
        this(updateObjects, lThreadPoolUpdater, objectsPerThread, threadPoolAlgorithm, 5);

    }

    public LThreadPool(List updateObjects, LThreadPoolUpdater lThreadPoolUpdater, int objectsPerThread, ThreadPoolAlgorithm threadPoolAlgorithm, int fixedThreadCount) {
        this.lThreadPoolUpdater = lThreadPoolUpdater;
        this.updateObjects = updateObjects;
        this.objectsPerThread = Tools.Calc.lowerBounds(objectsPerThread, 1);
        switch (threadPoolAlgorithm) {
            case FIXED -> {
                this.threadPool = Executors.newFixedThreadPool(Tools.Calc.lowerBounds(fixedThreadCount, 1));
            }
            case CACHED -> this.threadPool = Executors.newCachedThreadPool();
            case WORKSTEALING -> this.threadPool = Executors.newWorkStealingPool();
        }
        this.tasks = new ArrayDeque<>();
    }



    public void update() {
        if(updateObjects.size() == 0) return;

        if (updateObjects.size() <= objectsPerThread) { // dont use threadpool if objects would fit into one thread
            try {
                new Worker(this.updateObjects, 0, (objectsPerThread - 1)).call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tasks.clear();
            for (int i = 0; i < updateObjects.size(); i = i + objectsPerThread) {
                tasks.add(new Worker(updateObjects, i, i + (objectsPerThread - 1)));
            }
            try {
                threadPool.invokeAll(tasks);
            } catch (InterruptedException e) {
            }
        }

        return;
    }


    class Worker implements Callable<Object> {

        private int fromIndex, toIndex;
        private List objects;

        public Worker(List objects, int fromIndex, int toIndex) {
            this.objects = objects;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            if (this.toIndex > (objects.size() - 1)) this.toIndex = objects.size() - 1;
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
