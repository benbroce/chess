package chess;

import chess.movesCalculators.*;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece == null) return null;
        return switch (piece.type) {
            case KING -> (new KingMovesCalculator()).pieceMoves(board, myPosition);
            case QUEEN -> (new QueenMovesCalculator()).pieceMoves(board, myPosition);
            case BISHOP -> (new BishopMovesCalculator()).pieceMoves(board, myPosition);
            case KNIGHT -> (new KnightMovesCalculator()).pieceMoves(board, myPosition);
            case ROOK -> (new RookMovesCalculator()).pieceMoves(board, myPosition);
            case PAWN -> (new PawnMovesCalculator()).pieceMoves(board, myPosition);
        };
    }

    @Override
    public String toString() {
        String pieceString = switch (this.type) {
            case PieceType.KING -> "K";
            case PieceType.QUEEN -> "Q";
            case PieceType.BISHOP -> "B";
            case PieceType.KNIGHT -> "N";
            case PieceType.ROOK -> "R";
            case PieceType.PAWN -> "P";
        };
        if (this.pieceColor == ChessGame.TeamColor.BLACK) {
            pieceString = pieceString.toLowerCase();
        }
        return pieceString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        ChessPiece that = (ChessPiece) o;
        return (pieceColor == that.pieceColor) && (type == that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
