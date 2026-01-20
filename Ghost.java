package Server;

import Add_Ons.Index2D;
import exe.ex3.game.GhostCL;
import java.util.ArrayList;
import java.util.List;

public class Ghost {
    private Index2D _pos;
    private int _id;
    private int _status;    // 1 = Dangerous, 0 = Edible
    private int _edibleTime = 0;

    public Ghost(int x, int y, int id) {
        this._pos = new Index2D(x, y);
        this._id = id;
        this._status = 1;
    }

    public int getId() {
        return _id;
    }

    public Index2D getPos() {
        return _pos;
    }

    public void setPos(Index2D p) {
        this._pos = p;
    }

    public void setEdible(int timeInTicks) {
        if (timeInTicks > 0) {
            this._status = 0;
            this._edibleTime = timeInTicks;
        } else {
            this._status = 1;
            this._edibleTime = 0;
        }
    }

    /**
     * ממיר את הרוח לאובייקט GhostCL
     * תיקון: יצירת מחלקה אנונימית כי GhostCL אבסטרקטית
     */
    public GhostCL toClient() {
        return new GhostCL() {
            @Override
            public int getType() {
                return 0;
            }

            @Override
            public String getPos(int i) {
                return "";
            }

            @Override
            public String getInfo() {
                return "";
            }

            @Override
            public double remainTimeAsEatable(int i) {
                return 0;
            }

            @Override
            public int getStatus() {
                return 0;
            }
        };
    }

    public void move(int[][] board, Pixel2D pacmanPos) {
        if (_status == 0) {
            _edibleTime--;
            if (_edibleTime <= 0) {
                _status = 1;
            }
        }

        List<Index2D> validMoves = getValidMoves(board);
        if (validMoves.isEmpty()) return;

        Index2D bestMove = validMoves.get(0);

        if (_status == 1 && Math.random() < 0.65) {
            double minDst = Double.MAX_VALUE;
            for (Index2D move : validMoves) {
                double d = move.distance2D(pacmanPos);
                if (d < minDst) {
                    minDst = d;
                    bestMove = move;
                }
            }
        } else {
            int randIndex = (int) (Math.random() * validMoves.size());
            bestMove = validMoves.get(randIndex);
        }

        this._pos = bestMove;
    }

    private List<Index2D> getValidMoves(int[][] board) {
        List<Index2D> moves = new ArrayList<>();
        int x = _pos.getX();
        int y = _pos.getY();
        int cols = board.length;
        int rows = board[0].length;
        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx >= 0 && nx < cols && ny >= 0 && ny < rows) {
                if (board[nx][ny] != Game.WALL) {
                    moves.add(new Index2D(nx, ny));
                }
            }
        }
        return moves;
    }
}