package mockservice.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

import static mockservice.exception.ExceptionRepository.API_BAD_REQUEST;

@ControllerAdvice
public class GlobExceptionHandler {
    protected final Log logger = LogFactory.getLog(this.getClass());

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ErrorResponse exceptionHandler(Exception e, HttpServletResponse res) {
        e.printStackTrace();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String message = e.getMessage();
        if(message == null) {
            message = "UNKNOWN ERROR";
        } else {
            int end = 0;
            if((end = message.indexOf(":")) > 0) {
                message = message.substring(0, end);
            }
        }
        logger.error(e.getMessage());
        return ErrorResponse.getErrorResponse(API_BAD_REQUEST.errorCode,
                API_BAD_REQUEST.description, message, null);
    }

    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public ErrorResponse baseExceptionHandler(BaseException e, HttpServletResponse res) {
        e.printStackTrace();
        res.setStatus(e.getHttpStatus());
        return ErrorResponse.getErrorResponse(e.getErrorCode(), e.getDescription(), e.getDetail(), null);
    }
}
