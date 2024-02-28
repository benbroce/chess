package service.serviceExceptions;

/**
 * Indicates the requested resource is already claimed by another user
 */
public class AlreadyTakenException extends Exception {
    public AlreadyTakenException(String message) {
        super(message);
    }
}