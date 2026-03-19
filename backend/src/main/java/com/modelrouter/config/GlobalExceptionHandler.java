package com.modelrouter.config;

import com.modelrouter.exception.InvalidApiKeyException;
import com.modelrouter.service.RouterService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI 兼容错误格式: {"error": {"message": "...", "type": "...", "code": "..."}}
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** OpenAI 标准错误体 */
    private static Map<String, Object> openAIError(String message, String type, String code) {
        Map<String, Object> err = new HashMap<>();
        err.put("message", message != null ? message : "Unknown error");
        if (type != null) err.put("type", type);
        if (code != null) err.put("code", code);
        return Map.of("error", err);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidApiKey(InvalidApiKeyException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(openAIError(e.getMessage(), "invalid_request_error", "invalid_api_key"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        String detail = cause != null ? cause.getMessage() : e.getMessage();
        return ResponseEntity.badRequest()
                .body(openAIError("Invalid request body: " + (detail != null ? detail : "parse error"), "invalid_request_error", "parse_error"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException e) {
        String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(openAIError("Data conflict: " + (msg != null ? msg : "constraint violation"), "invalid_request_error", null));
    }

    @ExceptionHandler(JpaObjectRetrievalFailureException.class)
    public ResponseEntity<Map<String, Object>> handleJpaNotFound(JpaObjectRetrievalFailureException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(openAIError("Referenced entity not found: " + (e.getMessage() != null ? e.getMessage() : ""), "invalid_request_error", null));
    }

    @ExceptionHandler(RouterService.RouterException.class)
    public ResponseEntity<Map<String, Object>> handleRouterException(RouterService.RouterException e) {
        Map<String, Object> err = new HashMap<>();
        err.put("message", e.getMessage());
        err.put("type", "api_error");
        err.put("code", "model_request_failed");
        Map<String, Object> body = new HashMap<>();
        body.put("error", err);
        body.put("router_log", e.getAttempts());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        if (msg.contains("All models failed") || msg.contains("No models configured for route")
                || msg.contains("No available model")) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(openAIError(msg, "api_error", "model_request_failed"));
        }
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(openAIError(msg, "internal_server_error", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(openAIError(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), "internal_server_error", null));
    }
}
