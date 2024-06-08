package net.mslivo.core.engine.tools.threads;

import java.util.List;

class Worker<T> implements Runnable {

    public int from, to;
    public ThreadedListUpdater.ItemUpdater updater;
    public List<T> updateObjects;

    public Worker(List<T> updateObjects) {
        this.updateObjects = updateObjects;
    }

    @Override
    public void run() {
        //System.out.println("start "+from+"-"+to);
        for (int i = from; i <= to; i++) {
            updater.updateFromThread(updateObjects.get(i), i);
        }
        //System.out.println("end "+from+"-"+to);
    }
}