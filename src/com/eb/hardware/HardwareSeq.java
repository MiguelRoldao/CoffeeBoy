package com.eb.hardware;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HardwareSeq {
    byte[] rom;
    Cartridge cartridge;
    public CPU cpu;

    PPU ppu;

    boolean run = true;

    public HardwareSeq() {}

    public HardwareSeq(String path) {
        load(path);
        cpu.disassemble = true;
    }

    int cycles = 0;
    public void frame() throws Exception {
        // v_blank + 144 = 154
        for (int y = 0; y<154; y++) {
            cpu.mem.writeHigh(IO.LY, (byte)y);

            // signal V-Blank interrupt flag
            if (y == 144 && cpu.ime) {
                byte intflags = cpu.mem.readHigh(0xff);
                if ((intflags & 0b000_00001) != 0) {
                    byte intflagreg = cpu.mem.readHigh(0x0f);
                    cpu.mem.writeHigh(0x0f, (byte) (intflagreg | 1));
                    // TODO this
                }
            }

            // process instructions
            // 160 + h_blank = 456; 456 / 4 = 114
            while (cycles < 114) {
                cycles += cpu.step();
                if (cpu.disassemble) System.out.printf("0x%04x: %s\n", cpu.pc, cpu.disassembly);
            }
            cycles -= 114;
        }

        ppu.render();
    }

    public void load(String path) {
        try {
            rom = Files.readAllBytes(Paths.get(path));
            cartridge = Cartridge.createCartridgeFromRom(rom);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cpu = new CPUGB(cartridge);
        ppu = new PPU(cpu.mem);
    }

    public void reset() {
        cartridge.reset();
        cpu.reset();
    }

    public BufferedImage getFrameBuffer() {
        return ppu.getFrameBuffer();
    }

    public Object cloneFrameBuffer() {
        BufferedImage bi = ppu.getFrameBuffer();
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
