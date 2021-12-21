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
        int state = 0;
        while (true) {
            state = screen.getScreenState();
            if(state == 5){ //播放demo完毕，4为正在播放
                break;
            }
            else if(state == 2){ //单人模式游戏结束
                screen.getThreadPool().shutdown(); //关闭开启的线程
                screen.gameOverScreen();
                mainWindow.repaint();
                break;
            }
            else{
                // System.out.println("refreshing");
                mainWindow.repaint();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("stop refreshing");
    }
}
