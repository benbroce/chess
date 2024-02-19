package chess.movesCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class KnightMovesCalculator extends PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPosition testPosition;

        // check up, left
        testPosition = new ChessPosition((position.getRow() + 2), (position.getColumn() - 1));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check up, right
        testPosition = new ChessPosition((position.getRow() + 2), (position.getColumn() + 1));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check down, left
        testPosition = new ChessPosition((position.getRow() - 2), (position.getColumn() - 1));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check down, right
        testPosition = new ChessPosition((position.getRow() - 2), (position.getColumn() + 1));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check left, up
        testPosition = new ChessPosition((position.getRow() + 1), (position.getColumn() - 2));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check left, down
        testPosition = new ChessPosition((position.getRow() - 1), (position.getColumn() - 2));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check right, up
        testPosition = new ChessPosition((position.getRow() + 1), (position.getColumn() + 2));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        // check right, down
        testPosition = new ChessPosition((position.getRow() - 1), (position.getColumn() + 2));
        if (!isMoveOutOfBounds(testPosition) && (!isMoveCollision(board, testPosition) || isMoveCollisionWithEnemy(board, position, testPosition))) {
            moves.add(new ChessMove(position, testPosition, null));
        }

        return moves;
    }
}
