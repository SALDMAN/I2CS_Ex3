package Server;
/**
 * Interface for implementing a Pacman playing algorithm.
 */
public interface PacManAlgo {

    /**
     * @return Information about the algorithm (e.g., student name/ID).
     */
    String getInfo();

    /**
     * Calculates the next move based on the current game state.
     *
     * @param game The current game instance/interface.
     * @return An integer representing the chosen direction (UP, DOWN, LEFT, RIGHT, STAY).
     */
    int move(PacmanGame game);
}