package Client;

import Server.Game;
import Server.PacManAlgo;
import Server.PacmanGame;

public class Ex3Main {
    private static Character _cmd;

    public static void main(String[] args) {
        prepareAssets();
        play1();
    }

    public static void play1() {
        Game ex3 = new Game();
        ex3.init(GameInfo.CASE_SCENARIO, GameInfo.MY_ID, GameInfo.CYCLIC_MODE, GameInfo.RANDOM_SEED, GameInfo.RESOLUTION_NORM, GameInfo.DT, -1);

        PacManAlgo man = GameInfo.ALGO;

        System.out.println("Main Loop Started. Please click on the Game Window and press SPACE.");

        while(ex3.getStatus() != PacmanGame.DONE) {
            // קריאת המקש שנלחץ
            _cmd = ex3.getKeyChar();

            // דיבאג: אם נלחץ משהו, נדפיס אותו
            if (_cmd != null) {
                System.out.println("Key Pressed: '" + _cmd + "'");

                if (_cmd == ' ') {
                    ex3.play(); // הפעלת/עצירת המשחק
                }
            }

            if (_cmd != null && _cmd == 'h') {
                System.out.println("Help: Press SPACE to start/pause.");
            }

            // תנועת האלגוריתם (קורית רק כשהמשחק רץ, כי Game.move בודק את הסטטוס)
            int dir = man.move(ex3);
            ex3.move(dir);
        }
        ex3.end(-1);
    }

    public static Character getCMD() {
        return _cmd;
    }

    private static void prepareAssets() {
        ensureFileExists("test.bit");
        ensureFileExists("p1.png");
        ensureFileExists("g0.png");
        ensureFileExists("g1.png");
        ensureFileExists("g2.png");
        ensureFileExists("g3.png");
    }

    private static void ensureFileExists(String filename) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(filename);
            if (java.nio.file.Files.exists(p)) return;
            try (var in = Ex3Main.class.getResourceAsStream("/" + filename)) {
                if (in == null) return;
                java.nio.file.Files.copy(in, p);
            }
        } catch (Exception e) {}
    }
}