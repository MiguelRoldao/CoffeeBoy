package com.eb.hardware;

public abstract class Cartridge implements MemoryComponent {
    static final int cartridgeTypeAddress = 0x147;
    static final int RomOnly = 0;
    static final int RomMbc1 = 1;

    abstract void reset();

    static Cartridge createCartridgeFromRom(byte rom[]) throws Exception {
        int code = rom[cartridgeTypeAddress];
        Cartridge cart = switch (code) {
            case RomOnly -> new RomOnlyCartridge(rom);
            default -> throw new Exception("Invalid cartridge code: " + code);
        };
        return cart;
    }
}
