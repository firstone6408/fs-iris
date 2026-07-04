package com.iris.backend.util;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Generic HTTP response envelope returned by every endpoint.
 *
 * <pre>
 * {
 *   "ok": true,
 *   "status": 200,
 *   "message": "ok",
 *   "data": { ... },
 *   "timestamp": "2026-07-02T21:00:00"
 * }
 * </pre>
 *
 * <p>Use the static factory methods to build {@link ResponseEntity} instances:
 * <ul>
 *   <li>{@link #success(Object)} — 200 OK with data</li>
 *   <li>{@link #error(HttpStatus, String)} — error with status and message</li>
 *   <li>{@link #of(boolean, HttpStatus, String, Object)} — full control</li>
 * </ul>
 *
 * @param <T> type of the {@code data} payload
 */
public class ApiResponse<T> {

    private boolean ok;
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private ApiResponse(boolean ok, int status, String message, T data) {
        this.ok = ok;
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(boolean ok, HttpStatus httpStatus, String message, T data) {
        return ResponseEntity.status(httpStatus)
                .body(new ApiResponse<>(ok, httpStatus.value(), message, data));
    }

    // --- success ---

    /** 200 OK with data payload. */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return build(true, HttpStatus.OK, "ok", data);
    }

    /** 200 OK with message only, no data. */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return build(true, HttpStatus.OK, message, null);
    }

    /** Custom status with data; message defaults to the status name. */
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus httpStatus, T data) {
        return build(true, httpStatus, httpStatus.name(), data);
    }

    /** Custom status with message, no data. */
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus httpStatus, String message) {
        return build(true, httpStatus, message, null);
    }

    /** 200 OK with message and data. */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return build(true, HttpStatus.OK, message, data);
    }

    /** Custom status with message and data. */
    public static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus httpStatus, String message, T data) {
        return build(true, httpStatus, message, data);
    }

    // --- error ---

    /** Error response with the given HTTP status and message. */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus httpStatus, String message) {
        return build(false, httpStatus, message, null);
    }

    // --- generic builder ---

    /** Full control over all fields. */
    public static <T> ResponseEntity<ApiResponse<T>> of(boolean ok, HttpStatus status, String message, T data) {
        return build(ok, status, message, data);
    }
}
