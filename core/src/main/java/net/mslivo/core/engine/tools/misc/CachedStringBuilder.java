package net.mslivo.core.engine.tools.misc;

import java.io.IOException;
import java.util.stream.IntStream;

public class CachedStringBuilder implements Appendable, Comparable<CachedStringBuilder>, CharSequence {
    private final StringBuilder current;
    private final StringBuilder previous;
    private String cached;
    private boolean dirty;

    public CachedStringBuilder() {
        this.current = new StringBuilder();
        this.previous = new StringBuilder();
        this.cached = "";
        this.dirty = true;
    }

    public CachedStringBuilder append(CharSequence csq) {
        current.append(csq);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder delete(int start, int end) {
        current.delete(start, end);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder deleteCharAt(int index) {
        current.deleteCharAt(index);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder replace(int start, int end, String str) {
        current.replace(start, end, str);
        this.dirty = true;
        return this;
    }


    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        current.append(csq, start, end);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(char c) {
        current.append(c);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(Object o) {
        current.append(o);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(char[] str, int offset, int len) {
        current.append(str, offset, len);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(int i) {
        current.append(i);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(long l) {
        current.append(l);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(float f) {
        current.append(f);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(double d) {
        current.append(d);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(boolean b) {
        current.append(b);
        this.dirty = true;
        return this;
    }

    public CachedStringBuilder append(StringBuilder sb) {
        current.append(sb);
        this.dirty = true;
        return this;
    }

    public void setLength(int length) {
        if(current.length() != length){
            current.setLength(length);
            this.dirty = true;
        }
    }

    public void reset() {
        current.setLength(0);
        this.dirty = true;
    }

    @Override
    public int length() {
        return current.length();
    }

    @Override
    public char charAt(int index) {
        return current.charAt(index);
    }

    @Override
    public boolean isEmpty() {
        return current.isEmpty();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return current.subSequence(start, end);
    }

    @Override
    public String toString() {
        if (dirty && !equals(current, previous)) {
            // Only now allocate a new String
            cached = current.toString();

            // Copy current into previous
            previous.setLength(0);
            previous.append(current);
            this.dirty = false;
        }
        return cached;
    }

    @Override
    public IntStream chars() {
        return current.chars();
    }

    @Override
    public IntStream codePoints() {
        return current.codePoints();
    }

    private static boolean equals(StringBuilder a, StringBuilder b) {
        int len = a.length();
        if (b.length() != len) return false;
        for (int i = 0; i < len; i++)
            if (a.charAt(i) != b.charAt(i)) return false;
        return true;
    }

    @Override
    public int compareTo(CachedStringBuilder o) {
        return this.toString().compareTo(o.toString());
    }


    public CachedStringBuilder insert(int index, char[] str, int offset,
                                      int len) {
        current.insert(index, str, offset, len);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, Object obj) {
        current.insert(offset, obj);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, String str) {
        current.insert(offset, str);
        this.dirty = true;
        return this;
    }

    public void setCharAt(int index, char ch) {
        current.setCharAt(index, ch);
        this.dirty = true;
    }

    public CachedStringBuilder insert(int offset, char[] str) {
        current.insert(offset, str);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int dstOffset, CharSequence s) {
        current.insert(dstOffset, s);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int dstOffset, CharSequence s,
                                      int start, int end) {
        current.insert(dstOffset, s, start, end);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, boolean b) {
        current.insert(offset, b);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, char c) {
        current.insert(offset, c);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, int i) {
        current.insert(offset, i);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, long l) {
        current.insert(offset, l);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, float f) {
        current.insert(offset, f);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder insert(int offset, double d) {
        current.insert(offset, d);
        this.dirty = true;
        return this;
    }

    public int indexOf(String str) {
        return current.indexOf(str);
    }

    public int indexOf(String str, int fromIndex) {
        return current.indexOf(str, fromIndex);
    }

    public int lastIndexOf(String str) {
        return current.lastIndexOf(str);
    }

    public int lastIndexOf(String str, int fromIndex) {
        return current.lastIndexOf(str, fromIndex);
    }

    public CachedStringBuilder reverse() {
        current.reverse();
        return this;
    }


    public CachedStringBuilder repeat(int codePoint, int count) {
        current.repeat(codePoint, count);
        this.dirty = true;
        return this;
    }


    public CachedStringBuilder repeat(CharSequence cs, int count) {
        current.repeat(cs, count);
        this.dirty = true;
        return this;
    }
}