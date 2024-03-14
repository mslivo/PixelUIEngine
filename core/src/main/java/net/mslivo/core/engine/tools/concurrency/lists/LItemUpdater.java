package net.mslivo.core.engine.tools.concurrency.lists;

public interface LItemUpdater<T> {

    void updateFromThread(T object, int index);
}
