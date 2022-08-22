package com.eb.hardware;

public interface MemoryComponent {
    public void write(short addr, byte data) throws MemoryException;
    public byte read(short addr) throws MemoryException;
}
