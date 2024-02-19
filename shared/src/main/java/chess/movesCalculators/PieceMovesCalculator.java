package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public abstract class PieceMovesCalculator {
    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);

    /**
     * Indicates whether the given move parameters are out of bounds.
     *
     * @param endPosition the position where the move is attempting to end
     * @return whether the move is out of bounds
     */
    public static boolean isMoveOutOfBounds(ChessPosition endPosition) {
        return ((endPosition.getRow() < 1) || (endPosition.getRow() > ChessBoard.BOARD_SIDE_LENGTH)
                || (endPosition.getColumn() < 1) || (endPosition.getColumn() > ChessBoard.BOARD_SIDE_LENGTH));
    }

    /**
     * Indicates whether the given move parameters result in moving to an occupied position.
     * PREREQUISITE: isMoveOutOfBounds() returns false
     *
     * @param board       the board in question for the proposed ChessMove
     * @param endPosition the position where the move is attempting to end
     * @return whether the move is onto an occupied position
     */
    public static boolean isMoveCollision(ChessBoard board, ChessPosition endPosition) {
        return (board.getPiece(endPosition) != null);
    }

    /**
     * Indicates whether the given move parameters result in moving to an enemy-occupied position.
     *
     * @param board         the board in question for the proposed ChessMove
     * @param startPosition the position where the move is starting
     * @param endPosition   the position where the move is ending
     * @return whether the move is onto an enemy-occupied position
     */
    public static boolean isMoveCollisionWithEnemy(ChessBoard board, ChessPosition startPosition, ChessPosition endPosition) {
        return isMoveCollision(board, endPosition) && (board.getPiece(startPosition).getTeamColor() != board.getPiece(endPosition).getTeamColor());
    }

    /**
     * Indicates that the given move is a 2-square non-diagonal movement
     * (meant to check situational move cases on pieces that usually move 1 square: pawn and king)
     *
     * @param move the move to check (NOTE: result is only meaningful if this move is taken by a king or pawn)
     * @return if the move is a 2-square non-diagonal move
     */
    public static boolean isDoubleMove(ChessMove move) {
        boolean sameOnRow = (move.getEndPosition().getRow() == move.getStartPosition().getRow());
        boolean doubleOnRow = (Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) == 2);
        boolean sameOnCol = (move.getEndPosition().getColumn() == move.getStartPosition().getColumn());
        boolean doubleOnCol = (Math.abs(move.getEndPosition().getColumn() - move.getStartPosition().getColumn()) == 2);
        return ((sameOnRow && doubleOnCol) || (sameOnCol && doubleOnRow));
    }
}
