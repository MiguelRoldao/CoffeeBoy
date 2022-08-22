package com.eb.hardware;

import java.awt.image.BufferedImage;

public class PPU {
    BufferedImage frameBuffer;

    final int screenWidth = 160;
    final int screenHeight = 144;

    final int imageType = BufferedImage.TYPE_INT_RGB;


    MemoryMap mem;

    public PPU(MemoryMap mem) {
        this.mem = mem;
        frameBuffer = new BufferedImage(screenWidth, screenHeight, imageType);
    }

    public void render() {
        byte scrollX;
        byte scrollY;
        try {
            scrollX = mem.readHigh(IO.SCX);
            scrollY = mem.readHigh(IO.SCY);
        } catch (MemoryException e) {
            throw new RuntimeException(e);
        }


        for (int y = 0; y<screenHeight; y++) {
            for (int x = 0; x<screenWidth; x++) {
                int coordX = (x + scrollX) & 0xff;
                int coordY = (y + scrollY) & 0xff;
                int tileX = coordX / 8;
                int tileY = coordY / 8;
                int pixelX = coordX % 8;
                int pixelY = coordY % 8;
                int pixel;
                try {
                    byte tileID = mem.read((short) (0x9800 + tileX + tileY * 32));
                    int addr = 0x8000 + (tileID & 0xff) * 16 + pixelY * 2;
                    byte lowerData = mem.read((short) addr);
                    byte higherData = mem.read((short) (addr + 1));
                    boolean lowPixel = ((lowerData >> (7 - pixelX)) & 1) == 1;
                    boolean highPixel = ((higherData >> (7 - pixelX)) & 1) == 1;
                    pixel = (highPixel ? 2 : 0) | (lowPixel ? 1 : 0);
                    frameBuffer.setRGB(x, y,
                            switch (pixel) {
                                case 0 -> 0x2c1e74;
                                case 1 -> 0xc23a73;
                                case 2 -> 0xd58863;
                                case 3 -> 0xdad3af;
                                default -> 0x00ff00;
                            });
                } catch (MemoryException e) {
                    throw new RuntimeException(e);
                }

            }
        }

    }

    public BufferedImage getFrameBuffer() {
        return frameBuffer;
    }
}
