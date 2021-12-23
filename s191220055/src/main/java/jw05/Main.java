package jw05;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import jw05.anish.calabashbros.World;
import jw05.anish.screen.Screen;
import jw05.anish.screen.WorldScreen;

import jw05.asciiPanel.AsciiFont;
import jw05.asciiPanel.AsciiPanel;
import jw05.anish.screen.RefreshScreen;

public class Main extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private Screen screen;

    public Main(String[] args) {
        super();
        //for debug
        // String[]myargs = {};
        // String[]myargs = {"-record"};
        // String[]myargs = {"-demo","demo-1640168085000.txt"};
        String[]myargs = {"-online","123"};
        // String[]myargs = {"-server"};
        terminal = new AsciiPanel(World.WIDTH, World.HEIGHT, AsciiFont.Guybrush_square_16x16); 
        add(terminal);
        pack();
        screen = new WorldScreen(myargs);
        Thread t = new Thread(new RefreshScreen(this,screen));
        addKeyListener(this);
        t.start();  
    }

    @Override
    public void repaint() {
        terminal.clear();
        screen.displayOutput(terminal);
        super.repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        screen = screen.respondToUserInput(e);
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        screen = screen.releaseKey();
        repaint();
    }

    public static void main(String[] args) {
        Main app = new Main(args);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }

}
