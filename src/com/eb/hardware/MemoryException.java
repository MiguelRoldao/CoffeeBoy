package com.eb.hardware;

public class MemoryException extends Exception {

    private int addr;
    public MemoryException(int addr, String msg) {
        super(msg);
        setAddr(addr);
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }
}
