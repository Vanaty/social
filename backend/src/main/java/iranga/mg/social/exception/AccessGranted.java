package iranga.mg.social.exception;

public class AccessGranted extends RuntimeException {
    public AccessGranted(String message) {
        super(message);
    }

    public AccessGranted(String message, Throwable cause) {
        super(message, cause);
    }
}
