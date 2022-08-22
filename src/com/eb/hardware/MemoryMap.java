package com.eb.hardware;

public class MemoryMap implements MemoryComponent {
    MemoryComponent cartridge;
    OAM oam[];
    Vram vram;
    Wram wram;
    byte iram[];
    IO io;


    boolean prohibitedMemoryAccessAllowed = true;

    public MemoryMap(MemoryComponent cartridge) {
        this.cartridge = cartridge;
        oam = new OAM[40];
        for (int i = 0; i<40; i++) {
            oam[i] = new OAM();
        }
        vram = new VramGB();
        wram = new WramGB();
        iram = new byte[127];
        io = new IO();
    }

    @Override
    public void write(short addr, byte data) throws MemoryException {
        int addri = addr & 0xffff;
        if (addri < 0x8000 || addri >= 0xa000 && addri < 0xc000) {
            cartridge.write(addr, data);
        } else if (addri >= 0x8000 && addri < 0xa000) {
            vram.write((short) (addri - 0x8000), data);
        } else if (addri >= 0xc000 && addri < 0xe000) {
            wram.write((short) (addri - 0xc000), data);
        } else if (addri >= 0xfe00 && addri < 0xfea0) {
            int idx = addri - 0xfe00;
            oam[idx / 4].write(idx % 4, data);
        } else if (addri >= 0xff00 && addri < 0xff4c || addri == 0xffff) {
            io.write((short) (addri - 0xff00), data);
        } else if (addri >= 0xff80 && addri < 0xffff) {
            iram[addri - 0xff80] = data;
        } else {
            if (prohibitedMemoryAccessAllowed) {
                if (addri >= 0xe000 && addri < 0xfe00) {
                    wram.write((short) (addri - 0xe000), data);
                }
            } else {
                throw new MemoryException(addr, "Tried to access prohibited memory space");
            }
        }
    }

    @Override
    public byte read(short addr) throws MemoryException {
        int addri = addr & 0xffff;
        byte retval = (byte)0xff;
        if (addri < 0x8000 || addri >= 0xa000 && addri < 0xc000) {
            retval = cartridge.read(addr);
        } else if (addri >= 0x8000 && addri < 0xa000) {
            retval = vram.read((short) (addri - 0x8000));
        } else if (addri >= 0xc000 && addri < 0xe000) {
            retval = wram.read((short) (addri - 0xc000));
        } else if (addri >= 0xfe00 && addri < 0xfea0) {
            int idx = addri - 0xfe00;
            retval = oam[idx / 4].read(idx % 4);
        } else if (addri >= 0xff00 && addri < 0xff4c || addri == 0xffff) {
            retval = io.read((short) (addr - 0xff00));
        } else if (addri >= 0xff80 && addri < 0xffff) {
            retval = iram[addri - 0xff80];
        } else {
            if (prohibitedMemoryAccessAllowed) {
                if (addri >= 0xe000 && addri < 0xfe00) {
                    retval = wram.read((short) (addr - 0xe000));
                }
            } else {
                throw new MemoryException(addr, "Tried to access prohibited memory space");
            }
        }
        return retval;
    }

    public void writeHigh(int addr, byte data) throws MemoryException {
        if (addr >= 0x00 && addr < 0x4c || addr == 0xff) {
            io.write((short)addr, data);
        } else if (addr >= 0x80 && addr < 0xff) {
            iram[addr - 0x80] = data;
        } else {
            throw new MemoryException(addr, "Tried to access prohibited memory space");
        }
    }

    public byte readHigh(int addr) throws MemoryException {
        if (addr >= 0x00 && addr < 0x4c || addr == 0xff) {
            return io.read((short)addr);
        } else if (addr >= 0x80 && addr < 0xff) {
            return iram[addr - 0x80];
        } else {
            throw new MemoryException(addr, "Tried to access prohibited memory space");
        }
    }
}
