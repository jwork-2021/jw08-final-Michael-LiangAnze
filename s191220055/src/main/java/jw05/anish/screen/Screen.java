package jw05.anish.screen;

import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;

import jw05.asciiPanel.AsciiPanel;

public interface Screen {

    public void displayOutput(AsciiPanel terminal);

    public Screen respondToUserInput(KeyEvent key);

    public Screen releaseKey();

    public void rulesScreen();

    public void gamingScreen();

    public void gameOverScreen();

    public int getScreenState();

    public ExecutorService getThreadPool();
    
}
