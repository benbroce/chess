package service.serviceExceptions;

/**
 * Indicates the user is unauthorized to access the requested resource
 */
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message) {
        super(message);
    }
}