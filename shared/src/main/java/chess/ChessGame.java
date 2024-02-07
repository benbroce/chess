package chess;

import java.util.Collection;

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
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
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
        if ((legalMoves == null) || !legalMoves.contains(move)) {
            throw new InvalidMoveException(move.toString());
        }
        // remove any captured piece at the endPosition
        this.board.removePiece(move.getEndPosition());
        // add the piece at the startPosition to the endPosition, promote if needed
        ChessPiece piece = this.board.getPiece(move.getStartPosition());
        this.board.addPiece(
                move.getEndPosition(),
                (new ChessPiece(
                        piece.getTeamColor(),
                        (move.getPromotionPiece() == null) ? piece.getPieceType() : move.getPromotionPiece()))
        );
        // remove the piece from the startPosition
        this.board.removePiece(move.getStartPosition());
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // check every piece on the board
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