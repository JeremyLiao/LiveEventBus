package com.jeremyliao.liveeventbus.ipc.encode;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class EncodeException extends Exception {
    public EncodeException() {
    }

    public EncodeException(String message) {
        super(message);
    }

    public EncodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodeException(Throwable cause) {
        super(cause);
    }
}
