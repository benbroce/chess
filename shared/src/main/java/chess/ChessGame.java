package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets the valid moves for a piece at the given location
     * NOTE: Does not invalidate moves for being out-of-turn
     *
     * @param startPosition the position of the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece pieceToMove = this.board.getPiece(startPosition);
        // return null if there is no piece at startPosition
        if (pieceToMove == null) {
            return null;
        }
        // filter possible moves (pieceMoves()) to account for check positions
        Collection<ChessMove> moves = new HashSet<>();
        for (ChessMove move : pieceToMove.pieceMoves(this.board, startPosition)) {
            ChessBoard testBoard = new ChessBoard(this.board);
            makeMoveUnchecked(testBoard, move);
            // if in check: keep moves that take the king out of check
            // if not in check: keep moves that don't put the king in check
            if (!isInCheck(testBoard, pieceToMove.getTeamColor())) {
                moves.add(move);
            }
        }
        return moves;
    }

    /**
     * Make a move on a given board without checking if it is valid
     *
     * @param board board to make the move on (modified)
     * @param move  chess move to perform
     */
    private void makeMoveUnchecked(ChessBoard board, ChessMove move) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        // remove any captured piece at the endPosition
        board.removePiece(move.getEndPosition());
        // add the piece at the startPosition to the endPosition, promote if needed
        board.addPiece(
                move.getEndPosition(),
                (new ChessPiece(
                        piece.getTeamColor(),
                        (move.getPromotionPiece() == null) ? piece.getPieceType() : move.getPromotionPiece()))
        );
        // remove the piece from the startPosition
        board.removePiece(move.getStartPosition());
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legalMoves = this.validMoves(move.getStartPosition());
        // if move is illegal, throw an exception
        if (legalMoves == null) {
            throw new InvalidMoveException(
                    String.format("Attempted Move: %s Moves a nonexistent piece.", move));
        }
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        if (piece.getTeamColor() != this.teamTurn) {
            throw new InvalidMoveException(
                    String.format("Attempted Move: %s Moves out of turn.", move));
        }
        if (!legalMoves.contains(move)) {
            throw new InvalidMoveException(
                    String.format("Attempted Move: %s Invalid for piece or endangers the king.", move));
        }
        // make the move
        makeMoveUnchecked(this.board, move);
        // change the teamTurn
        this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Finds the position of the king on the given team and board
     *
     * @param board     the board to check
     * @param teamColor which team to check
     * @return the position of the king of teamColor, or null if the king is not on the board
     */
    private ChessPosition getKingPosition(ChessBoard board, TeamColor teamColor) {
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if ((piece != null)
                        && (piece.getTeamColor() == teamColor)
                        && (piece.getPieceType() == ChessPiece.PieceType.KING)) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Private helper to determine if given team is in check on an arbitrary board.
     *
     * @param board     the board to determine check status on
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        // return true if teamColor's king can be captured
        ChessPosition kingPosition = getKingPosition(board, teamColor);
        // not in check if the king is not on the board
        if (kingPosition == null) {
            return false;
        }
        // for every opposite colored piece
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if ((piece != null) && (piece.getTeamColor() != teamColor)) {
                    // check each potential move for capture of the teamColor king
                    for (ChessMove move : piece.pieceMoves(board, position)) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(this.board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // return true if teamColor's king isInCheck() and all teamColor pieces have no validMoves()
        return (isInCheck(teamColor) && hasNoValidMoves(teamColor));
    }

    /**
     * Determines whether a team has no valid moves remaining.
     *
     * @param teamColor the color team to check
     * @return True if a valid move remains for that team
     */
    private boolean hasNoValidMoves(TeamColor teamColor) {
        // check every square on the board
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = this.board.getPiece(position);
                // return false on proof that there is at least one valid move for the given teamColor
                // proof if: there is a piece, it is of the given teamColor, and it has a valid move
                if ((piece != null) && (piece.getTeamColor() == teamColor) && !validMoves(position).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // return false if it is not teamColor's turn or teamColor is in check
        if (teamColor != this.teamTurn || isInCheck(teamColor)) {
            return false;
        }
        return hasNoValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}