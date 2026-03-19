package com.modelrouter.exception;

/**
 * API Key 无效，符合 OpenAI 兼容格式返回 401
 */
public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException() {
        super("Invalid API key");
    }

    public InvalidApiKeyException(String message) {
        super(message);
    }
}
