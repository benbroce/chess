package chess;

import java.util.ArrayList;

public class ChessMovesLog {

    public static class Entry {
        public Entry(ChessMove move, ChessPiece piece) {
            this.move = new ChessMove(move);
            this.piece = new ChessPiece(piece);
        }

        public ChessPiece piece;
        public ChessMove move;

        @Override
        public String toString() {
            return piece + " " + move;
        }
    }

    private ArrayList<Entry> entries;

    public ChessMovesLog() {
        entries = new ArrayList<>();
    }

    public void addMove(Entry entry) {
        entries.add(entry);
    }

    public Entry getLastEntry() {
        return entries.getLast();
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder outString = new StringBuilder();
        outString.append("ChessMovesLog:\n");
        for (Entry entry : entries) {
            outString.append(entry);
            outString.append("\n");
        }
        return outString.toString();
    }
}
