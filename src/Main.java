import com.eb.awtcli.DebugDesktop;
import com.eb.awtcli.EmuDesktop;
import com.eb.hardware.HardwareSeq;

import java.awt.image.BufferedImage;

import static java.lang.Thread.sleep;

public class Main {
    public static void main(String[] args) throws Exception {
        EmuDesktop cli = new EmuDesktop();
        HardwareSeq hw = new HardwareSeq("tetris.gb");
        DebugDesktop dbg = new DebugDesktop(hw);

        cli.image = hw.getFrameBuffer();

        while(true) {
            if (cli.isRunning()) {
                hw.frame();
                //cli.image = (BufferedImage) hw.cloneFrameBuffer();
                cli.imagePanel.revalidate();
                cli.imagePanel.repaint();

                dbg.update();
            }


            try {
                sleep(160);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
/*
        Thread tCli = new Thread(() -> { cli.launch(hw); });
        Thread tHardware = new Thread(() -> {
            try {
                hw.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        tCli.start();
        tHardware.start();

        try {
            tCli.join();
            tHardware.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello world!");*/
    }
}