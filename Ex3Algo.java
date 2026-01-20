package Client;

import Add_Ons.Index2D;
import Server.Map;
import Server.Map2D;
import Server.Pixel2D;
import Server.Game;          // שימוש במחלקת Game מהשרת
import exe.ex3.game.GhostCL; // שימוש ב-GhostCL (שנמצא ב-exe.ex3.game או Server, תלוי איפה שמרת)
import Server.PacManAlgo;    // הממשק הנכון!
import Server.PacmanGame;    // הממשק הנכון!

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Ex3Algo - Refactored and optimized PacMan Algorithm.
 */
public class Ex3Algo implements PacManAlgo {

    private int tickCounter;
    private static final int SAFE_DISTANCE_VAL = 1000;
    private static final int WALL_VAL = -1;

    // Global safety state
    private Map2D _lastGhostHeatMap = null;
    private List<Index2D> _dangerousGhostsGlobal = new ArrayList<>();

    // Modes
    private static final int MODE_COLLECT = 0;
    private static final int MODE_HUNT = 1;

    // Config
    private static final boolean ALWAYS_AVOID_GHOSTS = false; // שיניתי ל-false כדי שיהיה מעניין (ירדוף אחרי רוחות)

    private int _mode = MODE_COLLECT;
    private int _modeLock = 0;
    private int _lastPelletCount = -1;
    private int _lastGreenEatTick = -1;
    private int _lastGreenCount = -1;

    public Ex3Algo() {
        tickCounter = 0;
    }

    // שים לב: ב-Server.PacManAlgo המינימלי שיצרנו אין getInfo(), אבל אם זה לא מפריע לקומפילציה תשאיר.
    public String getInfo() {
        return "Smart Greedy Algorithm";
    }

    @Override
    public int move(PacmanGame game) {
        tickCounter++;
        int code = 0;
        int[][] boardData = game.getGame(code);
        int blueCode = Game.getIntColor(Color.BLUE, code);

        // המרת מיקום הפקמן
        Pixel2D pacmanPos = toIndex(game.getPos(code));
        GhostCL[] ghosts = (GhostCL[]) game.getGhosts(code);

        // יצירת מפות
        Map2D gameMap = new Map(boardData);
        Map2D pacmanDistances = gameMap.allDistance(pacmanPos, blueCode);

        // חישוב מפת חום (סכנות)
        Map2D ghostHeatMap = computeGhostHeatMap(gameMap, ghosts);
        _lastGhostHeatMap = ghostHeatMap;

        // --- אסטרטגיה ---

        // 1. האם יש רוח אכילה קרובה? (תקיפה)
        if (!ALWAYS_AVOID_GHOSTS) {
            Pixel2D vulnerableGhost = findVulnerableGhost(pacmanDistances, ghosts);
            if (vulnerableGhost != null) {
                return computeNextStep(gameMap, vulnerableGhost, pacmanPos);
            }
        }

        // 2. בריחה מסכנה מיידית
        final int DANGER_RADIUS_TRIGGER = 3;
        int pacGD = ghostHeatMap.getPixel(pacmanPos);
        if (pacGD != SAFE_DISTANCE_VAL && pacGD <= DANGER_RADIUS_TRIGGER) {
            return executeEvasion(gameMap, ghostHeatMap, ghosts, pacmanPos);
        }

        // 3. איסוף נקודות (רגיל)
        // מתייחסים לרוחות כקירות כדי לא להתקע בהן
        Map2D mapWithGhostsAsWalls = treatGhostsAsObstacles(new Map(gameMap.getMap()), ghosts);

        // איסוף רשימות
        List<Index2D> greenList = new ArrayList<>();
        List<Index2D> pinkList = new ArrayList<>();
        int greenCode = Game.getIntColor(Color.GREEN, code);
        int pinkCode = Game.getIntColor(Color.PINK, code);

        for (int x = 0; x < boardData.length; x++) {
            for (int y = 0; y < boardData[0].length; y++) {
                if (boardData[x][y] == greenCode) greenList.add(new Index2D(x,y));
                else if (boardData[x][y] == pinkCode) pinkList.add(new Index2D(x,y));
            }
        }

        // בחירת היעד הטוב ביותר (Best Target)
        double bestScore = Double.NEGATIVE_INFINITY;
        Pixel2D bestTarget = null;

        // פונקציית הערכה פנימית (פשוטה יותר מהלמדה כדי למנוע סיבוך בקוד)
        for(Index2D p : pinkList) {
            double s = evaluateTarget(p, "PINK", gameMap, pacmanPos, ghostHeatMap, blueCode);
            if(s > bestScore) { bestScore = s; bestTarget = p; }
        }

        // עדיפות לנקודות כוח ירוקות אם בטוח
        for(Index2D g : greenList) {
            double s = evaluateTarget(g, "GREEN", gameMap, pacmanPos, ghostHeatMap, blueCode);
            if(s > bestScore) { bestScore = s; bestTarget = g; }
        }

        if(bestTarget != null) {
            return computeNextStep(gameMap, bestTarget, pacmanPos);
        }

        // Fallback
        int safe = chooseSafeDirection(gameMap, new Index2D(pacmanPos), blueCode);
        if(safe != Integer.MIN_VALUE) return safe;

        return getRandomDirection();
    }

    // --- פונקציות עזר (Logic) ---

    private double evaluateTarget(Pixel2D t, String type, Map2D map, Pixel2D pacPos, Map2D heatMap, int wallColor) {
        Pixel2D[] path = map.shortestPath(pacPos, t, wallColor);
        if(path == null) return Double.NEGATIVE_INFINITY;

        int len = path.length - 1;
        if(len <= 0) return Double.NEGATIVE_INFINITY;

        int minGhostDist = Integer.MAX_VALUE;
        for(Pixel2D p : path) {
            if(isUnsafeCell(p)) return Double.NEGATIVE_INFINITY;
            int gd = heatMap.getPixel(p);
            if(gd >= 0 && gd < minGhostDist) minGhostDist = gd;
        }
        if(minGhostDist == Integer.MAX_VALUE) minGhostDist = SAFE_DISTANCE_VAL;

        double safety = (minGhostDist == SAFE_DISTANCE_VAL ? 100.0 : minGhostDist);
        double score = ("GREEN".equals(type) ? 500 : 100) + (1000.0 / len) + (5 * safety);

        return score;
    }

    private Map computeGhostHeatMap(Map2D map, GhostCL[] ghosts) {
        int W = map.getWidth();
        int H = map.getHeight();
        int blueCode = Game.getIntColor(Color.BLUE, 0);
        int[][] dist = new int[W][H];

        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) dist[x][y] = -1;
        Deque<Index2D> q = new ArrayDeque<>();

        for (GhostCL ghost : ghosts) {
            Index2D gp = toIndex(ghost.getPos(0));
            if (gp == null) continue;
            // אם הרוח מסוכנת (לא אכילה)
            if (ghost.getStatus() != 0) {
                dist[gp.getX()][gp.getY()] = 0;
                q.add(gp);
            }
        }

        int[] dx = {-1,1,0,0};
        int[] dy = {0,0,-1,1};

        while (!q.isEmpty()) {
            Index2D cur = q.poll();
            int d = dist[cur.getX()][cur.getY()];

            for (int k = 0; k < 4; k++) {
                int nx = cur.getX() + dx[k];
                int ny = cur.getY() + dy[k];
                if (nx >= 0 && nx < W && ny >= 0 && ny < H) {
                    if (map.getPixel(nx, ny) != blueCode && dist[nx][ny] == -1) {
                        dist[nx][ny] = d + 1;
                        q.add(new Index2D(nx, ny));
                    }
                }
            }
        }

        Map heatMap = new Map(W, H, SAFE_DISTANCE_VAL);
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                if (map.getPixel(x,y) == blueCode) heatMap.setPixel(x,y, WALL_VAL);
                else if (dist[x][y] != -1) heatMap.setPixel(x,y, dist[x][y]);
            }
        }
        return heatMap;
    }

    private int computeNextStep(Map2D map, Pixel2D target, Pixel2D currentPos) {
        int blueCode = Game.getIntColor(Color.BLUE, 0);
        Pixel2D[] path = map.shortestPath(currentPos, target, blueCode);

        if (path != null && path.length >= 2) {
            Pixel2D next = path[1];
            int cx = currentPos.getX(), cy = currentPos.getY();
            int nx = next.getX(), ny = next.getY();

            if (nx > cx) return Game.RIGHT;
            if (nx < cx) return Game.LEFT;
            if (ny > cy) return Game.UP;
            if (ny < cy) return Game.DOWN;
        }
        return getRandomDirection();
    }

    private int executeEvasion(Map2D map, Map2D heatMap, GhostCL[] ghosts, Pixel2D currentPos) {
        // מחפש את השכן הבטוח ביותר (הכי רחוק מרוחות)
        Pixel2D[] neighbors = getNeighbors(currentPos, map.getWidth(), map.getHeight());
        Pixel2D best = null;
        int maxDist = -1;

        for(Pixel2D n : neighbors) {
            if(map.getPixel(n) == Game.getIntColor(Color.BLUE,0)) continue;
            int d = heatMap.getPixel(n);
            if(d > maxDist) {
                maxDist = d;
                best = n;
            }
        }

        if(best != null) return computeNextStep(map, best, currentPos);
        return getRandomDirection();
    }

    private Pixel2D findVulnerableGhost(Map2D pacmanDistMap, GhostCL[] ghosts) {
        Pixel2D best = null;
        int minD = Integer.MAX_VALUE;

        for(GhostCL g : ghosts) {
            if(g.getStatus() == 0) { // Edible
                Index2D pos = toIndex(g.getPos(0));
                int d = pacmanDistMap.getPixel(pos);
                if(d != -1 && d < minD) {
                    minD = d;
                    best = pos;
                }
            }
        }
        return best;
    }

    // --- Helpers Utilities ---

    private Map2D treatGhostsAsObstacles(Map2D map, GhostCL[] ghosts) {
        int blueCode = Game.getIntColor(Color.BLUE, 0);
        for (GhostCL ghost : ghosts) {
            if (ghost.getStatus() != 0) { // Only dangerous ghosts are obstacles
                Index2D p = toIndex(ghost.getPos(0));
                if(p != null) map.setPixel(p, blueCode);
            }
        }
        return map;
    }

    private boolean isUnsafeCell(Pixel2D cell) {
        if (_lastGhostHeatMap == null) return false;
        int d = _lastGhostHeatMap.getPixel(cell);
        return (d != -1 && d <= 1);
    }

    private Index2D toIndex(Object o) {
        if (o == null) return null;
        if (o instanceof Pixel2D) return new Index2D((Pixel2D)o);
        try {
            String[] s = o.toString().split(",");
            return new Index2D(Integer.parseInt(s[0].trim()), Integer.parseInt(s[1].trim()));
        } catch(Exception e) { return null; }
    }

    private Pixel2D[] getNeighbors(Pixel2D p, int w, int h) {
        return new Pixel2D[] {
                new Index2D(p.getX()+1, p.getY()), new Index2D(p.getX()-1, p.getY()),
                new Index2D(p.getX(), p.getY()+1), new Index2D(p.getX(), p.getY()-1)
        };
    }

    private int chooseSafeDirection(Map2D m, Index2D p, int wall) {
        Pixel2D[] n = getNeighbors(p, m.getWidth(), m.getHeight());
        for(Pixel2D ni : n) {
            if(m.isInside(ni) && m.getPixel(ni)!=wall && !isUnsafeCell(ni))
                return computeNextStep(m, ni, p);
        }
        return Integer.MIN_VALUE;
    }

    private int getRandomDirection() {
        return (int)(Math.random()*4);
    }
}