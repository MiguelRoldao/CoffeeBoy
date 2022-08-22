package com.eb.hardware;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// TODO: Emulator runs forever. It's waiting for a VBLANK interrupt.
// TODO: Time to work on the PPU

public class Hardware extends Thread{

    byte[] rom;
    Cartridge cartridge;
    CPU cpu;

    boolean run = true;

    public Hardware() {}

    public Hardware(String path) throws Exception {
        rom = Files.readAllBytes(Paths.get(path));
        cartridge = Cartridge.createCartridgeFromRom(rom);
        cpu = new CPUGB(cartridge);
    }

    public boolean terminate = false;

    public void execute() throws Exception {
        int cycles = 0;
        cpu.disassemble = true;
        while(!terminate) {
            //long start = System.nanoTime();
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

                while (cycles < 114) {
                    if (!run) {
                        try { synchronized (this) { wait(); }}
                        catch (InterruptedException ignored) {}
                    }
                    short pc = cpu.pc;
                    if (pc == 0x0281) {
                        byte hello = 0;
                    }
                    cycles += cpu.step();
                    if (cpu.disassemble) System.out.printf("0x%04x: %s\n", pc, cpu.disassembly);
                }
                cycles -= 114;
            }



            /*while(cycles < 17556) {
                if (!run) {
                    try { synchronized (this) { wait(); }}
                    catch (InterruptedException ignored) {}
                }
                cycles += cpu.step();
                if (cpu.disassemble) System.out.printf("0x%04x: %s\n", cpu.pc, cpu.disassembly);
            }
            cycles -= 17556;*/

            //long end = System.nanoTime();
            //System.out.println("Elapsed Time in nano seconds: " + (end - start));

            // wait() until its time for another frame
            try { synchronized(this) { wait(); }}
            catch (InterruptedException ignored) {}
        }
    }


    public void load(String path) throws Exception {
        synchronized (this) {
            rom = Files.readAllBytes(Paths.get(path));
            cartridge = Cartridge.createCartridgeFromRom(rom);
            cpu = new CPUGB(cartridge);
        }
    }

    public void play() {
        synchronized (this) {
            run = true;
        }
    }

    public void pause() {
        synchronized (this) {
            run = false;
        }
    }

    public void reset() {
        synchronized (this) {
            cartridge.reset();
            cpu.reset();
        }
    }


    public static void main(String[] args) {
        //File game = new File("tetris.gb");
        Hardware hw = new Hardware();
        // prepare thread to run cpu
        Thread tHw = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    hw.execute();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        // load hardware and start cpu thread
        try {
            hw.load("tetris.gb");
            tHw.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        hw.cpu.disassemble = false;

        // prepare scheduler to run cpu thread @ 59.73 Hz
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable clockTask = () -> {
            synchronized (hw) {
                hw.notify();
            }
        };
        int periodicDelay = 16742006;
        scheduler.scheduleAtFixedRate(clockTask, periodicDelay, periodicDelay, TimeUnit.NANOSECONDS);

        // shutdown thread and exit orderly;
        try {
            sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduler.shutdown();
        try {
            synchronized (hw) {
                hw.terminate = true;
                hw.notify();
            }
            tHw.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("Hello world!");
    }
}