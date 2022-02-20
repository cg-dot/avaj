package me.cgdot.avaj;

public class ObfuscateException extends RuntimeException {
    public ObfuscateException() {
    }

    public ObfuscateException(String message) {
        super(message);
    }

    public ObfuscateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObfuscateException(Throwable cause) {
        super(cause);
    }

    public ObfuscateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
