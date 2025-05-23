package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;
    private final char[] columnLetters = {0, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public ChessPosition(char col, int row) {
        int search_col = 0;
        for (int i = 1; i < this.columnLetters.length; ++i) {
            if (this.columnLetters[i] == col) {
                search_col = i;
                break;
            }
        }
        this.col = search_col;
        this.row = row;
    }

    public ChessPosition(ChessPosition other) {
        this.row = other.row;
        this.col = other.col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.col;
    }

    @Override
    public String toString() {
        return String.format("%c%d", this.columnLetters[col], row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        ChessPosition that = (ChessPosition) o;
        return (row == that.row) && (col == that.col);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}