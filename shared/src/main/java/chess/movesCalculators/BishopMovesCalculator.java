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
        ChessPosition testPosition;

        // check up, right
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() + numMoves), (position.getColumn() + numMoves));
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

        // check up, left
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() + numMoves), (position.getColumn() - numMoves));
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

        // check down, right
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() - numMoves), (position.getColumn() + numMoves));
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

        // check down, left
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() - numMoves), (position.getColumn() - numMoves));
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

        return moves;
    }
}
