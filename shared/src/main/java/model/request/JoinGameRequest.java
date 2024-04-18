package model.request;

// playerColor is "WHITE" or "BLACK" (case-insensitive)
public record JoinGameRequest(String playerColor, int gameID) {
}
