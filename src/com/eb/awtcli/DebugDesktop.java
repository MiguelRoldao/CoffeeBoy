package com.eb.awtcli;

import com.eb.hardware.HardwareSeq;
import com.eb.hardware.MemoryException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class DebugDesktop {

    HardwareSeq hw;

    JFrame frame;
    JPanel tilesetPanel;
    BufferedImage imgTileset;

    final int tilesetWidth = 128;
    final int tilesetHeight = 192;

    JPanel tilemapPanel;
    BufferedImage imgTilemap;
    final int tilemapWidth = 256;
    final int tilemapHeight = 256;

    JTextField registryAFField;
    JTextField registryDEField;
    JTextField registryHLField;
    JTextField registryBCField;

    JCheckBox flagZBox;
    JCheckBox flagNBox;
    JCheckBox flagHBox;
    JCheckBox flagCBox;

    final int[] tilesetColors = {
            0x371f00,
            0x7f3f00,
            0xbf5f00,
            0xff7f00
    };


    public DebugDesktop (HardwareSeq hw) {
        this.hw = hw;

        frame = new JFrame("EmuBoy Debugger");
        frame.setLayout(new FlowLayout());


        // Create tileset panel
        imgTileset = new BufferedImage(tilesetWidth, tilesetHeight, BufferedImage.TYPE_INT_RGB);
        tilesetPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imgTileset, 0, 0, tilesetWidth *2, tilesetHeight *2, null);
            }
        };
        tilesetPanel.setMinimumSize(new Dimension(tilesetWidth *2, tilesetHeight *2));
        tilesetPanel.setPreferredSize(new Dimension(tilesetWidth *2, tilesetHeight *2));
        imgTileset.setRGB(1,1,0xffffffff);
        JPanel tilesetPanelBevel = new JPanel(new BorderLayout());
        tilesetPanelBevel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        tilesetPanelBevel.add(tilesetPanel);
        JPanel tilesetPanelBorder = new JPanel(new BorderLayout());
        tilesetPanelBorder.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Tileset"));
        tilesetPanelBorder.add(tilesetPanelBevel);
        frame.add(tilesetPanelBorder);

        // Create tilemap panel
        imgTilemap = new BufferedImage(tilemapWidth, tilemapHeight, BufferedImage.TYPE_INT_RGB);
        tilemapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(imgTilemap, 0, 0, tilemapWidth *2, tilemapHeight *2, null);
            }
        };
        tilemapPanel.setMinimumSize(new Dimension(tilemapWidth *2, tilemapHeight *2));
        tilemapPanel.setPreferredSize(new Dimension(tilemapWidth *2, tilemapHeight *2));
        imgTilemap.setRGB(1,1,0xffffffff);
        JPanel tilemapPanelBevel = new JPanel(new BorderLayout());
        tilemapPanelBevel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        tilemapPanelBevel.add(tilemapPanel);
        JPanel tilemapPanelBorder = new JPanel(new BorderLayout());
        tilemapPanelBorder.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Tilemap"));
        tilemapPanelBorder.add(tilemapPanelBevel);
        frame.add(tilemapPanelBorder);

        // Create register text fields
        JPanel registryPanel_ = new JPanel(new BorderLayout());
        registryPanel_.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Registry"));
        JPanel registryPanel = new JPanel();
        registryPanel.setLayout(new GridLayout(0, 2));
        registryAFField = new RegisterTextField(this) {
            @Override
            public void setValue(short value) {
                hw.cpu.setAF(value);
            }
        };
        registryBCField = new RegisterTextField(this) {
            @Override
            public void setValue(short value) {
                hw.cpu.setBC(value);
            }
        };
        registryDEField = new RegisterTextField(this) {
            @Override
            public void setValue(short value) {
                hw.cpu.setDE(value);
            }
        };
        registryHLField = new RegisterTextField(this) {
            @Override
            public void setValue(short value) {
                dbg.hw.cpu.setHL(value);
            }
        };
        registryPanel.add(new JLabel("AF"));
        registryPanel.add(registryAFField);
        registryPanel.add(new JLabel("BC"));
        registryPanel.add(registryBCField);
        registryPanel.add(new JLabel("DE"));
        registryPanel.add(registryDEField);
        registryPanel.add(new JLabel("HL"));
        registryPanel.add(registryHLField);
        registryPanel_.add(registryPanel);
        frame.add(registryPanel_);

        // Create register text fields
        JPanel flagsPanel_ = new JPanel(new BorderLayout());
        flagsPanel_.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Flags"));
        JPanel flagsPanel = new JPanel();
        flagsPanel.setLayout(new BoxLayout(flagsPanel, BoxLayout.PAGE_AXIS));
        flagZBox = new FlagCheckBox(this, "Zero") {
            @Override
            public void setValue(boolean value) {
                hw.cpu.setFz(value);
            }
        };
        flagNBox = new FlagCheckBox(this, "Negative") {
            @Override
            public void setValue(boolean value) {
                hw.cpu.setFn(value);
            }
        };
        flagHBox = new FlagCheckBox(this, "Half-carry") {
            @Override
            public void setValue(boolean value) {
                hw.cpu.setFh(value);
            }
        };
        flagCBox = new FlagCheckBox(this, "Carry") {
            @Override
            public void setValue(boolean value) {
                hw.cpu.setFc(value);
            }
        };
        flagsPanel.add(flagZBox);
        flagsPanel.add(flagNBox);
        flagsPanel.add(flagHBox);
        flagsPanel.add(flagCBox);
        flagsPanel_.add(flagsPanel);
        frame.add(flagsPanel_);


        // final initializations
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.pack();

    }

    public void update() {
        // paint image
        drawTileset();
        drawTilemap();

        updateRegistry();


        tilesetPanel.revalidate();
        tilesetPanel.repaint();
        tilemapPanel.revalidate();
        tilemapPanel.repaint();
    }

    public void drawTileset() {
        for (int i = 0; i<384; i++) {
            for (int row = 0; row<8; row++) {
                int addr = 0x8000 + i * 16 + row * 2;
                try {
                    byte loByte = hw.cpu.mem.read((short) addr);
                    byte hiByte = hw.cpu.mem.read((short) (addr+1));
                    for (int px = 7; px>=0; px--) {
                        int data = (((loByte>>px)&1)<<1) | ((hiByte>>px)&1);
                        int x = (7-px + i*8)%128;
                        int y = row + (i/16)*8;
                        imgTileset.setRGB(x, y, tilesetColors[data]);
                    }
                } catch (MemoryException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void drawTilemap() {
        for (int y = 0; y<tilemapHeight; y++) {
            for (int x = 0; x<tilemapWidth; x++) {
                int tileX = x / 8;
                int tileY = y / 8;
                int pixelX = x % 8;
                int pixelY = y % 8;
                int pixel;
                try {
                    byte tileID = hw.cpu.mem.read((short) (0x9800 + tileX + tileY * 32));
                    int addr = 0x8000 + (tileID & 0xff) * 16 + pixelY * 2;
                    byte lowerData = hw.cpu.mem.read((short) addr);
                    byte higherData = hw.cpu.mem.read((short) (addr + 1));
                    boolean lowPixel = ((lowerData >> (7 - pixelX)) & 1) == 1;
                    boolean highPixel = ((higherData >> (7 - pixelX)) & 1) == 1;
                    pixel = (highPixel ? 2 : 0) | (lowPixel ? 1 : 0);
                    imgTilemap.setRGB(x, y,
                            switch (pixel) {
                                case 0 -> 0x003f3f;
                                case 1 -> 0x007f7f;
                                case 2 -> 0x00bfbf;
                                case 3 -> 0x00ffff;
                                default -> 0xff0000;
                            });
                } catch (MemoryException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    public void updateRegistry() {
        registryAFField.setText(String.format("%04x", hw.cpu.getAF()));
        registryBCField.setText(String.format("%04x", hw.cpu.getBC()));
        registryDEField.setText(String.format("%04x", hw.cpu.getDE()));
        registryHLField.setText(String.format("%04x", hw.cpu.getHL()));

        flagZBox.setSelected(hw.cpu.getFz());
        flagNBox.setSelected(hw.cpu.getFn());
        flagHBox.setSelected(hw.cpu.getFh());
        flagCBox.setSelected(hw.cpu.getFc());
    }
}
