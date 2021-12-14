package mockservice.exception;

import org.springframework.http.HttpStatus;

public class BaseException extends RuntimeException {
    private String errorCode;
    private String description;
    private Object detail;
    private int httpStatus = HttpStatus.BAD_REQUEST.value();

    public BaseException(String errorCode, String description) {
        super(description);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BaseException(String errorCode, String description, int httpStatus) {
        super(description);
        this.errorCode = errorCode;
        this.description = description;
        this.httpStatus = httpStatus;
    }

    public BaseException(Throwable t, String errorCode, String description) {
        super(description, t);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BaseException(String errorCode, String description, Object detail) {
        this.errorCode = errorCode;
        this.description = description;
        this.detail = detail;
    }

    public BaseException(String message, String errorCode, String description, Object detail) {
        super(message);
        this.errorCode = errorCode;
        this.description = description;
        this.detail = detail;
    }

    public BaseException(String message, Throwable cause, String errorCode, String description, Object detail) {
        super(message, cause);
        this.errorCode = errorCode;
        this.description = description;
        this.detail = detail;
    }

    public BaseException(Throwable cause, String errorCode, String description, Object detail) {
        super(cause);
        this.errorCode = errorCode;
        this.description = description;
        this.detail = detail;
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String errorCode, String description, Object detail) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
        this.description = description;
        this.detail = detail;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }

    public Object getDetail() {
        return detail;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
