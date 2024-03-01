package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.function.Function;

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

    /**
     * Adds moves to a collection of moves for a piece moving along a linear path (bishop, for example),
     * with steps defined in the generateTestPosition lambda,
     * stopping on collision with another piece (after capture if it is an enemy) or the edge of the board
     *
     * @param board                the board to consider collisions on
     * @param position             the starting position of the piece
     * @param moves                the collection to add moves to
     * @param generateTestPosition a lambda function used to generate steps along the linear test path
     *                             (maps number of steps -> ChessPosition)
     */
    public static void addLinearMovesUntilCollision(ChessBoard board,
                                                    ChessPosition position,
                                                    Collection<ChessMove> moves,
                                                    Function<Integer, ChessPosition> generateTestPosition) {
        for (int numMoves = 1; true; ++numMoves) {
            ChessPosition testPosition = generateTestPosition.apply(numMoves);
            if (isMoveOutOfBounds(testPosition)) {
                break;
            } else {
                if (isMoveCollision(board, testPosition)) {
                    if (isMoveCollisionWithEnemy(board, position, testPosition)) {
                        moves.add(new ChessMove(position, testPosition, null));
                    }
                    break;
                }
                moves.add(new ChessMove(position, testPosition, null));
            }
        }
    }
}
