package chess;

import java.util.Collection;

public abstract class PieceMovesCalculator {
    public abstract Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);

    /**
     * Indicates whether the given move parameters are out of bounds.
     *
     * @param board       the board in question for the proposed ChessMove
     * @param endPosition the position where the move is attempting to end
     * @return whether the move is out of bounds
     */
    public boolean isMoveOutOfBounds(ChessBoard board, ChessPosition endPosition) {
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
    public boolean isMoveCollision(ChessBoard board, ChessPosition endPosition) {
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
    public boolean isMoveCollisionWithEnemy(ChessBoard board, ChessPosition startPosition, ChessPosition endPosition) {
        return isMoveCollision(board, endPosition) && (board.getPiece(startPosition).getTeamColor() != board.getPiece(endPosition).getTeamColor());
    }
}
