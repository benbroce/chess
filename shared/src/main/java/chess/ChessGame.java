package chess;

import chess.movesCalculators.PieceMovesCalculator;
import jdk.jshell.spi.ExecutionControl;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private ChessMovesLog movesLog;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.movesLog = new ChessMovesLog();
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
     * Gets the en passant move the given piece can make, if any
     *
     * @param startPosition the position of the piece to test
     * @return the en passant capture move the piece can make, or null if there isn't one
     */
    private ChessMove enPassantMove(ChessPosition startPosition) {
        ChessPiece pawn = this.board.getPiece(startPosition);
        // en passant cannot be the first move
        if (movesLog.getEntries().isEmpty()) {
            return null;
        }
        ChessPiece opponentPawn = movesLog.getLastEntry().getPiece();
        ChessMove opponentPawnMove = movesLog.getLastEntry().getMove();
        // return En Passant capture move if all the following are true:
        // - this piece is a pawn
        // - opponent (last logged moving piece) is an enemy pawn
        // - this piece is in an adjacent column to opponent
        // - last logged move is a double move by the opponent
        if ((pawn != null)
                && (pawn.getPieceType() == ChessPiece.PieceType.PAWN)
                && (pawn.getTeamColor() != opponentPawn.getTeamColor())
                && (opponentPawn.getPieceType() == ChessPiece.PieceType.PAWN)
                && (Math.abs(startPosition.getColumn() - opponentPawnMove.getEndPosition().getColumn()) == 1)
                && PieceMovesCalculator.isDoubleMove(opponentPawnMove)) {
            // return move that is capture of opponentPawn
            return new ChessMove(
                    startPosition,
                    (new ChessPosition(
                            (opponentPawnMove.getEndPosition().getRow()
                                    + ((pawn.getTeamColor() == TeamColor.WHITE) ? 1 : -1)),
                            opponentPawnMove.getEndPosition().getColumn()
                    )),
                    null);
        }
        // otherwise, return null
        return null;
    }

    /**
     * Determines whether the given valid move is an en passant capture
     *
     * @param validMove a valid move
     * @return whether the move is an en passant capture
     */
    private boolean isEnPassantMove(ChessMove validMove) {
        ChessPiece piece = this.board.getPiece(validMove.getStartPosition());
        ChessPiece target = this.board.getPiece(validMove.getEndPosition());
        // valid move is en passant if the piece is a pawn, the move is a capture, and the capture square is empty
        return ((piece.getPieceType() == ChessPiece.PieceType.PAWN)
                && (validMove.getStartPosition().getColumn() != validMove.getEndPosition().getColumn())
                && (target == null));
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
        Collection<ChessMove> possibleMoves = pieceToMove.pieceMoves(this.board, startPosition);
        // add En Passant move, if any
        ChessMove enPassantMove = this.enPassantMove(startPosition);
        if (enPassantMove != null) {
            possibleMoves.add(enPassantMove);
        }
        // filter possibleMoves to account for check positions
        Collection<ChessMove> moves = new HashSet<>();
        for (ChessMove move : possibleMoves) {
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
        // if the move is an en passant capture, remove the captured piece
        if (isEnPassantMove(move)) {
            board.removePiece(movesLog.getLastEntry().getMove().getEndPosition());
        }
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
        // record the move in the moves log
        this.movesLog.addMove(new ChessMovesLog.Entry(move, piece));
        // change the teamTurn
        this.teamTurn = (this.teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Finds the first occurrence of a piece type on the given team and board
     *
     * @param board     the board to search
     * @param teamColor the team color to search for
     * @param pieceType the piece type to search for
     * @return the position of the first instance (scanning rows starting at bottom left) of the piece,
     * or null if the piece is not on the board
     */
    private ChessPosition getPiecePosition(ChessBoard board, TeamColor teamColor, ChessPiece.PieceType pieceType) {
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if ((piece != null)
                        && (piece.getTeamColor() == teamColor)
                        && (piece.getPieceType() == pieceType)) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Private helper to determine if the given piece is capturable.
     *
     * @param board         the board to determine capturable status on
     * @param teamColor     which team to check capturable
     * @param piecePosition the position of the piece to determine capturable
     * @return True if the specified team is in check
     */
    private boolean isCapturable(ChessBoard board, TeamColor teamColor, ChessPosition piecePosition) {
        // return true if teamColor's specified piece can be captured
        // not in check if the piece is not on the board
        if (piecePosition == null) {
            return false;
        }
        // for every opposite colored piece
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition opponentPosition = new ChessPosition(row, col);
                ChessPiece opponentPiece = board.getPiece(opponentPosition);
                if ((opponentPiece != null) && (opponentPiece.getTeamColor() != teamColor)) {
                    // check each potential move for capture of the teamColor king
                    for (ChessMove opponentMove : opponentPiece.pieceMoves(board, opponentPosition)) {
                        if (opponentMove.getEndPosition().equals(piecePosition)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Private helper to determine if given team is in check on an arbitrary board.
     *
     * @param board     the board to determine check status on
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        return isCapturable(board, teamColor, getPiecePosition(board, teamColor, ChessPiece.PieceType.KING));
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