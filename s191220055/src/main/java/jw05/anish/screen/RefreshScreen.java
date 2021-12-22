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
            if(state == 1){ //正在进行单人游戏
                mainWindow.repaint();
            }
            else if((state == 2 || state == 3) && screen.getThreadPool() != null){ //单人模式游戏结束，且不是demo模式
                screen.getThreadPool().shutdown(); //关闭开启的线程
                screen.gameOverScreen();
                mainWindow.repaint();
                break;
            }
            else if(state == 4){ //正在播放demo
                mainWindow.repaint();
            }
            else if(state == 5){ //demo播放完毕
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
        System.out.println("stop refreshing");
    }
}
