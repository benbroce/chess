package service.serviceExceptions;

/**
 * Indicates the request is malformed or out of bounds
 */
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}