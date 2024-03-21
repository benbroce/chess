package clientAPI;

/**
 * Indicates the server encountered an error in responding to the given request
 */
public class ResponseException extends Exception {
    public ResponseException(String message) {
        super(message);
    }
}