package net.mslivo.pixelui.utils.misc;

public class IntValueWatcher {
    private int value = 0;
    private int difference;
    private boolean initialized;

    public IntValueWatcher() {
        super();
    }

    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.difference = value - this.value;
        this.value = value;
    }

    public int delta() {
        return difference;
    }

    public boolean hasValueChanged(int currentValue) {
        if (!initialized || this.value != currentValue) {
            this.setValue(currentValue);
            this.initialized = true;
            return true;
        }
        return false;
    }

}