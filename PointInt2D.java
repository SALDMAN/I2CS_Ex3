package Server;

import java.io.Serializable;

public class PointInt2D implements Serializable {
    private int _x;
    private int _y;

    /**
     * Constructor
     * @param x coordinate
     * @param y coordinate
     */
    public PointInt2D(int x, int y) {
        this._x = x;
        this._y = y;
    }

    /**
     * Copy Constructor
     * @param p another PointInt2D
     */
    public PointInt2D(PointInt2D p) {
        this(p.getX(), p.getY());
    }

    public PointInt2D(String pos) {
        String[] parts = pos.split(",");
        this._x = Integer.parseInt(parts[0]);
        this._y = Integer.parseInt(parts[1]);
    }

    // --- Getters ---

    public int getX() {
        return _x;
    }

    public int getY() {
        return _y;
    }

    // --- Standard Object Methods ---

    @Override
    public String toString() {
        // מחזיר מחרוזת בפורמט "x,y" שנוחה ללוגים
        return _x + "," + _y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PointInt2D other = (PointInt2D) obj;
        return _x == other._x && _y == other._y;
    }

    /**
     * Helper method to calculate Manhattan distance (typical for grid games)
     * Or Euclidean distance if needed. Here is checking direct grid equality usually.
     */
    public boolean equals(int x, int y) {
        return this._x == x && this._y == y;
    }
}