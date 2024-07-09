package net.mslivo.core.engine.tools.threads;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;

public class ForkJoinListUpdater<T> {

    private final List<T> updateObjects;
    private final ItemUpdater<T> itemUpdater;
    private final ForkJoinPool forkJoinPool;
    private int threshold;
    private ArrayDeque<UpdateTask> updateTaskPool;
    private int listSizeLast;

    public ForkJoinListUpdater(List<T> updateObjects, ItemUpdater<T> itemUpdater) {
        this.updateObjects = updateObjects;
        this.itemUpdater = itemUpdater;
        this.forkJoinPool = new ForkJoinPool();
        this.updateTaskPool = new ArrayDeque<>();
    }

    private int calculateThreshold() {
        return 1000;
        //return MathUtils.round(updateObjects.size() / Runtime.getRuntime().availableProcessors());
    }

    private synchronized UpdateTask getNextUpdateTask(int threshold, int start, int end) {
        UpdateTask updateTask = updateTaskPool.poll();
        if (updateTask == null){
            updateTask = new UpdateTask(threshold, start,end);
        }else{
            updateTask.reinitialize();
            updateTask.reset(threshold, start, end);
        }
        return updateTask;
    }

    private synchronized void addUpdateTaskToPool(UpdateTask updateTask){
        this.updateTaskPool.add(updateTask);
    }

    public void update() {
        /*if(updateObjects.size() != this.listSizeLast){
            this.threshold = calculateThreshold();
            this.listSizeLast = updateObjects.size();
        }

        this.forkJoinPool.invoke(getNextUpdateTask(this.threshold, 0, updateObjects.size()));*/
        updateObjects.parallelStream().forEach(new Consumer<T>() {
            @Override
            public void accept(T t) {
                itemUpdater.updateFromThread(t, 0);
            }
        });
    }


    private class UpdateTask extends RecursiveAction {
        private int start;
        private int end;
        private int threshold; // adjust threshold based on performance

        UpdateTask(int threshold, int start, int end) {
            this.threshold = threshold;
            this.start = start;
            this.end = end;
        }

        protected void reset(int threshold, int start, int end) {
            this.threshold = threshold;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start <= this.threshold) {
                for (int i = start; i < end; i++) {
                    itemUpdater.updateFromThread(updateObjects.get(i), i);
                }
            } else {
                int mid = (start + end) / 2;
                invokeAll(getNextUpdateTask(this.threshold, start, mid), getNextUpdateTask(this.threshold, mid, end));
            }

            addUpdateTaskToPool(this);
        }
    }

    public interface ItemUpdater<T> {
        void updateFromThread(T object, int index);
    }
}