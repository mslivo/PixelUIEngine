package org.mslivo.core.engine.tools.particles;

public interface CustomDataProvider<D> {

    D provideNewInstance();

    void setValues(D customData);

}
