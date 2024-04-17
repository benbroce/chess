package chess;

import chess.movesCalculators.PieceMovesCalculator;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;
    private final ChessMovesLog movesLog;
    private boolean isOver;

    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.movesLog = new ChessMovesLog();
        this.isOver = false;
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
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    /**
     * Sets this game's chessboard with a given board and resets the move log
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.movesLog.reset();
    }

    public boolean isOver() {
        return this.isOver;
    }

    public void setOver() {
        this.isOver = true;
    }

    // MOVES //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Makes a move in a chess game, change the team turn
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> legalMoves = this.validMoves(move.getStartPosition());
        // if move is illegal, throw an exception
        if (this.isOver()) {
            throw new InvalidMoveException("Cannot Move, Game is Over");
        }
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
        // set game over under stalemate or checkmate
        if (this.isInStalemate(this.teamTurn) || this.isInCheckmate(this.teamTurn)) {
            this.isOver = true;
        }
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
        // add castling moves, if any
        possibleMoves.addAll(this.castlingMoves(startPosition));
        // filter possibleMoves to account for check positions
        Collection<ChessMove> moves = new HashSet<>();
        for (ChessMove move : possibleMoves) {
            ChessBoard testBoard = new ChessBoard(this.board);
            makeMoveUnchecked(testBoard, move);
            // only keep moves that do not end with the king in check
            // if move is castling move, also only keep moves that do not make the castling rook capturable
            boolean isCastleLeft = (move.getStartPosition().getColumn() > move.getEndPosition().getColumn());
            if (!isInCheck(testBoard, pieceToMove.getTeamColor())
                    && (!isCastlingMove(move) || !isCapturable(testBoard, pieceToMove.getTeamColor(), (isCastleLeft
                    ? (new ChessPosition(move.getStartPosition().getRow(), 4))
                    : (new ChessPosition(move.getStartPosition().getRow(), (ChessBoard.BOARD_SIDE_LENGTH - 2)))
            )))
            ) {
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
            board.removePiece(movesLog.getLastEntry().move().getEndPosition());
        }
        // if the move is a castling move, move the rook to one space inside from the king
        if (isCastlingMove(move)) {
            boolean isCastleLeft = (move.getStartPosition().getColumn() > move.getEndPosition().getColumn());
            board.removePiece(isCastleLeft
                    ? (new ChessPosition(move.getStartPosition().getRow(), 1))
                    : (new ChessPosition(move.getStartPosition().getRow(), ChessBoard.BOARD_SIDE_LENGTH)));
            board.addPiece((isCastleLeft
                            ? (new ChessPosition(move.getStartPosition().getRow(), 4))
                            : (new ChessPosition(move.getStartPosition().getRow(), (ChessBoard.BOARD_SIDE_LENGTH - 2)))
                    ),
                    (new ChessPiece(piece.getTeamColor(), ChessPiece.PieceType.ROOK)));
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

    // SITUATIONAL MOVES //////////////////////////////////////////////////////////////////////////

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
        ChessPiece opponentPawn = movesLog.getLastEntry().piece();
        ChessMove opponentPawnMove = movesLog.getLastEntry().move();
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
     * Gets the castling move the king can make, if any
     *
     * @param startPosition the position of the piece to test
     * @return the castling move the piece can make, or null if there isn't one
     */
    private Collection<ChessMove> castlingMoves(ChessPosition startPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece king = this.board.getPiece(startPosition);
        // add castling move if all the following are true:
        // - this piece is a king
        // - the king has not moved yet and started in the correct position
        // - the king is not in check
        // - ...
        if ((king == null)
                || (king.getPieceType() != ChessPiece.PieceType.KING)
                || (pieceHasMoved(startPosition))
                || !(startPosition.equals(new ChessPosition(
                ((king.getTeamColor() == TeamColor.WHITE) ? 1 : ChessBoard.BOARD_SIDE_LENGTH), 5)))
                || isInCheck(king.getTeamColor())) {
            return moves; // empty set
        }
        // - (continued)
        // - the attempted rook has not moved yet
        // - there are no pieces between the king and the attempted rook
        ChessPosition rookPosition;
        boolean isPathClear;
        if (king.getTeamColor() == TeamColor.WHITE) {
            // check white castle left
            rookPosition = new ChessPosition(1, 1);
            isPathClear = true;
            for (int i = 1; i <= 3; ++i) {
                if (board.getPiece(new ChessPosition(rookPosition.getRow(), (rookPosition.getColumn() + i))) != null) {
                    isPathClear = false;
                }
            }
            if (!pieceHasMoved(rookPosition) && isPathClear) {
                moves.add(new ChessMove(startPosition,
                        (new ChessPosition(startPosition.getRow(), (startPosition.getColumn()) - 2)),
                        null));
            }
            // check white castle right
            rookPosition = new ChessPosition(1, ChessBoard.BOARD_SIDE_LENGTH);
            isPathClear = true;
            for (int i = 1; i <= 2; ++i) {
                if (board.getPiece(new ChessPosition(rookPosition.getRow(), (rookPosition.getColumn() - i))) != null) {
                    isPathClear = false;
                }
            }
            if (!pieceHasMoved(rookPosition) && isPathClear) {
                moves.add(new ChessMove(startPosition,
                        (new ChessPosition(startPosition.getRow(), (startPosition.getColumn()) + 2)),
                        null));
            }
        } else {
            // check black castle left
            rookPosition = new ChessPosition(ChessBoard.BOARD_SIDE_LENGTH, 1);
            isPathClear = true;
            for (int i = 1; i <= 3; ++i) {
                if (board.getPiece(new ChessPosition(rookPosition.getRow(), (rookPosition.getColumn() + i))) != null) {
                    isPathClear = false;
                }
            }
            if (!pieceHasMoved(rookPosition) && isPathClear) {
                moves.add(new ChessMove(startPosition,
                        (new ChessPosition(startPosition.getRow(), (startPosition.getColumn()) - 2)),
                        null));
            }
            // check black castle right
            rookPosition = new ChessPosition(ChessBoard.BOARD_SIDE_LENGTH, ChessBoard.BOARD_SIDE_LENGTH);
            isPathClear = true;
            for (int i = 1; i <= 2; ++i) {
                if (board.getPiece(new ChessPosition(rookPosition.getRow(), (rookPosition.getColumn() - i))) != null) {
                    isPathClear = false;
                }
            }
            if (!pieceHasMoved(rookPosition) && isPathClear) {
                moves.add(new ChessMove(startPosition,
                        (new ChessPosition(startPosition.getRow(), (startPosition.getColumn()) + 2)),
                        null));
            }
        }
        // return the resulting moves
        return moves;
    }

    /**
     * Determines whether the given valid move is a castling move
     *
     * @param validMove a valid move
     * @return whether the valid move is a castling move
     */
    private boolean isCastlingMove(ChessMove validMove) {
        ChessPiece piece = this.board.getPiece(validMove.getStartPosition());
        return ((piece.getPieceType() == ChessPiece.PieceType.KING)
                && (PieceMovesCalculator.isDoubleMove(validMove)));
    }

    /**
     * Determines whether the piece at the given position has moved this game
     *
     * @param piecePosition the position of the piece to inspect
     * @return whether the piece has moved this game
     */
    private boolean pieceHasMoved(ChessPosition piecePosition) {
        for (ChessMovesLog.Entry entry : movesLog.getEntries()) {
            if (entry.move().getStartPosition().equals(piecePosition)) {
                return true;
            }
        }
        return false;
    }

    // GAME END STATES ////////////////////////////////////////////////////////////////////////////

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
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(this.board, teamColor);
    }

    /**
     * Private helper to determine if given team is in check on an arbitrary board.
     *
     * @param board     the board to determine check status on
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    private boolean isInCheck(ChessBoard board, TeamColor teamColor) {
        return isCapturable(board, teamColor, getKingPosition(board, teamColor));
    }

    /**
     * Private helper to determine if the given piece is capturable.
     *
     * @param board         the board to determine capturable status on
     * @param teamColor     which team to check capturable
     * @param piecePosition the position of the piece to determine capturable
     * @return True if the specified piece is capturable
     */
    private boolean isCapturable(ChessBoard board, TeamColor teamColor, ChessPosition piecePosition) {
        // return true if teamColor's specified piece can be captured
        // not capturable if the piece is not on the board
        if (piecePosition == null) {
            return false;
        }
        // for every opposite colored piece
        for (int row = 1; row <= ChessBoard.BOARD_SIDE_LENGTH; ++row) {
            for (int col = 1; col <= ChessBoard.BOARD_SIDE_LENGTH; ++col) {
                ChessPosition opponentPosition = new ChessPosition(row, col);
                ChessPiece opponentPiece = board.getPiece(opponentPosition);
                if ((opponentPiece != null) && (opponentPiece.getTeamColor() != teamColor)) {
                    // check each potential move for capture of the piece
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
     * Finds the king on the given team and board
     *
     * @param board     the board to search
     * @param teamColor the team color to search for
     * @return the position of the king,
     * or null if the king is not on the board
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
}