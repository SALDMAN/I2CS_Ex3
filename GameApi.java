package Server;
import Add_Ons.GhostView;
import Add_Ons.Dir;

import java.util.List;

public interface GameApi {

    int[][] getBoard();
    int getWidth();
    int getHeight();

    Pixel2D getPacman();
    List<GhostView> getGhosts();

    int getScore();
    int getLives();
    boolean isGreenMode();

    void setNextDir(Dir d);
    void update(double dt);
}
