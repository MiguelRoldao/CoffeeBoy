package com.eb.awtcli;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.*;

public class EmuDesktop {

    JFrame frame;
    public BufferedImage image;
    public JPanel imagePanel;
    JMenuBar mb;

    static final int bufferWidth = 160;
    static final int bufferHeight = 144;

    private double zoom;

    public boolean run = true;

    int nFrames = 0;


    public void setZoom(double zoom) {
        this.zoom = zoom;
        Dimension d = new Dimension((int) (bufferWidth * zoom), (int) (bufferHeight * zoom));
        System.out.printf("New dimensions: %d, %d\n", d.width, d.height);
        imagePanel.setMinimumSize(new Dimension((int) (bufferWidth * zoom), (int) (bufferHeight * zoom)));
        imagePanel.setPreferredSize(new Dimension((int) (bufferWidth * zoom), (int) (bufferHeight * zoom)));
        if (frame.getExtendedState() != JFrame.NORMAL) {
            frame.setExtendedState(JFrame.NORMAL);
        }
        frame.pack();
    }

    public double getZoom() {
        return zoom;
    }

    public EmuDesktop() {
        try {
            String name = UIManager.getSystemLookAndFeelClassName();
            System.out.println(name);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        frame = new JFrame("EmuBoy");

        // create image buffer
        image = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
        imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            }
        };
        imagePanel.setMinimumSize(new Dimension(bufferWidth, bufferHeight));
        imagePanel.setPreferredSize(new Dimension(bufferWidth, bufferHeight));
        image.setRGB(1,1, 0xffffffff);
        frame.add(imagePanel);

        // create menu bar
        mb = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem menuFileOpen = new JMenuItem("Open");
        JMenuItem menuFileExit = new JMenuItem("Exit");
        menuFile.add(menuFileOpen);
        menuFile.add(menuFileExit);
        mb.add(menuFile);
        JMenu menuView = new JMenu("View");
        JMenuItem menuView100 = new JMenuItem(new AbstractAction("100%") {
            public void actionPerformed(ActionEvent e) {
                setZoom(1.0);
            }
        });
        JMenuItem menuView200 = new JMenuItem(new AbstractAction("200%") {
            public void actionPerformed(ActionEvent e) {
                setZoom(2.0);
            }
        });
        menuView.add(menuView100);
        menuView.add(menuView200);
        mb.add(menuView);
        JMenu menuEmulator = new JMenu("Emulator");
        JMenuItem menuEmulatorPause = new JMenuItem(new AbstractAction("Pause") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                run = false;
            }
        });
        JMenuItem menuEmulatorRun = new JMenuItem(new AbstractAction("Run") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                run = true;
            }
        });
        JMenuItem menuEmulatorNextFrame = new JMenuItem(new AbstractAction("Next frame") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!run) nFrames = 1;
            }
        });
        menuEmulator.add(menuEmulatorPause);
        menuEmulator.add(menuEmulatorRun);
        menuEmulator.add(menuEmulatorNextFrame);
        mb.add(menuEmulator);
        frame.setJMenuBar(mb);


        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.pack();
        frame.setMinimumSize( frame.getSize() );
    }

    public boolean isRunning() {
        if (run) return true;
        if (nFrames > 0) {
            nFrames--;
            return true;
        }
        return false;
    }

    // Main Method
    public static void main(String args[]) {
        EmuDesktop cli = new EmuDesktop();
    }
}