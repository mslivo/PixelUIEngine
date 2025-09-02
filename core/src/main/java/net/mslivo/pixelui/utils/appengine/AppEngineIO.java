package net.mslivo.pixelui.utils.appengine;

public class AppEngineIO {

    public static final int PARAMETERS_MAX = 16;
    private static final String ERROR_PUSH = String.format("Push exceeds maximum parameter limit of %s", PARAMETERS_MAX);
    private static final String ERROR_OBJECT_LOCKED = "IO Object is locked";
    private static final String ERROR_POLL = String.format("Poll exceeds the parameter count of %s",PARAMETERS_MAX);

    boolean locked;
    int type;
    int readIndex, writeIndex;
    final Object[] objectStack;
    final int[] intStack;
    final long[] longStack;
    final double[] doubleStack;
    final float[] floatStack;
    final boolean[] booleanStack;

    AppEngineIO() {
        this.objectStack = new Object[PARAMETERS_MAX];
        this.intStack = new int[PARAMETERS_MAX];
        this.longStack = new long[PARAMETERS_MAX];
        this.floatStack = new float[PARAMETERS_MAX];
        this.doubleStack = new double[PARAMETERS_MAX];
        this.booleanStack = new boolean[PARAMETERS_MAX];
        this.locked = false;
    }

    public AppEngineIO push(Object parameter) {
        checkWrite();
        objectStack[writeIndex] = parameter;
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

    public AppEngineIO pushBoolean(boolean parameter) {
        checkWrite();
        booleanStack[writeIndex] = parameter;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2, boolean parameter3) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        pushBoolean(parameter3);
        return this;
    }

    public AppEngineIO pushBoolean(boolean parameter1, boolean parameter2, boolean parameter3, boolean parameter4) {
        pushBoolean(parameter1);
        pushBoolean(parameter2);
        pushBoolean(parameter3);
        pushBoolean(parameter4);
        return this;
    }

    public AppEngineIO pushLong(long parameter) {
        checkWrite();
        longStack[writeIndex] = parameter;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushLong(long parameter1, long parameter2) {
        pushLong(parameter1);
        pushLong(parameter2);
        return this;
    }

    public AppEngineIO pushLong(long parameter1, long parameter2, long parameter3) {
        pushLong(parameter1);
        pushLong(parameter2);
        pushLong(parameter3);
        return this;
    }

    public AppEngineIO pushLong(long parameter1, long parameter2, long parameter3, long parameter4) {
        pushLong(parameter1);
        pushLong(parameter2);
        pushLong(parameter3);
        pushLong(parameter4);
        return this;
    }

    public AppEngineIO pushDouble(double parameter) {
        checkWrite();
        doubleStack[writeIndex] = parameter;
        writeIndex++;
        return this;
    }

    public AppEngineIO pushDouble(double parameter1, double parameter2) {
        pushDouble(parameter1);
        pushDouble(parameter2);
        return this;
    }

    public AppEngineIO pushDouble(double parameter1, double parameter2, double parameter3) {
        pushDouble(parameter1);
        pushDouble(parameter2);
        pushDouble(parameter3);
        return this;
    }

    public AppEngineIO pushDouble(double parameter1, double parameter2, double parameter3, double parameter4) {
        pushDouble(parameter1);
        pushDouble(parameter2);
        pushDouble(parameter3);
        pushDouble(parameter4);
        return this;
    }

    public AppEngineIO pushInt(int parameter) {
        checkWrite();
        intStack[writeIndex] = parameter;
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
        checkWrite();
        floatStack[writeIndex] = parameter;
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
        return poll(Object.class);
    }

    public <T> T poll(Class<T> type) {
        checkPoll();
        return (T) objectStack[readIndex++];
    }

    public boolean pollBoolean() {
        checkPoll();
        return booleanStack[readIndex++];
    }

    public int pollInt() {
        checkPoll();
        return intStack[readIndex++];
    }

    public long pollLong() {
        checkPoll();
        return longStack[readIndex++];
    }

    public float pollFloat() {
        checkPoll();
        return floatStack[readIndex++];
    }

    public double pollDouble() {
        checkPoll();
        return doubleStack[readIndex++];
    }

    public void skipPolls(int polls) {
        polls = Math.max(polls, 0);
        for (int i = 0; i < polls; i++) {
            checkPoll();
            readIndex++;
        }
    }


    public int type() {
        return type;
    }

    public int parametersCount() {
        return writeIndex;
    }

    public int parametersLeft() {
        return writeIndex - readIndex;
    }

    private void checkPoll() {
        if (readIndex >= PARAMETERS_MAX) throw new RuntimeException(ERROR_PUSH);
        if (readIndex >= writeIndex) throw new RuntimeException(ERROR_POLL);
    }

    private void checkWrite() {
        if (locked)
            throw new RuntimeException(ERROR_OBJECT_LOCKED);
        if (writeIndex >= PARAMETERS_MAX)
            throw new RuntimeException(ERROR_PUSH);
    }

}
