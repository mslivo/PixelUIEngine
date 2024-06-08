package net.mslivo.core.engine.tools.threads;

import java.util.List;

public class ThreadedListUpdater<T> {

    private final List<T> updateObjects;
    private final ThreadedListUpdater.ItemUpdater<T>[] itemUpdaters;
    private final Thread[] threads;
    private final Worker[] workers;
    private final int threadCount;
    private int updateObjectsSizeLast;

    public ThreadedListUpdater(List<T> updateObjects, ThreadedListUpdater.ItemUpdater<T> itemUpdater) {
        this(updateObjects, new ItemUpdater[]{itemUpdater});
    }

    public ThreadedListUpdater(List<T> updateObjects, ThreadedListUpdater.ItemUpdater<T>[] itemUpdaters) {
        this.updateObjects = updateObjects;
        this.itemUpdaters = itemUpdaters;
        this.threadCount = Runtime.getRuntime().availableProcessors();
        this.threads = new Thread[this.threadCount];
        this.workers = new Worker[this.threadCount];
        for(int i=0;i<this.threadCount;i++) this.workers[i] = new Worker(updateObjects);
        this.updateObjectsSizeLast = -1;
    }

    public void update() {
        if (updateObjects.size() == 0) return;
        if (updateObjects.size() < this.threadCount){
            // run single threaded
            for (int iu=0;iu<itemUpdaters.length;iu++) {
                itemUpdaters[iu].beforeUpdate();
                for(int il=0;il<updateObjects.size();il++) {
                    itemUpdaters[iu].updateFromThread(updateObjects.get(il),il);
                }
                itemUpdaters[iu].afterUpdate();
            }
            return;
        }
        // Run threaded
        if(updateObjects.size() != updateObjectsSizeLast){
            // Resize
            int itemsPerWorker = updateObjects.size() / threadCount;
            int itemsMod = updateObjects.size() % threadCount;
            for (int it = 0; it < threadCount; it++) {
                int fromIndex = it * itemsPerWorker;
                int toIndex = (it == threadCount - 1) ? updateObjects.size() - 1 : fromIndex + itemsPerWorker - 1;
                workers[it].from = fromIndex;
                workers[it].to = toIndex;
            }
            updateObjectsSizeLast = updateObjects.size();
        }
        for (int iu=0;iu<itemUpdaters.length;iu++) {
            itemUpdaters[iu].beforeUpdate();

            for (int it = 0; it < threadCount; it++) {
                workers[it].updater = itemUpdaters[iu];
                threads[it] = Thread.ofVirtual().start(workers[it]);
            }

            for (int it = 0; it < threadCount; it++) {
                try {
                    threads[it].join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            itemUpdaters[iu].afterUpdate();
        }

    }


    public interface ItemUpdater<T> {
        void updateFromThread(T object, int index); // called for every item in the list, order doesn't matter

        default void afterUpdate(){};

        default void beforeUpdate(){};
    }
}