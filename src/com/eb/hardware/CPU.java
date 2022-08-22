package com.eb.hardware;

public abstract class CPU {
    protected byte b;
    protected byte c;
    protected byte d;
    protected byte e;
    protected byte h;
    protected byte l;
    protected byte a;
    protected byte f;

    protected short sp;
    protected short pc;

    // interrupt master enable
    protected boolean ime;

    public boolean disassemble = false;
    public String disassembly;

    public MemoryMap mem;

    public CPU(Cartridge cartridge) {
        mem = new MemoryMap(cartridge);
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
    }


    public int step() throws Exception {
        OP op = new OP(mem.read(pc++));
        return decodeOP(op);
    }

    private short word(byte high, byte low) {
        return (short)((high << 8) | (low&0xff));
    }

    void setR(int id, byte data) throws Exception {
        switch (id) {
            case 0 -> b = data;
            case 1 -> c = data;
            case 2 -> d = data;
            case 3 -> e = data;
            case 4 -> h = data;
            case 5 -> l = data;
            case 7 -> a = data;
            case 6 -> mem.write(getRP(2), data);
            default -> throw new Exception("Invalid R id: " + id);
        }
    }

    byte getR(int id) throws Exception {
        return switch (id) {
            case 0 -> b;
            case 1 -> c;
            case 2 -> d;
            case 3 -> e;
            case 4 -> h;
            case 5 -> l;
            case 7 -> a;
            case 6 -> mem.read(getRP(2));
            default -> throw new Exception("Invalid R id: " + id);
        };
    }

    void setRP(int id, short data) throws Exception {
        switch (id) {
            case 0 -> { b = (byte) (data >> 8); c = (byte) data; }
            case 1 -> { d = (byte) (data >> 8); e = (byte) data; }
            case 2 -> { h = (byte) (data >> 8); l = (byte) data; }
            case 3 -> sp = data;
            default -> throw new Exception("Invalid RP id: " + id);
        }
    }

    short getRP(int id) throws Exception {
        return switch (id) {
            case 0 -> word(b, c);
            case 1 -> word(d, e);
            case 2 -> word(h, l);
            case 3 -> sp;
            default -> throw new Exception("Invalid RP id: " + id);
        };
    }

    void setRP2(int id, short data) throws Exception {
        switch (id) {
            case 0 -> { b = (byte) (data >> 8); c = (byte) data; }
            case 1 -> { d = (byte) (data >> 8); e = (byte) data; }
            case 2 -> { h = (byte) (data >> 8); l = (byte) data; }
            case 3 -> { a = (byte) (data >> 8); f = (byte) data; }
            default -> throw new Exception("Invalid RP2 id: " + id);
        }
    }

    short getRP2(int id) throws Exception {
        return switch (id) {
            case 0 -> word(b, c);
            case 1 -> word(d, e);
            case 2 -> word(h, l);
            case 3 -> word(a, f);
            default -> throw new Exception("Invalid RP2 id: " + id);
        };
    }

    public short getBC() {
        return word(b, c);
    }
    public short getDE() {
        return word(d, e);
    }
    public short getHL() {
        return word(h, l);
    }
    public short getAF() {
        return word(a, f);
    }

    public void setBC(short data) {
        b = (byte) (data >> 8);
        c = (byte) data;
    }

    public void setDE(short data) {
        d = (byte) (data >> 8);
        e = (byte) data;
    }

    public void setHL(short data) {
        h = (byte) (data >> 8);
        l = (byte) data;
    }

    public void setAF(short data) {
        a = (byte) (data >> 8);
        f = (byte) data;
    }

    boolean getCC(int id) throws Exception {
        return switch (id) {
            case 0 -> !getFz();
            case 1 -> getFz();
            case 2 -> !getFc();
            case 3 -> getFc();
            default -> throw new Exception("Invalid CC id: " + id);
        };
    }



    public int decodeOP(OP op) throws Exception {
        return switch (op.getX()) {
            case 0 -> switch (op.getZ()) {
                case 0 -> switch (op.getY()) {
                    case 0 -> opNOP();
                    case 1 -> opLDnnsp();
                    case 3 -> opJR();
                    case 4, 5, 6, 7 -> opJRc(op);
                    default -> throw new Exception("Unimplemented op: " + op);
                };
                case 1 -> {
                    if (op.getQ() == 0) yield opLDnn(op);
                    else yield opADDhl(op);
                }
                case 2 -> opLDi(op);
                case 3 -> {
                    if (op.getQ() == 0) yield opINC16(op);
                    else yield opDEC16(op);
                }
                case 4 -> opINC(op);
                case 5 -> opDEC(op);
                case 6 -> opLDn(op);
                case 7 -> switch (op.getY()) {
                    case 0 -> opRLCA();
                    case 1 -> opRRCA();
                    case 2 -> opRLA();
                    case 3 -> opRRA();
                    case 4 -> opDAA();
                    case 5 -> opCPL();
                    case 6 -> opSCF();
                    case 7 -> opCCF();
                    default -> throw new Exception("Unimplemented op: " + op);
                };
                default -> throw new Exception("Unimplemented op: " + op);
            };
            case 1 -> {
                if (op.getY() == 6 & op.getX() == 6) throw new Exception("Unimplemented op: " + op);
                else yield opLD(op);
            }
            case 2 -> alu8r(op);
            case 3 -> switch (op.getZ()) {
                case 0 -> switch (op.getY()) {
                    case 0,1,2,3 -> opRetcc(op);
                    case 4 -> opLDffa();
                    case 5 -> opADDsp();
                    case 6 -> opLDaff();
                    case 7 -> opLDhl();
                    default -> throw new Exception("Unimplemented op: " + op);
                };
                case 1 -> {
                    if (op.getQ() == 0) yield opPop(op);
                    else yield switch(op.getP()) {
                        case 0 -> opRet();
                        case 1 -> opReti();
                        case 2 -> opJPhl();
                        case 3 -> opLDsp();
                        default -> throw new Exception("Unimplemented op: " + op);
                    };
                }
                case 2 -> switch (op.getY()) {
                    case 0,1,2,3 -> opJPcc(op);
                    case 4 -> opLDffca();
                    case 6 -> opLDaffc();
                    case 5 -> opLDnnia();
                    case 7 -> opLDanni();
                    default -> throw new Exception("Unimplemented op: " + op);
                };
                case 3 -> switch (op.getY()) {
                    case 0 -> opJPnn();
                    case 1 -> opCBprefix();
                    case 6 -> opDI();
                    case 7 -> opEI();
                    default -> throw new Exception("Unimplemented op: " + op);
                };
                case 4 -> opCallccnn(op);
                case 5 -> {
                    if (op.getQ() == 0) yield opPush(op);
                    else {
                        if (op.getP() == 0) yield opCallnn();
                        else throw new Exception("Unimplemented op: " + op);
                    }
                }
                case 6 -> alu8n(op);
                case 7 -> opReset(op);
                default -> throw new Exception("Unimplemented op: " + op);
            };
            default -> throw new Exception("Unimplemented op: " + op);
        };
    }

    private int opNOP() {
        if (disassemble) disassembly = "\tnop";
        return 1;
    }

    private int opLDnnsp() throws MemoryException {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        mem.write(nn, (byte) (sp & 0xff));
        mem.write((short) (nn + 1), (byte) (sp >> 8));
        if (disassemble) disassembly = String.format("\tld\t(0x%04x), sp", nn);
        return 5;
    }

    private int opJR () throws MemoryException {
        byte d = mem.read(pc++);
        pc += d;
        if (disassemble) disassembly = String.format("\tjr\t%d", d);
        return 3;
    }

    private int opJRc (OP op) throws Exception {
        int retval = 2;
        byte d = mem.read(pc++);
        if (getCC(op.getY()-4)) {
            pc += d;
            retval = 3;
        }
        if (disassemble) disassembly = String.format("\tjr\tcc(%d), %d", op.getY() - 4, d);
        return retval;
    }

    private int opLDnn(OP op) throws Exception {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        setRP(op.getP(), nn);
        if (disassemble) disassembly = String.format("\tld\trp(%d), 0x%04x", op.getP(), nn);
        return 3;
    }

    private int opADDhl(OP op) throws Exception {
        int ss = op.getP();
        short val = getRP(ss);
        byte valL = (byte) (val&0xff);
        byte valH = (byte) (val>>8);

        byte resL = (byte) (valL + l);
        int cyL = ((((int) valL & 0xff) + ((int) l & 0xff)) > 0xff) ? 1 : 0;
        byte resH = (byte) (valH + h + cyL);

        setFn(false);
        setFh(((valH ^ h ^ resH) & 0x10) != 0);
        setFc((((int) valH & 0xff) + ((int) h & 0xff) + cyL) > 0xff);
        l = resL;
        h = resH;
        if (disassemble) disassembly = String.format("\tadd\thl, rp(%d)", ss);
        return 2;
    }

    private int opADDsp() throws Exception {
        byte val = mem.read(pc++);
        byte spL = (byte) (sp&0xff);
        byte spH = (byte) (sp>>8);

        byte resL = (byte) (spL + val);
        int cyL = ((((int) spL & 0xff) + ((int) val & 0xff)) > 0xff) ? 1 : 0;
        byte resH = (byte) (spH + cyL);

        setFn(false);
        setFz(false);
        setFh(((spH ^ resH) & 0x10) != 0);
        setFc((((int) spH & 0xff) + cyL) > 0xff);
        sp = word(resH, resL);
        if (disassemble) disassembly = String.format("\tadd\tsp, 0x%02x", val);
        return 4;
    }

    private int opLDi(OP op) throws MemoryException {
        if (op.getQ() == 0) {
            switch (op.getP()) {
                case 0 -> mem.write(word(b,c), a);
                case 1 -> mem.write(word(d,e), a);
                case 2 -> { mem.write(word(h,l), a); incHL(); }
                case 3 -> { mem.write(word(h,l), a); decHL(); }
            }
            if (disassemble) disassembly = String.format("\tld\t[rp3(%d)], 0x%02x", op.getP(), a);
        } else {
            switch (op.getP()) {
                case 0 -> a = mem.read(word(b,c));
                case 1 -> a = mem.read(word(d,e));
                case 2 -> { a = mem.read(word(h,l)); incHL(); }
                case 3 -> { a = mem.read(word(h,l)); decHL(); }
            }
            if (disassemble) disassembly = String.format("\tld\t0x%02x, [rp3(%d)]", a, op.getP());
        }
        return 2;
    }

    private void incHL() {
        if (++l == 0) h++;
    }

    private void decHL() {
        if (--l == (byte)0xff) h--;
    }

    private int opINC16(OP op) throws Exception {
        int id = op.getP();
        setRP(id, (short)(getRP(id) + 1));
        if (disassemble) disassembly = String.format("\tinc\trp(%d)", id);
        return 2;
    }

    private int opDEC16(OP op) throws Exception {
        int id = op.getP();
        setRP(id, (short)(getRP(id) - 1));
        if (disassemble) disassembly = String.format("\tdec\tr(%d)", id);
        return 2;
    }

    private int opINC(OP op) throws Exception {
        int id = op.getY();
        byte r = getR(id);
        byte res = (byte)(r + 1);
        setR(id, res);
        setFz(res == 0);
        setFn(false);
        setFh(((r ^ res) & 0x10) != 0);
        if (disassemble) disassembly = String.format("\tinc\tr(%d)", id);
        return 1;
    }

    private int opDEC(OP op) throws Exception {
        int id = op.getY();
        byte r = getR(id);
        byte res = (byte)(r - 1);
        setR(id, res);
        setFz(res == 0);
        setFn(true);
        setFh((r&0x0f) == 0);
        if (disassemble) disassembly = String.format("\tdec\tr(%d)", id);
        return 1;
    }

    private int opLDn(OP op) throws Exception {
        byte n = mem.read(pc++);
        setR(op.getY(), n);
        if (disassemble) disassembly = String.format("\tld\tr(%d), 0x%02x", op.getY(), n);
        return 2;
    }

    private int opRLCA() {
        boolean cy = (a&0x80) != 0;
        a = (byte) ((a << 1) | (cy ? 1 : 0));
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(false);
        if (disassemble) disassembly = "\trlca";
        return 1;
    }

    private int opRLA() {
        boolean cy = (a&0x80) != 0;
        a = (byte) ((a << 1) | (getFc() ? 1 : 0));
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(false);
        if (disassemble) disassembly = "\trla";
        return 1;
    }

    private int opRRCA() {
        boolean cy = (a&0x01) != 0;
        a = (byte) (((a>>1)&0x7f) | (cy ? 0x80 : 0x00));
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(false);
        if (disassemble) disassembly = "\trrca";
        return 1;
    }

    private int opRRA() {
        boolean cy = (a&0x01) != 0;
        a = (byte) (((a>>1)&0x7f) | (getFc() ? 0x80 : 0x00));
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(false);
        if (disassemble) disassembly = "\trra";
        return 1;
    }

    private int opDAA() {
        if (getFn()) {
            if (getFc()) a -= 0x60;
            if (getFh()) a -= 0x06;
        } else {
            if (getFc() || a > 0x99) {
                a += 0x60;
                setFc(true);
            }
            if (getFh() || (a&0x0f) > 0x09) a += 0x06;
        }
        setFz(a == 0);
        setFh(false);
        if (disassemble) disassembly = "\tdaa";
        return 1;
    }

    private int opCPL() {
        a = (byte) ~a;
        setFh(true);
        setFn(true);
        if (disassemble) disassembly = "\tcpl";
        return 1;
    }

    private int opSCF() {
        setFc(true);
        setFh(false);
        setFn(false);
        if (disassemble) disassembly = "\tscf";
        return 1;
    }

    private int opCCF() {
        setFc(!getFc());
        setFh(false);
        setFn(false);
        if (disassemble) disassembly = "\tccf";
        return 1;
    }

    private int opLD(OP op) throws Exception {
        byte val = getR(op.getZ());
        setR(op.getY(), val);
        if (disassemble) disassembly = String.format("\tld\tr(%d), r(%d)", op.getY(), op.getZ());
        return 1;
    }

    private int alu8r(OP op) throws Exception {
        byte operand = getR(op.getZ());
        alu8(op, operand);
        if (disassemble) disassembly += String.format(", r(%d)", op.getZ());
        return op.getZ() == 6 ? 2 : 1;
    }

    private int alu8n(OP op) throws Exception {
        byte operand = mem.read(pc++);
        alu8(op, operand);
        if (disassemble) disassembly += String.format(", 0x%02x", operand);
        return 2;
    }

    private void alu8(OP op, byte operand) throws Exception {
        byte res = 0;
        switch (op.getY()) {
            case 0 -> { // add
                res = (byte) (a + operand);
                setFz(res == 0);
                setFn(false);
                setFh(((a ^ operand ^ res) & 0x10) != 0);
                setFc((((int) a & 0xff) + ((int) operand & 0xff)) > 0xff);
                if (disassemble) disassembly = "\tadd\tA";
            }
            case 1 -> { // adc
                int cy = getFc() ? 1 : 0;
                res = (byte) (a + operand + cy);
                setFz(res == 0);
                setFn(false);
                setFh(((a ^ operand ^ res) & 0x10) != 0);
                setFc((((int) a & 0xff) + ((int) operand & 0xff) + cy) > 0xff);
                if (disassemble) disassembly = "\tadc\tA";
            }
            case 2 -> { // sub
                res = (byte) (a - operand);
                setFz(res == 0);
                setFn(true);
                setFh((operand & 0xf) > (a & 0xf));
                setFc(((int) operand & 0xff) > ((int) a & 0xff));
                if (disassemble) disassembly = "\tsub\tA";
            }
            case 3 -> { // sbc
                int cy = getFc() ? 1 : 0;
                res = (byte) (a - operand - cy);
                setFz(res == 0);
                setFn(true);
                setFh(((operand + cy) & 0xf) > (a & 0xf));
                setFc(((int) operand & 0xff) + cy > ((int) a & 0xff));
                if (disassemble) disassembly = "\tsbc\tA";
            }
            case 4 -> { // and
                res = (byte) (a & operand);
                setFz(res == 0);
                setFn(false);
                setFh(true);
                setFc(false);
                if (disassemble) disassembly = "\tand\tA";
            }
            case 5 -> { // xor
                res = (byte) (a ^ operand);
                setFz(res == 0);
                setFn(false);
                setFh(false);
                setFc(false);
                if (disassemble) disassembly = "\txor\tA";
            }
            case 6 -> { // or
                res = (byte) (a | operand);
                setFz(res == 0);
                setFn(false);
                setFh(true);
                setFc(false);
                if (disassemble) disassembly = "\tor\tA";
            }
            case 7 -> { // cp
                res = a;
                byte temp = (byte) (a - operand);
                setFz(temp == 0);
                setFn(true);
                setFh((operand & 0xf) > (a & 0xf));
                setFc(((int) operand & 0xff) > ((int) a & 0xff));
                if (disassemble) disassembly = "\tcp\tA";
            }
            default -> throw new Exception("Unimplemented op: " + op);
        }
        a = res;
    }

    private int opLDffa () throws MemoryException {
        byte n = mem.read(pc++);
        short addr = (short) (0xff00 + ((int)n&0xff));
        mem.write(addr, a);
        if (disassemble) disassembly = String.format("\tldh\t0x%02x, A", n);
        return 3;
    }

    private int opLDaff () throws MemoryException {
        byte n = mem.read(pc++);
        short addr = (short) (0xff00 + ((int)n&0xff));
        a = mem.read(addr);
        if (disassemble) disassembly = String.format("\tldh\tA, 0x%02x", n);
        return 3;
    }

    private int opLDhl() throws MemoryException {
        byte val = mem.read(pc++);
        byte spL = (byte) (sp & 0xff);
        byte spH = (byte) (sp >> 8);
        byte resL, resH;

        if (val > 0) {
            resL = (byte) (spL + val);
            int cyL = ((((int) spL & 0xff) + ((int) val & 0xff)) > 0xff) ? 1 : 0;
            resH = (byte) (spH + cyL);
            setFh(((spH ^ resH) & 0x10) != 0);
            setFc((((int) spH & 0xff) + cyL) > 0xff);
        } else {
            int ival = -val;
            resL = (byte) (spL - ival);
            int cyL = (ival > ((int) spL & 0xff)) ? 1 : 0;
            resH = (byte) (spH - cyL);
            setFh(cyL > (spH & 0xf));
            setFc(cyL > (spH & 0xff));
        }
        setFn(false);
        setFz(false);
        l = resL;
        h = resH;

        if (disassemble) disassembly = String.format("\tldhl\tsp, %d", val);
        return 3;
    }

    private int opJPcc(OP op) throws Exception {
        int retval = 3;
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        boolean cc = getCC(op.getY());
        if (cc) {
            pc = nn;
            retval = 4;
        }
        if (disassemble) disassembly = String.format("\tjp\tcc(%d), 0x%04x", op.getY(), nn);
        return retval;
    }

    private int opLDffca() throws MemoryException {
        short addr = (short) (0xff00 + ((int)c&0xff));
        mem.write(addr, a);
        if (disassemble) disassembly = String.format("\tldh\t(0xff00+c), A");
        return 2;
    }

    private int opLDaffc () throws MemoryException {
        short addr = (short) (0xff00 + ((int)c&0xff));
        a = mem.read(addr);
        if (disassemble) disassembly = String.format("\tldh\tA, (0xff00+c)");
        return 2;
    }

    private int opLDnnia() throws MemoryException {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        mem.write(nn, a);
        if (disassemble) disassembly = String.format("\tld\t(0x%04x), a", nn);
        return 4;
    }

    private int opLDanni() throws MemoryException {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        a = mem.read(nn);
        if (disassemble) disassembly = String.format("\tld\ta, (0x%04x)", nn);
        return 4;
    }

    private int opJPnn() throws MemoryException {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        short nn = word(high, low);
        pc = nn;
        if (disassemble) disassembly = String.format("\tjp\t0x%04x", nn);
        return 4;
    }

    private int opDI () {
        ime = false;
        if (disassemble) disassembly = "\tdi";
        return 1;
    }

    private int opEI () {
        ime = true;
        if (disassemble) disassembly = "\tei";
        return 1;
    }

    private int opPop(OP op) throws MemoryException {
        switch (op.getP()) {
            case 0 -> {
                c = mem.read(sp++);
                b = mem.read(sp++);
            }
            case 1 -> {
                e = mem.read(sp++);
                d = mem.read(sp++);
            }
            case 2 -> {
                l = mem.read(sp++);
                h = mem.read(sp++);
            }
            case 3 -> {
                f = mem.read(sp++);
                a = mem.read(sp++);
            }
        }
        if (disassemble) disassembly = String.format("\tpop\trp2(%d)", op.getP());
        return 3;
    }

    private int opPush(OP op) throws MemoryException {
        switch (op.getP()) {
            case 0 -> {
                mem.write(--sp, b);
                mem.write(--sp, c);
            }
            case 1 -> {
                mem.write(--sp, d);
                mem.write(--sp, e);
            }
            case 2 -> {
                mem.write(--sp, h);
                mem.write(--sp, l);
            }
            case 3 -> {
                mem.write(--sp, a);
                mem.write(--sp, f);
            }
        }
        if (disassemble) disassembly = String.format("\tpush\trp2(%d)", op.getP());
        return 4;
    }

    private int opCallnn() throws MemoryException {
        byte low = mem.read(pc++);
        byte high = mem.read(pc++);
        mem.write(--sp, (byte) ((pc>>8)&0xff));
        mem.write(--sp, (byte) (pc&0xff));
        pc = word(high, low);
        if (disassemble) disassembly = String.format("\tcall\t0x%04x", pc);
        return 6;
    }

    private int opCallccnn(OP op) throws Exception {
        int retval = 3;
        boolean cc = getCC(op.getY());
        if (cc) retval = opCallnn();
        if (disassemble) disassembly = String.format("\tcall\tcc(%d), 0x%04x", op.getY(), pc);
        return retval;
    }

    private int opRet() throws MemoryException {
        byte lowpc = mem.read(sp++);
        byte highpc = mem.read(sp++);
        pc = word(highpc, lowpc);
        if (disassemble) disassembly = "\tret";
        return 4;
    }

    private int opReti() throws MemoryException {
        byte lowpc = mem.read(sp++);
        byte highpc = mem.read(sp++);
        pc = word(highpc, lowpc);
        ime = true;
        if (disassemble) disassembly = "\treti";
        return 4;
    }

    private int opJPhl() throws MemoryException {
        short hl = word(h, l);
        pc = hl;
        if (disassemble) disassembly = "\tjp\thl";
        return 1;
    }

    private int opLDsp() {
        short hl = word(h, l);
        sp = hl;
        if (disassemble) disassembly = "\tld\tsp, hl";
        return 2;
    }

    private int opRetcc(OP op) throws Exception {
        boolean cc = getCC(op.getY());
        int retval = 2;
        if (cc) {
            byte lowpc = mem.read(sp++);
            byte highpc = mem.read(sp++);
            pc = word(highpc, lowpc);
            retval = 5;
        }
        if (disassemble) disassembly = String.format("\tret\tcc(%d)", op.getY());
        return retval;
    }

    private int opCBprefix() throws Exception {
        OP op = new OP(mem.read(pc++));
        return switch(op.getX()) {
            case 0 -> switch(op.getY()) {
                case 0 -> opRLC(op);
                case 1 -> opRL(op);
                case 2 -> opRRC(op);
                case 3 -> opRR(op);
                case 4 -> opSLA(op);
                case 5 -> opSRA(op);
                case 6 -> opSWAP(op);
                case 7 -> opSRL(op);
                default -> throw new Exception("Unimplemented CB-prefixed op: " + op);
            };
            case 1 -> opBIT(op);
            case 2 -> opRES(op);
            case 3 -> opSET(op);
            default -> throw new Exception("Unimplemented CB-prefixed op: " + op);
        };
    }

    private int opRLC(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (a&0x80) != 0;
        byte res = (byte) ((val << 1) | (cy ? 1 : 0));
        setR(r, res);
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(res == 0);
        if (disassemble) disassembly = String.format("\trlc\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opRL(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (a&0x80) != 0;
        byte res = (byte) ((val << 1) | (getFc() ? 1 : 0));
        setR(r, res);
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(res == 0);
        if (disassemble) disassembly = String.format("\trl\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opRRC(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (a&0x01) != 0;
        byte res = (byte) (((val >> 1)&0x7f) | (cy ? 0x80 : 0x00));
        setR(r, res);
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(res == 0);
        if (disassemble) disassembly = String.format("\trrc\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opRR(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (a&0x01) != 0;
        byte res = (byte) (((val >> 1)&0x7f) | (getFc() ? 0x80 : 0x00));
        setR(r, res);
        setFc(cy);
        setFh(false);
        setFn(false);
        setFz(res == 0);
        if (disassemble) disassembly = String.format("\trr\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opSLA(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (val&0x80) != 0;
        byte res = (byte) (val<<1);
        setR(r, res);
        setFz(res == 0);
        setFn(false);
        setFh(false);
        setFc(cy);
        if (disassemble) disassembly = String.format("\tsla\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opSRA(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (val&0x01) != 0;
        byte res = (byte) (val>>1);
        setR(r, res);
        setFz(res == 0);
        setFn(false);
        setFh(false);
        setFc(cy);
        if (disassemble) disassembly = String.format("\tsra\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opSRL(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        boolean cy = (val&0x01) != 0;
        byte res = (byte) ((val>>1)&0x7f);
        setR(r, res);
        setFz(res == 0);
        setFn(false);
        setFh(false);
        setFc(cy);
        if (disassemble) disassembly = String.format("\tsrl\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opSWAP(OP op) throws Exception {
        int r = op.getZ();
        byte val = getR(r);
        byte res = (byte) (((val>>4)&0x0f) | ((val<<4)&0xf0));
        setR(r, res);
        setFz(res == 0);
        setFn(false);
        setFh(false);
        setFc(false);
        if (disassemble) disassembly = String.format("\tswap\tr(%d)", r);
        return r == 6 ? 4 : 2;
    }

    private int opBIT(OP op) throws Exception {
        int r = op.getZ();
        int b = op.getY();
        byte val = getR(r);
        boolean rb = (val & (1<<b)) != 0;
        setFh(true);
        setFn(false);
        setFz(!rb);
        if (disassemble) disassembly = String.format("\tbit\t%d, r(%d)", b, r);
        return r == 6 ? 3 : 2;
    }

    private int opSET(OP op) throws Exception {
        int r = op.getZ();
        int b = op.getY();
        byte val = (byte) (getR(r) | (1<<b));
        setR(r, val);
        if (disassemble) disassembly = String.format("\tset\t%d, r(%d)", b, r);
        return r == 6 ? 4 : 2;
    }

    private int opRES(OP op) throws Exception {
        int r = op.getZ();
        int b = op.getY();
        byte val = (byte) (getR(r) & ~(1<<b));
        setR(r, val);
        if (disassemble) disassembly = String.format("\tres\t%d, r(%d)", b, r);
        return r == 6 ? 4 : 2;
    }

    private int opReset(OP op) throws MemoryException {
        mem.write(--sp, (byte) ((pc>>8)&0xff));
        mem.write(--sp, (byte) (pc&0xff));
        pc = (short) (op.getY() * 8);
        if (disassemble) disassembly = String.format("\trst\t0x%04x", pc);
        return 4;
    }

    public boolean getFz() {
        return ((f >> 7)&1)==1;
    }

    public boolean getFn() {
        return ((f >> 6)&1)==1;
    }

    public boolean getFh() {
        return ((f >> 5)&1)==1;
    }

    public boolean getFc() {
        return ((f >> 4)&1)==1;
    }

    public void setFz(boolean fz) {
        if (fz) f |= 0b1000_0000;
        else f &= 0b0111_0000;
    }

    public void setFn(boolean fn) {
        if (fn) f |= 0b0100_0000;
        else f &= 0b1011_0000;
    }

    public void setFh(boolean fh) {
        if (fh) f |= 0b0010_0000;
        else f &= 0b1101_0000;
    }

    public void setFc(boolean fc) {
        if (fc) f |= 0b0001_0000;
        else f &= 0b1110_0000;
    }

    public abstract void reset();
}
