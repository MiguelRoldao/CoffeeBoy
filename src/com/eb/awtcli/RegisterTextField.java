package com.eb.awtcli;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class RegisterTextField extends JTextField {

    Color defaultColor;
    DebugDesktop dbg;


    public RegisterTextField(DebugDesktop dbg) {
        super("0000", 4);
        this.dbg = dbg;
        defaultColor = getForeground();
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    //System.out.println("pressed enter");
                    setForeground(defaultColor);
                    try {
                        int number = Integer.parseInt(getText(), 16);
                        setValue((short) number);
                        //System.out.println("new value was set: " + String.format("0x%04x", dbg.hw.cpu.getAF()));
                    } catch (NumberFormatException e) {

                    }
                    dbg.updateRegistry();
                } else {
                    setForeground(Color.RED);
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {}
        });
    }

    abstract public void setValue(short value);
}
