package mockservice.exception;

public class ErrorResponse {
    private String errorCode;
    private String description;
    private Object detail;
    private Object data;

    public static ErrorResponse getErrorResponse(String errorCode, String description, Object detail, Object data) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode);
        errorResponse.setDescription(description);
        errorResponse.setDetail(detail);
        errorResponse.setData(data);
        return errorResponse;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getDetail() {
        return detail;
    }

    public void setDetail(Object detail) {
        this.detail = detail;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
