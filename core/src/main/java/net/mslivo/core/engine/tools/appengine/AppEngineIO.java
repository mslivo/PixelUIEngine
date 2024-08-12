package net.mslivo.core.engine.tools.appengine;

public class AppEngineIO {

    public enum PARAMETER_TYPE {
        OBJECT, INTEGER, FLOAT, BOOLEAN
    }

    public static final int PARAMETERS_MAX = 16;
    private static final String ERROR_PARAMETERS_MAX = String.format("Push exceeds maximum parameter limit of %s", PARAMETERS_MAX);
    private static final String ERROR_INT_AUTOBOXING = "Integer autoboxed, use pushInt() instead of push()";
    private static final String ERROR_FLOAT_AUTOBOXING = "Float autoboxed, use pushFloat() instead of push()";
    private static final String ERROR_BOOLEAN_AUTOBOXING = "Boolean autoboxed, use pushBoolean() instead of push()";
    private static final String ERROR_PARAMETERS_COUNT = "Poll exceeds the parameter count of %s";

    int type;
    int readIndex, writeIndex;
    final Object[] objectStack;
    final int[] intStack;
    final float[] floatStack;
    final boolean[] booleanStack;

    PARAMETER_TYPE[] parameterTypes;

    AppEngineIO() {
        this.objectStack = new Object[PARAMETERS_MAX];
        this.intStack = new int[PARAMETERS_MAX];
        this.floatStack = new float[PARAMETERS_MAX];
        this.booleanStack = new boolean[PARAMETERS_MAX];
        this.parameterTypes = new PARAMETER_TYPE[PARAMETERS_MAX];
    }

    public AppEngineIO push(Object parameter) {
        if (writeIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        switch (parameter){
            case Integer _ -> throw new RuntimeException(ERROR_INT_AUTOBOXING);
            case Float _ -> throw new RuntimeException(ERROR_FLOAT_AUTOBOXING);
            case Boolean _ -> throw new RuntimeException(ERROR_BOOLEAN_AUTOBOXING);
            case null, default -> {
                objectStack[writeIndex] = parameter;
                parameterTypes[writeIndex] = PARAMETER_TYPE.OBJECT;
                writeIndex++;
                return this;
            }
        }
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

    public AppEngineIO pushBoolean(boolean parameter) {
        if (writeIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PARAMETERS_MAX);
        booleanStack[writeIndex] = parameter;
        parameterTypes[writeIndex] = PARAMETER_TYPE.BOOLEAN;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2,boolean parameter3) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        pushBoolean(parameter3);
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2,boolean parameter3,boolean parameter4) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        pushBoolean(parameter3);
        pushBoolean(parameter4);
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
        pushInt(parameter1);
        pushInt(parameter2);
        return this;
    }

    public AppEngineIO pushInt(int parameter1, int parameter2, int parameter3) {
        pushInt(parameter1);
        pushInt(parameter2);
        pushInt(parameter3);
        return this;
    }

    public AppEngineIO pushInt(int parameter1, int parameter2, int parameter3, int parameter4) {
        pushInt(parameter1);
        pushInt(parameter2);
        pushInt(parameter3);
        pushInt(parameter4);
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
        pushFloat(parameter1);
        pushFloat(parameter2);
        return this;
    }

    public AppEngineIO pushFloat(float parameter1, float parameter2, float parameter3) {
        pushFloat(parameter1);
        pushFloat(parameter2);
        pushFloat(parameter3);
        return this;
    }

    public AppEngineIO pushFloat(float parameter1, float parameter2, float parameter3, float parameter4) {
        pushFloat(parameter1);
        pushFloat(parameter2);
        pushFloat(parameter3);
        pushFloat(parameter4);
        return this;
    }

    public Object poll() {
        checkPoll();
        return switch (parameterTypes[readIndex]){
            case OBJECT -> objectStack[readIndex++];
            case INTEGER -> intStack[readIndex++];
            case FLOAT -> floatStack[readIndex++];
            case BOOLEAN -> booleanStack[readIndex++];
        };
    }

    public boolean pollBoolean() {
        checkPoll();
        return booleanStack[readIndex++];
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
