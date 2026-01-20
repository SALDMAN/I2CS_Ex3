package Server;

import exe.ex3.game.GhostCL; // <--- חייב להיות זה! לא Server.GhostCL

public interface PacmanGame {
    /** Returns the visual representation of the ghosts (for client/algo) */
    public GhostCL[] getGhosts(int code);

    public String getMapData();
    public int[][] getGame(int code);
    public String getPos(int code);
    public int getStatus();
    public void move(int dir);
    public void play();
    public void end(int i);
    public Character getKeyChar();
    public void init(int level, String id, boolean cyclic, long seed, double resolution, int dt, int timeout);

    // Constants
    public static final int RUNNING = 0;
    public static final int PAUSED = 1;
    public static final int DONE = 2;

    public static final int UP = 1;
    public static final int RIGHT = 2;
    public static final int DOWN = 3;
    public static final int LEFT = 4;
}