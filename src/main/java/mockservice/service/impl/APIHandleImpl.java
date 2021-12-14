package mockservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import mockservice.domain.API;
import mockservice.exception.ExceptionRepository;
import mockservice.exception.ValidateException;
import mockservice.service.CacheService;
import mockservice.utils.UtilsForJavaScript;
import mockservice.validator.RequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static mockservice.exception.ExceptionRepository.INVALID_REQUEST_BODY;

@Scope("prototype")
@Component
public class APIHandleImpl implements mockservice.service.APIHandle {
    private static final Logger logger = LoggerFactory.getLogger(APIHandleImpl.class);
    private static final Logger javaScriptLogger = LoggerFactory.getLogger("JavaScript");
    private static Method HANDLE_METHOD;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper xmlMapper = new XmlMapper();
    private static final UtilsForJavaScript utilsForJavaScript = new UtilsForJavaScript();

    static {
        try {
            HANDLE_METHOD = APIHandleImpl.class.getMethod("handleMethod",
                    Map.class, Map.class, String.class, HttpServletRequest.class);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Autowired
    private CacheService<Object> cacheService;

    private API api;
    private RequestMappingInfo requestMappingInfo;
    private RequestValidator requestValidator;

    @ResponseBody
    public Object handleMethod(@RequestHeader Map<String, Object> headers,
                               @RequestParam(required = false) Map<String, Object> paramMap,
                               @RequestBody(required = false) String body,
                               HttpServletRequest req) throws InterruptedException {
        return handleRequest(headers, paramMap, body, req);
    }

    private Object handleRequest(Map<String, Object> headers, Map<String, Object> paramMap, String body, HttpServletRequest req) throws InterruptedException {
        Map<String, Object> bodyMap = parseToMap(body);

        try {
            requestValidator.validateRequestBody(bodyMap);
        } catch (ValidateException e) {
            throw INVALID_REQUEST_BODY.getException("field " + e.getField() + " is invalid", e.getDetail());
        }

        Object response = handleResponseContent(api.getResponse(), req, headers, paramMap, bodyMap);

        execLatency();

        return response;
    }

    private Map<String, Object> parseToMap(String str) {
        if(str == null) {
            return null;
        }

        Map<String, Object> map = null;
        str = str.trim();
        ObjectMapper mapper = null;
        if (str.startsWith("{") && str.endsWith("}")) {
            mapper = jsonMapper;
        } else if (str.startsWith("<") && str.endsWith(">")) {
            mapper = xmlMapper;
        }

        if(mapper != null) {
            try {
                map = mapper.readValue(str,
                        mapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class));
            } catch (JsonProcessingException e) {
                throw INVALID_REQUEST_BODY.getException();
            }
        }

        return map;
    }

    private Object handleResponseContent(String response, HttpServletRequest req, Map<String, Object> headers, Map<String, Object> paramMap, Map<String, Object> body) {
        response = Optional.ofNullable(response).orElse("");
        Object result = response;
        try {
            if (response.startsWith("<script>") && response.endsWith("</script>")) {
                result = execScript(response.substring(8).substring(0, response.length() - 17), headers, paramMap, body);
            }
        } finally {
            try {
                String headerLog = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(headers);
                String paramMapLog = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(paramMap);
                String reqBodyLog = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                Object resBodyLog = result;
                if (result instanceof String
                        && ((String) result).trim().startsWith("{") && ((String) result).trim().endsWith("}")) {
                    resBodyLog = jsonMapper.readValue(result.toString(), Map.class);
                }
                resBodyLog = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resBodyLog);
                logger.info("\n\n{} {}\n\nreqHeaders:{}\n\nparams:{}\n\nreqBody:{}\n\nresponseBody:{}",
                        req.getMethod(), req.getRequestURI(), headerLog, paramMapLog, reqBodyLog, resBodyLog);
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    private Object execScript(String script, Map<String, Object> headers, Map<String, Object> paramMap, Map<String, Object> body) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            engine.put("reqHeaders", headers);
            engine.put("reqParams", paramMap);
            engine.put("reqBody", body);
            engine.put("Utils", utilsForJavaScript);
            engine.put("Logger", javaScriptLogger);
            engine.put("Cache", cacheService);
            Object result = engine.eval(script);
            if(!(result instanceof String)) {
                try {
                    engine.put("JavaScriptResult", result);
                    result = engine.eval("JSON.stringify(JavaScriptResult)");
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
            if(result instanceof String) {
                try {
                    Map<String, Object> resultMap = parseToMap((String) result);
                    if(resultMap != null) {
                        result = resultMap;
                    }
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
            }
            return result;
        } catch (Exception e) {
            throw ExceptionRepository.API_INVALID_RESPONSE_SCRIPT.getException(e.getMessage());
        }
    }

    private void execLatency() throws InterruptedException {
        if (api.getLatency() != null && api.getLatency() > 0) {
            TimeUnit.MILLISECONDS.sleep(api.getLatency());
        }
    }

    @Override
    public Method getHandleMethod() {
        return APIHandleImpl.HANDLE_METHOD;
    }

    @Override
    public API getApi() {
        return api;
    }

    @Override
    public void setApi(API api) {
        this.api = api;
    }

    public RequestMappingInfo getRequestMappingInfo() {
        return requestMappingInfo;
    }

    public void setRequestMappingInfo(RequestMappingInfo requestMappingInfo) {
        this.requestMappingInfo = requestMappingInfo;
    }

    public RequestValidator getRequestValidator() {
        return requestValidator;
    }

    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }
}
