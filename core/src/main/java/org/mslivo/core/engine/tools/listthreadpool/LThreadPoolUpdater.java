package org.mslivo.core.engine.tools.listthreadpool;

public interface LThreadPoolUpdater<T> {

    void updateFromThread(T object, int index);
}
