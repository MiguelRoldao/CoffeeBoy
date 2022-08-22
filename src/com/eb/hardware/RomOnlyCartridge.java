package com.eb.hardware;

public class RomOnlyCartridge extends Cartridge {

    byte rom[];

    public RomOnlyCartridge(byte rom[]) {
        this.rom = rom;
    }

    @Override
    public void write(short addr, byte data) throws MemoryException {
        return;
    }

    @Override
    public byte read(short addr) throws MemoryException {
        if (addr < 0x0000 || addr > 0x7fff) {
            throw new MemoryException(addr, "RomOnly Cartridge invalid address: " + addr);
        }
        return rom[addr];
    }

    @Override
    void reset() {
        // nothing to reset
    }
}
