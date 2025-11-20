package net.mslivo.pixelui.utils.misc;

public abstract class ValueWatcher {

    protected boolean init;

    protected boolean shouldChange(boolean condition) {
        if (!init || condition) {
            init = true;
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // INT
    // ---------------------------------------------------------------------
    public static class Int extends ValueWatcher {
        private int value;
        private int diff;

        public boolean hasChanged(int v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public int value() { return value; }
        public int delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // LONG
    // ---------------------------------------------------------------------
    public static class Long extends ValueWatcher {
        private long value;
        private long diff;

        public boolean hasChanged(long v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public long value() { return value; }
        public long delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // FLOAT
    // ---------------------------------------------------------------------
    public static class Float extends ValueWatcher {
        private float value;
        private float diff;

        public boolean hasChanged(float v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public float value() { return value; }
        public float delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // DOUBLE
    // ---------------------------------------------------------------------
    public static class Double extends ValueWatcher {
        private double value;
        private double diff;

        public boolean hasChanged(double v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public double value() { return value; }
        public double delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // BYTE
    // ---------------------------------------------------------------------
    public static class Byte extends ValueWatcher {
        private byte value;
        private int diff; // widened difference fits any byte delta

        public boolean hasChanged(byte v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public byte value() { return value; }
        public int delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // SHORT
    // ---------------------------------------------------------------------
    public static class Short extends ValueWatcher {
        private short value;
        private int diff; // widened difference fits any short delta

        public boolean hasChanged(short v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public short value() { return value; }
        public int delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // CHAR
    // ---------------------------------------------------------------------
    public static class Char extends ValueWatcher {
        private char value;
        private int diff; // widening OK

        public boolean hasChanged(char v) {
            if (shouldChange(v != value)) {
                diff = v - value;
                value = v;
                return true;
            }
            return false;
        }

        public char value() { return value; }
        public int delta() { return diff; }
    }

    // ---------------------------------------------------------------------
    // BOOLEAN (no diff possible)
    // ---------------------------------------------------------------------
    public static class Bool extends ValueWatcher {
        private boolean value;

        public boolean hasChanged(boolean v) {
            if (shouldChange(v != value)) {
                value = v;
                return true;
            }
            return false;
        }

        public boolean value() { return value; }

        // no delta() — not meaningful for boolean
    }
}
