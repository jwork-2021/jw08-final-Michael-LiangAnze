package jw05.anish.calabashbros;

import java.awt.Color;
import jw05.anish.screen.ScreenInfo;
import jw05.anish.algorithm.Tuple;

public class World {

    public static final int WIDTH = 40;
    public static final int HEIGHT = WIDTH + 4;
    public final int hpStartX = 4;
    public final int hpStartY = WIDTH;
    public final int scoreStartX = hpStartX + 17;
    public final int scoreStartY = hpStartY;
    public final int rulesStartX = 3;
    public final int rulesStartY = 2;
    public final int pressEnterX = 9;
    public final int pressEnterY = 20;
    public final int gameOverInfoX = 15;
    public final int gameOverInfoY = 20;
    private Tile<Thing>[][] tiles;
    private int worldState;

    public void setWorldState(int s) {
        worldState = s;
    }

    public int getWorldState() {
        return worldState;
    }

    @SuppressWarnings(value = "all")
    public World() {
        if (tiles == null) {
            tiles = new Tile[WIDTH][HEIGHT];
        }
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                tiles[i][j] = new Tile<>(i, j);
            }
        }
    }

    public void setRulesWorld() { // 开始界面
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                tiles[i][j].setThing(new ScreenInfo(this, new Color(0, 0, 0), 0));
            }
        }
        String[] rules = { "RULES:",
                "  -PRESS WASD TO MOVE AND SPACE",
                "   TO SHOOT",
                "  -KILL ALL MONSTERS TO WIN",
                "  -WHEN MONSTER'S COLOR BECAME",
                "   BRIGHTER,YOU ARE SPOTTED",
                "  -PICK UP PROPS AND GET REWARDS",
        };
        int lineNum = 0;
        for (String line : rules) {
            for (int i = 0; i < line.length(); ++i) {
                tiles[rulesStartX + i][rulesStartY + lineNum]
                        .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) line.charAt(i)));
            }
            lineNum += 2;
        }

        String temp = "PRESS ENTER TO START";
        for (int i = 0; i < temp.length(); ++i) {
            tiles[pressEnterX + i][pressEnterY]
                    .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) temp.charAt(i)));
        }
    }

    public void setGamingWorld() { // 游戏正式开始
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                tiles[i][j].setThing(new Floor(this));
            }
        }

    }

    public void setGameOverWorld() {
        String info1 = " YOU WIN ";
        String info2 = "GAME OVER";
        for (int i = gameOverInfoX - 1; i <= gameOverInfoX + 9; i++) {
            for (int j = gameOverInfoY - 1; j < gameOverInfoY + 2; j++) {
                tiles[i][j].setThing(new ScreenInfo(this, new Color(0, 0, 0), 0));
            }
        }
        if (getWorldState() == 2) {
            for (int i = 0; i < info1.length(); ++i) {
                tiles[gameOverInfoX + i][gameOverInfoY]
                        .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) info1.charAt(i)));
            }
        } else {
            for (int i = 0; i < info2.length(); ++i) {
                tiles[gameOverInfoX + i][gameOverInfoY]
                        .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) info2.charAt(i)));
            }
        }

    }

    private int transformX(int xPos) {
        return xPos;
    }

    private int transformY(int YPos) {
        return YPos + WIDTH;
    }

    public void setPlayerInfo(int hp, int score) {
        tiles[transformX(hpStartX)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'H'));
        tiles[transformX(hpStartX + 1)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'P'));
        tiles[transformX(hpStartX + 2)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) ':'));
        tiles[transformX(scoreStartX)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'S'));
        tiles[transformX(scoreStartX + 1)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'C'));
        tiles[transformX(scoreStartX + 2)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'O'));
        tiles[transformX(scoreStartX + 3)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'R'));
        tiles[transformX(scoreStartX + 4)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) 'E'));
        tiles[transformX(scoreStartX + 5)][transformY(0)]
                .setThing(new ScreenInfo(this, new Color(255, 255, 255), (int) ':'));
        updateInfo(hp, score);
    }

    private void updateInfo(int hp, int score) {
        for (int i = hpStartX + 3; i < scoreStartX; ++i) {
            tiles[transformX(i)][transformY(0)].setThing(new ScreenInfo(this, new Color(0, 0, 0), 0));
        }
        for (int i = scoreStartX + 6; i < WIDTH; ++i) {
            tiles[transformX(i)][transformY(0)].setThing(new ScreenInfo(this, new Color(0, 0, 0), 0));
        }
        for (int i = 0; i < hp && i < scoreStartX; ++i) {
            tiles[transformX(hpStartX + 3 + i)][transformY(0)]
                    .setThing(new ScreenInfo(this, new Color(255, 0, 0), 3));
        }
        int num1 = score % 10;
        int num2 = score / 10;
        if (score == 0) {
            tiles[transformX(scoreStartX + 7)][transformY(0)]
                    .setThing(new ScreenInfo(this, new Color(30, 30, 240), (int) '0'));
        } else {
            if (num1 != 0) {
                tiles[transformX(scoreStartX + 7)][transformY(0)]
                        .setThing(new ScreenInfo(this, new Color(30, 30, 240), (int) '0' + num1));
            }
            if (num2 != 0) {
                tiles[transformX(scoreStartX + 6)][transformY(0)]
                        .setThing(new ScreenInfo(this, new Color(30, 30, 240), (int) '0' + num2));
            }
        }

    }

    public int getWorldSize() {
        return Math.min(WIDTH, HEIGHT);
    }

    public Thing get(int x, int y) {
        return this.tiles[x][y].getThing();
    }

    public void put(Thing t, Tuple<Integer, Integer> pos) {
        this.tiles[pos.first][pos.second].setThing(t);
    }

    public void swapPos(Tuple<Integer, Integer> p1, Tuple<Integer, Integer> p2) {
        Thing t1 = get(p1.first, p1.second);
        Thing t2 = get(p2.first, p2.second);
        put(t2, p1);
        put(t1, p2);
    }
}
