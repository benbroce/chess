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
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() + numMoves), position.getColumn());
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

        // check down
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition((position.getRow() - numMoves), position.getColumn());
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

        // check left
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition(position.getRow(), (position.getColumn() - numMoves));
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

        // check right
        for (int numMoves = 1; true; ++numMoves) {
            testPosition = new ChessPosition(position.getRow(), (position.getColumn() + numMoves));
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
