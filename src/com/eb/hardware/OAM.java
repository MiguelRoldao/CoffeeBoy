package com.eb.hardware;

public class OAM {
    byte y;
    byte x;
    byte id;
    byte flags;


    public void write(int idx, byte data) {
        switch (idx) {
            case 0:
                y = data;
                break;
            case 1:
                x = data;
                break;
            case 2:
                id = data;
                break;
            case 3:
                flags = data;
                break;
        }
    }

    public byte read(int idx) {
        byte retval = 0;
        switch (idx) {
            case 0:
                retval = y;
                break;
            case 1:
                retval = x;
                break;
            case 2:
                retval = id;
                break;
            case 3:
                retval = flags;
                break;
        }
        return retval;
    }


    // setters and getters
    public byte getY() {
        return y;
    }

    public void setY(byte y) {
        this.y = y;
    }

    public byte getX() {
        return x;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

}
