package com.jeremyliao.liveeventbus.ipc.decode;

/**
 * Created by liaohailiang on 2019/3/26.
 */
public class DecodeException extends Exception {
    public DecodeException() {
    }

    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecodeException(Throwable cause) {
        super(cause);
    }
}
