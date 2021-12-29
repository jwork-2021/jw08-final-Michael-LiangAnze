package jw05.anish.screen;

import javax.swing.JFrame;

public class RefreshScreen implements Runnable {
    private JFrame mainWindow;
    private Screen screen;

    public RefreshScreen(JFrame mainWindow,Screen screen) {
        this.mainWindow = mainWindow;
        this.screen = screen;
    }

    // 状态0：单人游戏规则界面
    // 状态1：单人游戏界面
    // 状态2：单人游戏胜利界面
    // 状态3：单人游戏失败界面
    // 状态4：播放demo界面
    // 状态5：demo播放完毕界面
    // 状态6：多人游戏等待玩家界面（服务器对应的玩家
    // 状态7：多人游戏等待玩家界面（客户端对应的玩家
    // 状态8：多人游戏对战界面
    // 状态9：多人游戏结束界面
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
                screen.standAloneGameOverScreen();
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
            else if(state == 9){ //多人模式结束，且玩家胜利
                screen.onlineGameWinScreen();
                mainWindow.repaint();
                // break;
            }
            else if(state == 10){
                screen.onlineGameLostScreen();
                mainWindow.repaint();
            }
            else{
                mainWindow.repaint();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("stop refreshing");
    }
}
