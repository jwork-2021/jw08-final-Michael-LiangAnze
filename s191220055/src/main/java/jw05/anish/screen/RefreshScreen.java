package jw05.anish.screen;

import javax.swing.JFrame;

public class RefreshScreen implements Runnable {
    private JFrame mainWindow;
    private Screen screen;

    public RefreshScreen(JFrame mainWindow,Screen screen) {
        this.mainWindow = mainWindow;
        this.screen = screen;
    }

    @Override
    public void run() {
        while (true) {
            if(screen.getScreenState() > 1){ //游戏结束
                screen.getThreadPool().shutdown(); //关闭开启的线程
                screen.gameOverScreen();
                mainWindow.repaint();
                break;
            }
            else{
                mainWindow.repaint();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
