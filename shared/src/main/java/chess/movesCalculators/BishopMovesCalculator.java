package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class BishopMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();

        // check up, right
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() + numMoves), (position.getColumn() + numMoves))));

        // check up, left
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() + numMoves), (position.getColumn() - numMoves))));

        // check down, right
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() - numMoves), (position.getColumn() + numMoves))));

        // check down, left
        PieceMovesCalculator.addLinearMovesUntilCollision(board, position, moves,
                (numMoves -> new ChessPosition((position.getRow() - numMoves), (position.getColumn() - numMoves))));

        return moves;
    }
}
