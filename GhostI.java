package Server;


public interface GhostI {
    int INIT = 0;
    int PLAY = 1;
    int PAUSE = 2;
    int RANDOM_WALK0 = 10;
    int RANDOM_WALK1 = 11;
    int GREEDY_SP = 12;
    int START_X = 9;
    int START_Y = 10;
    int IN_CAGE = 0;
    int OUT_CAGE = 1;

    int getType();

    String getPos(int code);

    String getInfo();

    double remainTimeAsEatable(int code);

    int getStatus();

    int getX();

    int getY();

    void setPos(int x, int y);

    void setPos(Pixel2D p);

    String getPic();

    int getID();

    int getOldValue();

    void eatable();

    void setPic(String cat);
}