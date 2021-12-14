package mockservice.exception;

import org.springframework.http.HttpStatus;

public enum ExceptionRepository {
    LOGIN_FAILED("40000", "user name or password is invalid", HttpStatus.UNAUTHORIZED.value()),
    API_BAD_REQUEST("50000", "bad request"),
    API_MISS_PATTERN("50001", "pattern must not empty"),
    API_MISS_METHOD("50006", "method must not empty"),
    API_INVALID_PARAMS("50011", "duplicate param of invalid param exist"),
    API_INVALID_PARAM_TYPES("50016", "invalid param types"),
    API_INVALID_HEADERS("50021", "invalid headers"),
    API_INVALID_LATENCY("50026", "invalid latency, latency must greater than or equal to zero or null"),
    API_ADD_FAILED("50028", "add failed, an API with the same conditions already exists"),
    API_NOT_FOUNT("50031", "not fount"),
    API_DELETES_SOME_FAILED("50034", "some items failed to delete"),
    API_UNREGISTER_FAILED("50036", "unregister mapping failed"),
    API_INVALID_REQUEST_BODY_RULE("50041", "invalid rule for request body"),
    API_INVALID_RESPONSE_SCRIPT("50044", "invalid script for response"),
    INVALID_REQUEST_PARAMS("50046", "invalid request params"),
    INVALID_REQUEST_BODY("50051", "invalid request body"),
    UNAUTHORIZED("50053", "Unauthorized", HttpStatus.UNAUTHORIZED.value()),
    OAUTH_NOT_SUPPORT_AUTH_TYPE("50056", "not supported auth type"),
    OAUTH_MISSING_OR_INVALID_STATE("50058", "state is missing or invalid"),
    OAUTH_INVALID_CODE("50061", "invalid code"),
    OAUTH_INVALID_REFRESH_TOKEN("50066", "invalid refresh token"),
    OAUTH_REFRESH_TOKEN_EXPIRED("50068", "refresh token is expired", HttpStatus.UNAUTHORIZED.value()),
    OAUTH_INVALID_CLIENT_SECRET("50071", "invalid client secret"),
    OAUTH_INVALID_CLIENT_ID("500673", "invalid client id"),
    OAUTH_INVALID_REDIRECT_URI("500676", "invalid redirect uri"),
    OAUTH_INVALID_SCOPE("500678", "invalid scope"),
    OAUTH_AUTH_FAILED("500681", "auth failed", HttpStatus.UNAUTHORIZED.value()),
    OAUTH_INVALID_TOKEN("500686", "invalid token", HttpStatus.UNAUTHORIZED.value()),
    OAUTH_CODE_EXPIRED("500691", "code is expired", HttpStatus.UNAUTHORIZED.value()),
    OAUTH_TOKEN_EXPIRED("500691", "token is expired", HttpStatus.UNAUTHORIZED.value()),
    HTTP_REQUEST_INVALID_PARAMS("500696", "http request invalid params"),
    HTTP_REQUEST_INVALID_BODY("500701", "http request invalid body");

    final String errorCode;
    final String description;
    final int httpStatus;

    ExceptionRepository(String errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
        this.httpStatus = HttpStatus.BAD_REQUEST.value();
    }

    ExceptionRepository(String errorCode, String description, int httpStatus) {
        this.errorCode = errorCode;
        this.description = description;
        this.httpStatus = httpStatus;
    }

    public BaseException getException(Throwable t) {
        return new BaseException(t, errorCode, description, httpStatus);
    }

    public BaseException getException() {
        return new BaseException(errorCode, description, httpStatus);
    }

    public BaseException getException(String description, Object data) {
        return new BaseException(errorCode, this.description,  description, data);
    }

    public BaseException getException(Object data) {
        return new BaseException(errorCode, description, data);
    }
}
