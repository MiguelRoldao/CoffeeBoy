package com.eb.hardware;

public class WramGB extends Wram {

    public WramGB() {
        data = new byte[8192];
    }

    @Override
    public void write(short addr, byte data) {
        this.data[addr] = data;
    }

    @Override
    public byte read(short addr) {
        return data[addr];
    }
}
