package net.mslivo.core.engine.tools.appengine;

public class AppEngineIO {

    public enum PARAMETER_TYPE {
        OBJECT, INTEGER, FLOAT
    }

    public static final int PARAMETERS_MAX = 16;
    private static final String ERROR_PARAMETERS_MAX = String.format("Push exceeds maximum parameter limit of %s", PARAMETERS_MAX);
    private static final String ERROR_PARAMETERS_COUNT = "Poll exceeds the parameter count of %s";
    int type;
    int readIndex, writeIndex;
    Object[] objectStack;
    int[] intStack;
    float[] floatStack;
    PARAMETER_TYPE[] parameterTypes;

    AppEngineIO() {
        this.objectStack = new Object[PARAMETERS_MAX];
        this.intStack = new int[PARAMETERS_MAX];
        this.floatStack = new float[PARAMETERS_MAX];
        this.parameterTypes = new PARAMETER_TYPE[PARAMETERS_MAX];
    }

    public AppEngineIO push(Object parameter) {
        if (writeIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        objectStack[writeIndex] = parameter;
        parameterTypes[writeIndex] = PARAMETER_TYPE.OBJECT;
        writeIndex++;
        return this;
    }

    public AppEngineIO push(Object parameter1, Object parameter2) {
        push(parameter1).push(parameter2);
        return this;
    }

    public AppEngineIO push(Object parameter1, Object parameter2, Object parameter3) {
        push(parameter1).push(parameter2).push(parameter3);
        return this;
    }

    public AppEngineIO push(Object parameter1, Object parameter2, Object parameter3, Object parameter4) {
        push(parameter1).push(parameter2).push(parameter3).push(parameter4);
        return this;
    }

    public AppEngineIO pushInt(int parameter) {
        if (writeIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        intStack[writeIndex] = parameter;
        parameterTypes[writeIndex] = PARAMETER_TYPE.INTEGER;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushInt(int parameter1, int parameter2) {
        push(parameter1);
        push(parameter2);
        return this;
    }

    public AppEngineIO pushInt(int parameter1, int parameter2, int parameter3) {
        push(parameter1);
        push(parameter2);
        push(parameter3);
        return this;
    }

    public AppEngineIO pushInt(int parameter1, int parameter2, int parameter3, int parameter4) {
        push(parameter1);
        push(parameter2);
        push(parameter3);
        push(parameter4);
        return this;
    }

    public AppEngineIO pushFloat(float parameter) {
        if (writeIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        floatStack[writeIndex] = parameter;
        parameterTypes[writeIndex] = PARAMETER_TYPE.FLOAT;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushFloat(float parameter1, float parameter2) {
        push(parameter1);
        push(parameter2);
        return this;
    }

    public AppEngineIO pushFloat(float parameter1, float parameter2, float parameter3) {
        push(parameter1);
        push(parameter2);
        push(parameter3);
        return this;
    }

    public AppEngineIO pushFloat(float parameter1, float parameter2, float parameter3, float parameter4) {
        push(parameter1);
        push(parameter2);
        push(parameter3);
        push(parameter4);
        return this;
    }

    public Object poll() {
        checkPoll();
        return switch (parameterTypes[readIndex]){
            case OBJECT -> objectStack[readIndex++];
            case INTEGER -> intStack[readIndex++];
            case FLOAT -> floatStack[readIndex++];
        };
    }

    public int pollInt() {
        checkPoll();
        return intStack[readIndex++];
    }

    public float pollFloat() {
        checkPoll();
        return floatStack[readIndex++];
    }


    public int type() {
        return type;
    }

    public PARAMETER_TYPE nextParameterType() {
        return parameterTypes[readIndex];
    }
    
    public int parametersCount(){
        return writeIndex;
    }
    
    public int parametersLeft(){
        return writeIndex-readIndex;
    }

    private void checkPoll(){
        if (readIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        if (readIndex >= writeIndex) throw new RuntimeException(String.format(ERROR_PARAMETERS_COUNT, writeIndex));
    }

}
