package com.eb.hardware;

public class OP {
    private byte code;
    private byte x;
    private byte y;
    private byte z;
    private byte p;
    private byte q;

    OP(byte code) {
        set(code);
    }

    void set(byte code) {
        this.code = code;
        x = (byte)((code >> 6) & 0b11);
        y = (byte)((code >> 3) & 0b111);
        z = (byte)((code >> 0) & 0b111);
        p = (byte)((y >> 1) & 0b11);
        q = (byte)((y >> 0) & 0b1);
    }

    public byte getCode() {
        return code;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public byte getZ() {
        return z;
    }

    public byte getP() {
        return p;
    }

    public byte getQ() {
        return q;
    }

    @Override
    public String toString() {
        return "OP{" +
                "code=" + String.format("0x%02x", code) +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", p=" + p +
                ", q=" + q +
                '}';
    }
}