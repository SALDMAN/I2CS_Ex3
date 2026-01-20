package Server;

import Add_Ons.Index2D;
import Client.GameInfo;
import exe.ex3.game.GhostCL;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

public class Game implements PacmanGame {

    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int DOT = 2;
    public static final int POWER = 3;

    private int _status = PAUSED; // מתחילים ב-PAUSED
    private int[][] _board;
    private ArrayList<Ghost> _ghosts;
    private Index2D _pacmanPos;
    private int _score = 0;
    private int _lastDir = RIGHT;
    private long _startTime;

    private int rows, cols;
    private boolean guiInit = false;

    public Game() {
        _ghosts = new ArrayList<>();
    }

    @Override
    public void init(int level, String id, boolean cyclic, long seed, double resolution, int dt, int timeout) {
        System.out.println("Initializing Level: " + level);
        _startTime = System.currentTimeMillis();
        loadExactMap();
        initGui();
        draw();
    }

    private void loadExactMap() {
        String[] mapDesign = {
                "############################",
                "#............##............#",
                "#.####.#####.##.#####.####.#",
                "#O####.#####.##.#####.####O#",
                "#.####.#####.##.#####.####.#",
                "#..........................#",
                "#.####.##.########.##.####.#",
                "#.####.##.########.##.####.#",
                "#......##....##....##......#",
                "######.##### ## #####.######",
                "     #.##### ## #####.#     ",
                "     #.##          ##.#     ",
                "     #.## ###  ### ##.#     ",
                "######.## # G  G # ##.######",
                "      .   #  G   #   .      ",
                "######.## #      # ##.######",
                "     #.## ######## ##.#     ",
                "     #.##          ##.#     ",
                "     #.## ######## ##.#     ",
                "######.## ######## ##.######",
                "#............##............#",
                "#.####.#####.##.#####.####.#",
                "#.####.#####.##.#####.####.#",
                "#O..##.......P.......##..O.#",
                "###.##.##.########.##.##.###",
                "###.##.##.########.##.##.###",
                "#......##....##....##......#",
                "#.##########.##.##########.#",
                "#.##########.##.##########.#",
                "#..........................#",
                "############################"
        };

        rows = mapDesign.length;
        cols = mapDesign[0].length();
        _board = new int[cols][rows];

        for (int y = 0; y < rows; y++) {
            String line = mapDesign[rows - 1 - y];
            for (int x = 0; x < cols; x++) {
                char c = (x < line.length()) ? line.charAt(x) : ' ';
                int val = EMPTY;
                if (c == '#') val = WALL;
                else if (c == '.') val = DOT;
                else if (c == 'O') val = POWER;
                _board[x][y] = val;

                if (c == 'P') { _pacmanPos = new Index2D(x, y); _board[x][y] = EMPTY; }
                else if (c == 'G') { _ghosts.add(new Ghost(x, y, _ghosts.size())); _board[x][y] = (y > 10 && y < 20) ? EMPTY : DOT; }
            }
        }
    }

    private void initGui() {
        if (!guiInit) {
            StdDraw.setCanvasSize(cols * 22, (rows + 3) * 22);
            StdDraw.setXscale(0, cols);
            StdDraw.setYscale(0, rows + 3);
            StdDraw.enableDoubleBuffering();
            guiInit = true;
        }
    }

    @Override
    public void move(int dir) {
        // --- תיקון למצב השהייה ---
        if (_status != RUNNING) {
            draw();
            // שינה יזומה של 50ms כדי לא לחנוק את המעבד ולאפשר קליטת מקשים
            try { Thread.sleep(50); } catch (Exception e) {}
            return;
        }
        // -------------------------

        if (dir != -1) _lastDir = dir;
        int nx = _pacmanPos.getX();
        int ny = _pacmanPos.getY();

        if (dir == RIGHT) nx++; else if (dir == LEFT) nx--; else if (dir == UP) ny++; else if (dir == DOWN) ny--;
        if (nx < 0) nx = cols - 1; if (nx >= cols) nx = 0;

        if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && _board[nx][ny] != WALL) {
            _pacmanPos = new Index2D(nx, ny);
            int cell = _board[nx][ny];
            if (cell == DOT) { _score++; _board[nx][ny] = EMPTY; }
            else if (cell == POWER) { _score += 10; _board[nx][ny] = EMPTY; for (Ghost g : _ghosts) g.setEdible(100); }
        }

        for (Ghost g : _ghosts) {
            g.move(_board, _pacmanPos);
            Index2D gp = g.getPos();
            if (gp.getX() == _pacmanPos.getX() && gp.getY() == _pacmanPos.getY()) {
                if (g.toClient().getStatus() == 0) { _score += 20; g.setEdible(0); }
                else { _status = DONE; System.out.println("GAME OVER"); }
            }
        }
        draw();
        try { Thread.sleep(GameInfo.DT); } catch (Exception e) {}
    }

    @Override public void play() {
        if (_status == PAUSED) { _status = RUNNING; System.out.println("Game Started!"); }
        else if (_status == RUNNING) { _status = PAUSED; System.out.println("Game Paused!"); }
    }

    private void draw() {
        StdDraw.clear(Color.BLACK);
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int val = _board[x][y];
                double dx = x + 0.5, dy = y + 0.5;
                if (val == WALL) { StdDraw.setPenColor(Color.BLUE); StdDraw.setPenRadius(0.005); StdDraw.square(dx, dy, 0.45); StdDraw.setPenRadius(); }
                else if (val == DOT) { StdDraw.setPenColor(new Color(255, 180, 180)); StdDraw.filledCircle(dx, dy, 0.10); }
                else if (val == POWER) { StdDraw.setPenColor(Color.GREEN); StdDraw.filledCircle(dx, dy, 0.25); }
            }
        }
        double pX = _pacmanPos.getX() + 0.5, pY = _pacmanPos.getY() + 0.5, angle = 0;
        if (_lastDir == UP) angle = 90; if (_lastDir == LEFT) angle = 180; if (_lastDir == DOWN) angle = 270;
        try { StdDraw.picture(pX, pY, "p1.png", 0.9, 0.9, angle); } catch (Exception e) { StdDraw.setPenColor(Color.YELLOW); StdDraw.filledCircle(pX, pY, 0.4); }

        for (Ghost g : _ghosts) {
            Index2D gp = g.getPos();
            double gX = gp.getX() + 0.5, gY = gp.getY() + 0.5;
            String imgName = "g" + (g.getId() % 3) + ".png";
            try { StdDraw.picture(gX, gY, imgName, 0.9, 0.9); if (g.toClient().getStatus() == 0) { StdDraw.setPenColor(Color.CYAN); StdDraw.circle(gX, gY, 0.5); } }
            catch (Exception e) { StdDraw.setPenColor(Color.RED); StdDraw.filledCircle(gX, gY, 0.4); }
        }

        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 18));
        if (_status == PAUSED) StdDraw.text(cols / 2.0, rows + 1.0, "PAUSED - PRESS SPACE TO START");
        else {
            double time = (System.currentTimeMillis() - _startTime) / 1000.0;
            StdDraw.text(cols / 2.0, rows + 1.0, String.format("T: %.3f, S: %d, St: %d, K: 0, P: %d,%d, D: 0", time, _score, _status, _pacmanPos.getX(), _pacmanPos.getY()));
        }
        StdDraw.show();
    }

    // --- מימושים ---
    @Override public int[][] getGame(int code) { return _board; }
    @Override public exe.ex3.game.GhostCL[] getGhosts(int code) {
        GhostCL[] res = new GhostCL[_ghosts.size()];
        for (int i = 0; i < _ghosts.size(); i++) res[i] = (GhostCL) _ghosts.get(i).toClient();
        return (exe.ex3.game.GhostCL[]) res;
    }

    // --- התיקון הקריטי כאן: מחזיר מחרוזת פשוטה שהאלגוריתם יודע לקרוא ---
    @Override public String getPos(int code) {
        if (_pacmanPos == null) return "0,0"; // מונע קריסה במקרה קצה
        return _pacmanPos.getX() + "," + _pacmanPos.getY();
    }

    @Override public int getStatus() { return _status; }
    @Override public Character getKeyChar() { if (StdDraw.hasNextKeyTyped()) return StdDraw.nextKeyTyped(); return null; }
    @Override public void end(int i) { System.exit(0); }
    @Override public String getMapData() { return ""; }
    public static int getIntColor(Color c, int code) { if (c == Color.BLUE) return WALL; if (c == Color.PINK) return DOT; if (c == Color.GREEN) return POWER; return EMPTY; }
}