package service.serviceExceptions;

/**
 * Indicates a server error has occurred
 */
public class ServerErrorException extends Exception {
    public ServerErrorException(String message) {
        super(message);
    }
}