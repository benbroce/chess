package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class RookMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPosition testPosition;

        // check up
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() + numMoves), position.getColumn())));

        // check down
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() - numMoves), position.getColumn())));

        // check left
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition(position.getRow(), (position.getColumn() - numMoves))));

        // check right
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition(position.getRow(), (position.getColumn() + numMoves))));

        return moves;
    }
}
