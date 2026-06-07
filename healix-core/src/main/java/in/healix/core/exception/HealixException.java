package in.healix.core.exception;

public class HealixException extends RuntimeException {
    private final String errorCode;

    public HealixException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HealixException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
