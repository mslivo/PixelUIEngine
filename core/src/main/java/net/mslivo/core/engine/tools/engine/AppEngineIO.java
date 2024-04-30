package net.mslivo.core.engine.tools.engine;

public class AppEngineIO {
    private static final String ERROR_PARAMETERS = String.format("Parameter exceeds limit of %s",AppEngine.PARAMETERS_MAX);
    int type;
    int readIndex, writeIndex;
    Object[] objectParams;
    int[] intParams;
    float[] floatParams;

    AppEngineIO(){
        this.objectParams = new Object[AppEngine.PARAMETERS_MAX];
        this.intParams = new int[AppEngine.PARAMETERS_MAX];
        this.floatParams = new float[AppEngine.PARAMETERS_MAX];
    }

    public AppEngineIO write(Object parameter){
        if(writeIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        objectParams[writeIndex++] = parameter;
        return this;
    }

    public AppEngineIO write(int parameter){
        if(writeIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        intParams[writeIndex++] = parameter;
        return this;
    }

    public AppEngineIO write(float parameter){
        if(writeIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        floatParams[writeIndex++] = parameter;
        return this;
    }

    public Object read(){
        if(readIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return objectParams[readIndex++];
    }

    public int readInt(){
        if(readIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return intParams[readIndex++];
    }

    public float readFloat(){
        if(readIndex >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return floatParams[readIndex++];
    }

    public Object read(int index){
        if(index >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return objectParams[index];
    }

    public int readInt(int index){
        if(index >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return intParams[index];
    }

    public float readFloat(int index){
        if(index >= AppEngine.PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS);
        return floatParams[index];
    }

    public int type(){
        return type;
    }

}
