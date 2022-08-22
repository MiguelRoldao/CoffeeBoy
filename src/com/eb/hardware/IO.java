package com.eb.hardware;

public class IO implements MemoryComponent {
    byte bulk[];
    byte IE;

    static final short P1 = 0x00;
    static final short SB = 0x01;
    static final short SC = 0x02;
    static final short DIV = 0x04;
    static final short TIMA = 0x05;
    static final short IF = 0x0f;
    static final short NR10 = 0x10;
    static final short NR11 = 0x11;
    static final short NR12 = 0x12;
    static final short NR13 = 0x13;
    static final short NR14 = 0x14;
    static final short NR21 = 0x16;
    static final short NR22 = 0x17;
    static final short NR23 = 0x18;
    static final short NR24 = 0x19;
    static final short NR30 = 0x1a;
    static final short NR31 = 0x1b;
    static final short NR32 = 0x1c;
    static final short NR33 = 0x1d;
    static final short NR34 = 0x1e;
    static final short NR41 = 0x20;
    static final short NR42 = 0x21;
    static final short NR43 = 0x22;
    static final short NR44 = 0x23;
    static final short NR50 = 0x24;
    static final short NR51 = 0x25;
    static final short NR52 = 0x26;
    static final short WPRAM = 0x30;
    static final short LCDC = 0x40;
    static final short STAT = 0x41;
    static final short SCY = 0x42;
    static final short SCX = 0x43;
    static final short LY = 0x44;
    static final short LYC = 0x45;
    static final short DMA = 0x46;
    static final short BGP = 0x47;
    static final short OBP0 = 0x48;
    static final short OBP1 = 0x49;
    static final short WY = 0x4a;
    static final short WX = 0x4b;

    public IO(byte[] bulk, byte IE) {
        this.bulk = bulk;
        this.IE = IE;
    }

    public IO() {
        bulk = new byte[0x4c];
        IE = 0;
    }

    public void write(short addr, byte data) {
        if (addr == 0xff) {
            IE = data;
        } else {
            bulk[addr] = data;
        }
    }

    public byte read(short addr) {
        if (addr == 0xff) {
            return IE;
        } else {
            return bulk[addr];
        }
    }

}
