package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class QueenMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        // add diagonal moves
        moves.addAll((new BishopMovesCalculator()).pieceMoves(board, position));
        // add straight moves
        moves.addAll((new RookMovesCalculator()).pieceMoves(board, position));
        return moves;
    }
}
