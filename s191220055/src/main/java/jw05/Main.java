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
        String[]myargs = {"-demo","demo-1640144277465.txt"};//
        terminal = new AsciiPanel(World.WIDTH, World.HEIGHT, AsciiFont.Guybrush_square_16x16); 
        add(terminal);
        pack();
        if(myargs.length == 0){//没有参数，默认单人游戏
            System.out.println("Standalone game");
            screen = new WorldScreen(false,false,null);
            screen.rulesScreen();
        }
        // else if(myargs.length == 1){
        //     if(myargs[0].equals("-record")){
        //         System.out.println("Recording demo");
        //         screen = new WorldScreen(false,true,myargs[1]);
        //     }
        // }
        else if(myargs.length == 2){ //播放demo模式，或者在线模式
            if(myargs[0].equals("-demo")){
                System.out.println("Replaying demo");
                screen = new WorldScreen(false,true,myargs[1]);
            }
            else if(args[0].equals("-online")){
                System.out.println("Online game,args:"+args[0]+" "+args[1]);
                screen = new WorldScreen(true,false,args[1]);
                screen.rulesScreen();
            }
            else{
                System.out.println("Wrong arguments!");
                System.exit(-1);
            }
        }
        else{
            System.out.println("Wrong arguments!");
            System.exit(-1);
        }
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
