package jw05.anish.screen;

import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;

import jw05.asciiPanel.AsciiPanel;

public interface Screen {

    public void displayOutput(AsciiPanel terminal);

    public Screen respondToUserInput(KeyEvent key);

    public Screen releaseKey();

    public void rulesScreen();

    public void standAloneGameScreen();

    public void standAloneGameOverScreen();

    public void onlineGameWinScreen();

    public void onlineGameLostScreen();

    public int getScreenState();

    public void demoScreen();
    
    public ExecutorService getThreadPool();

    //线上部分

    public void onlineGameScreen();
    
}
