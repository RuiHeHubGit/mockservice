package mockservice.utils;

import com.aliyuncs.iot.model.v20180120.SetDevicePropertyResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import mockservice.domain.InvokePropertyVo;
import mockservice.exception.ExceptionRepository;
import mockservice.service.impl.Oauth2ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UtilsForJavaScript {
    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    private final static Logger logger = LoggerFactory.getLogger(UtilsForJavaScript.class);

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String base64Encode(String str) {
        if(str == null) {
            return null;
        }
        return new String(Base64.getEncoder().encode(str.getBytes()));
    }

    public String base64Decode(String str) {
        if(str == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(str.getBytes()));
    }

    public String utc() {
        ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
        return utc.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public ResponseEntity<String> sendRequest(String url) {
        return sendRequest(url, HttpMethod.GET.toString());
    }

    public ResponseEntity<String> sendRequest(String url, String method) {
        return sendRequest(url, method, null);
    }

    public ResponseEntity<String> sendRequest(String url, String method, Object requestBody) {
        return sendRequest(url, method, requestBody, null);
    }

    public ResponseEntity<String> sendRequest(String url, String method, Object requestBody, Object headers) {
        HttpMethod httpMethod;
        if (url == null || method == null || (httpMethod = HttpMethod.resolve(method.toUpperCase())) == null) {
            throw ExceptionRepository.HTTP_REQUEST_INVALID_PARAMS.getException();
        }

        logger.info("{} {} \r\n body: {}", method, url, requestBody);

        String body = serializationBody(requestBody);

        MultiValueMap<String, String> headersMap = buildHttpHeader(headers);

        if (body.startsWith("{") && body.endsWith("}")) {
            headersMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        } else if (body.startsWith("<") && body.endsWith(">")) {
            headersMap.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        }

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headersMap);
        Type responseType = String.class;
        RequestCallback requestCallback = restTemplate.httpEntityCallback(requestEntity, responseType);
        ResponseExtractor<ResponseEntity<String>> responseExtractor = restTemplate.responseEntityExtractor(responseType);
        return restTemplate.execute(url, httpMethod, requestCallback, responseExtractor);
    }

    private String serializationBody(Object requestBody) {
        String body = "";
        if (requestBody != null) {
            if (requestBody instanceof String) {
                body = String.valueOf(requestBody).trim();
            } else {
                try {
                    body = mapper.writeValueAsString(requestBody);
                } catch (JsonProcessingException e) {
                    throw ExceptionRepository.HTTP_REQUEST_INVALID_BODY.getException();
                }
            }
        }
        return body;
    }

    private MultiValueMap<String, String> buildHttpHeader(Object headers) {
        MultiValueMap<String, String> headersMap = new HttpHeaders();
        if (headers != null) {
            if (headers instanceof String) {
                try {
                    headers = mapper.readValue(String.valueOf(headers),
                            mapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class));
                } catch (JsonProcessingException e) {
                    throw ExceptionRepository.HTTP_REQUEST_INVALID_PARAMS.getException();
                }
            }

            if (headers instanceof Map) {
                Map<Object, Object> map = Map.class.cast(headers);
                for (Map.Entry<Object, Object> e : map.entrySet()) {
                    String[] values = String.valueOf(e.getValue()).split(";");
                    headersMap.put(String.valueOf(e.getKey()), Arrays.asList(values));
                }
            }
        }
        return headersMap;
    }

    public boolean setDeviceProperty(String iotId, String property, Integer value) {
        ObjectMapper mapper = new JsonMapper();
        AliIotUtil aliIotUtil = new AliIotUtil();
        InvokePropertyVo invokePropertyVo = new InvokePropertyVo();
        invokePropertyVo.setIotId(iotId);
        Map<String, Object> map = new HashMap<>();
        map.put(property, value);
        try {
            invokePropertyVo.setItems(mapper.writeValueAsString(map));
            SetDevicePropertyResponse response = aliIotUtil.setDeviceProperties(invokePropertyVo);
            logger.info("response = {}", mapper.writeValueAsString(response));
            return response.getSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}
