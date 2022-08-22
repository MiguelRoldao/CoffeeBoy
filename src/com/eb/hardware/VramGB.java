package com.eb.hardware;

public class VramGB extends Vram {


    public VramGB() {
        data = new byte[8192];
    }

    @Override
    public void write(short addr, byte data) {
        this.data[addr] = data;
    }

    @Override
    public byte read(short addr) {
        return this.data[addr];
    }
}
