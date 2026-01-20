package Server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class GameMap {
    private int[][] _map;
    private boolean _cyclicFlag;

    // קבועים לצבעים/סוגי משבצות
    public static final int WALL = 0;
    public static final int DOT = 1;
    public static final int EMPTY = 2; // או ערך אחר המייצג ריק

    public GameMap(int[][] map) {
        this._map = new int[map.length][map[0].length];
        for (int i = 0; i < map.length; i++) {
            System.arraycopy(map[i], 0, this._map[i], 0, map[i].length);
        }
        this._cyclicFlag = true;
    }

    public GameMap(int w, int h, int val) {
        this._map = new int[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                this._map[i][j] = val;
            }
        }
        this._cyclicFlag = true;
    }

    public int getWidth() { return _map.length; }
    public int getHeight() { return _map[0].length; }

    public int getPixel(int x, int y) { return _map[x][y]; }
    public int getPixel(PointInt2D p) { return getPixel(p.getX(), p.getY()); }

    public void setPixel(int x, int y, int v) { _map[x][y] = v; }
    public void setPixel(PointInt2D p, int v) { setPixel(p.getX(), p.getY(), v); }

    public boolean isCyclic() { return _cyclicFlag; }
    public void setCyclic(boolean cy) { _cyclicFlag = cy; }

    // בדיקה אם נקודה נמצאת בתוך גבולות המפה (רלוונטי למפה לא מעגלית)
    public boolean isInside(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }

    /**
     * מחשב את המרחק הקצר ביותר בין שתי נקודות (BFS)
     */
    public int shortestPathDist(PointInt2D start, PointInt2D end) {
        if (start.equals(end)) return 0;
        int[][] distMap = new int[getWidth()][getHeight()];
        for(int i=0; i<getWidth(); i++) for(int j=0; j<getHeight(); j++) distMap[i][j] = -1;

        Queue<PointInt2D> q = new LinkedList<>();
        q.add(start);
        distMap[start.getX()][start.getY()] = 0;

        while(!q.isEmpty()) {
            PointInt2D curr = q.poll();
            if (curr.equals(end)) return distMap[curr.getX()][curr.getY()];

            // בדיקת 4 הכיוונים
            int[] dx = {1, -1, 0, 0};
            int[] dy = {0, 0, 1, -1};

            for (int k = 0; k < 4; k++) {
                int nx = curr.getX() + dx[k];
                int ny = curr.getY() + dy[k];

                if (_cyclicFlag) {
                    nx = (nx + getWidth()) % getWidth();
                    ny = (ny + getHeight()) % getHeight();
                }

                if (isInside(nx, ny)) {
                    // אם זה לא קיר ועדיין לא ביקרנו שם
                    if (_map[nx][ny] != WALL && distMap[nx][ny] == -1) {
                        distMap[nx][ny] = distMap[curr.getX()][curr.getY()] + 1;
                        q.add(new PointInt2D(nx, ny));
                    }
                }
            }
        }
        return -1; // אין מסלול
    }
}