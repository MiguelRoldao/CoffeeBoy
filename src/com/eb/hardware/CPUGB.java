package com.eb.hardware;

public class CPUGB extends CPU {
    public CPUGB(Cartridge cartridge) {
        super(cartridge);
        reset();
    }

    @Override
    public void reset() {
        a = (byte)0x01;
        f = (byte)0xB0;
        b = (byte)0x00;
        c = (byte)0x13;
        d = (byte)0x00;
        e = (byte)0xd8;
        h = (byte)0x01;
        l = (byte)0x4d;

        sp = (short)0xfffe;
        pc = (short)0x0100;
        ime = false;
        disassembly = "";
    }
}
