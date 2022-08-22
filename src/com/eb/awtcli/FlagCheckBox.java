package com.eb.awtcli;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class FlagCheckBox extends JCheckBox {
    DebugDesktop dbg;

    public FlagCheckBox(DebugDesktop dbg, String text) {
        super(text);
        this.dbg = dbg;

        /*addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() != ItemEvent.SELECTED && itemEvent.getStateChange() != ItemEvent.DESELECTED) return;
                boolean state = itemEvent.getStateChange() == ItemEvent.SELECTED;
                setSelected(!state);
                dbg.updateRegistry();
                System.out.println("sup from checkbox: " + this);
            }
        });*/

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                setValue(selected);
                dbg.updateRegistry();
            }
        });
    }

    abstract public void setValue(boolean value);
}
