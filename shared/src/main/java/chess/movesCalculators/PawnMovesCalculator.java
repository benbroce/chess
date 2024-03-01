package chess.movesCalculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPosition testPosition;

        // parameters for black vs white pieces
        final boolean isWhite = (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE);
        final int startingRow = isWhite ? 2 : (ChessBoard.BOARD_SIDE_LENGTH - 1);
        final int promotionRow = isWhite ? ChessBoard.BOARD_SIDE_LENGTH : 1;

        // check forward
        testPosition = new ChessPosition((position.getRow() + (isWhite ? 1 : -1)), position.getColumn());
        if (!isMoveOutOfBounds(testPosition) && !isMoveCollision(board, testPosition)) {
            moves.add(new ChessMove(position, testPosition, null));

            // check double forward (only valid from starting row)
            if (position.getRow() == startingRow) {
                testPosition = new ChessPosition((position.getRow() + (isWhite ? 2 : -2)), position.getColumn());
                if (!isMoveCollision(board, testPosition)) {
                    moves.add(new ChessMove(position, testPosition, null));
                }
            }
        }

        // check diagonal left (only valid on capture)
        testPosition = new ChessPosition((position.getRow() + (isWhite ? 1 : -1)), (position.getColumn() - 1));
        if (!isMoveOutOfBounds(testPosition) && isMoveCollisionWithEnemy(board, position, testPosition)) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check diagonal right (only valid on capture)
        testPosition = new ChessPosition((position.getRow() + (isWhite ? 1 : -1)), (position.getColumn() + 1));
        if (!isMoveOutOfBounds(testPosition) && isMoveCollisionWithEnemy(board, position, testPosition)) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check promotion on all moves (hit promotion row? add all possible promotion moves)
        HashSet<ChessMove> movesWithPromotions = new HashSet<>();
        for (ChessMove move : moves) {
            if (move.getEndPosition().getRow() == promotionRow) {
                ChessPosition startPosition = move.getStartPosition();
                ChessPosition endPosition = move.getEndPosition();
                movesWithPromotions.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
                movesWithPromotions.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
                movesWithPromotions.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
                movesWithPromotions.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
            } else {
                movesWithPromotions.add(move);
            }
        }

        return movesWithPromotions;
    }
}
