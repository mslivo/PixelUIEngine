package org.mslivo.core.engine.tools.lthreadpool;

public interface LThreadPoolUpdater<T> {

    void updateFromThread(T object, int index);
}
