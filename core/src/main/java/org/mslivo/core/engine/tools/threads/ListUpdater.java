package org.mslivo.core.engine.tools.threads;

public interface ListUpdater<T> {

    void updateFromThread(T object, int index);
}
