package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public static final int BOARD_SIDE_LENGTH = 8;

    // implement chess board as a 2D array ([row][col]) of ChessPiece
    // NOTE: bottom left of board corresponds to ChessPosition (1, 1)
    // NOTE: board is 0-indexed [0-7], but ChessPosition is 1-indexed [1-8]
    private ChessPiece[][] board;

    public ChessBoard() {
        board = new ChessPiece[BOARD_SIDE_LENGTH][BOARD_SIDE_LENGTH];
        this.clearBoard();
    }

    public ChessBoard(ChessBoard other) {
        this();
        for (int row = 1; row <= BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= BOARD_SIDE_LENGTH; ++col) {
                ChessPiece otherPiece = other.board[row][col];
                if (otherPiece != null) {
                    this.board[row][col] = new ChessPiece(otherPiece.getTeamColor(), otherPiece.getPieceType());
                }
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Removes a chess piece from the chessboard
     *
     * @param position position to remove the piece from
     */
    public void removePiece(ChessPosition position) {
        this.board[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.board[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Clears the board of all pieces
     */
    private void clearBoard() {
        for (int row = 1; row <= BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= BOARD_SIDE_LENGTH; ++col) {
                removePiece(new ChessPosition(row, col));
            }
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // clear the board
        this.clearBoard();
        // place white bottom row
        this.addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        this.addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        this.addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        this.addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        // place black top row
        this.addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        this.addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        this.addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        this.addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        this.addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        this.addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        // place pawns
        for (int col = 1; col <= BOARD_SIDE_LENGTH; ++col) {
            this.addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            this.addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public String toString() {
        StringBuilder outString = new StringBuilder();
        ChessPiece piece = null;
        for (int row = BOARD_SIDE_LENGTH; row >= 1; --row) {
            for (int col = 1; col <= BOARD_SIDE_LENGTH; ++col) {
                outString.append("|");
                piece = this.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    outString.append(" ");
                } else {
                    outString.append(piece);
                }
            }
            outString.append("|\n");
        }
        return outString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
