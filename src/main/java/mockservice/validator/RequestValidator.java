package mockservice.validator;

import mockservice.domain.API;
import mockservice.service.impl.APIServiceImpl;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mockservice.exception.ExceptionRepository.*;

public class RequestValidator {
    private RequestParamsValidator requestParamsValidator;
    private RequestBodyValidator requestBodyValidator;

    public RequestParamsValidator getRequestParamsValidator() {
        return requestParamsValidator;
    }

    public void setRequestParamsValidator(RequestParamsValidator requestParamsValidator) {
        this.requestParamsValidator = requestParamsValidator;
    }

    public RequestBodyValidator getRequestBodyValidator() {
        return requestBodyValidator;
    }

    public void setRequestBodyValidator(RequestBodyValidator requestBodyValidator) {
        this.requestBodyValidator = requestBodyValidator;
    }

    public void validateRequestBody(Map<String, Object> body) {
        if(requestBodyValidator != null) {
            requestBodyValidator.exec(body);
        }
    }

    public void validateRequestParams(Map<String, Object> params) {
        requestParamsValidator.exec(params);
    }

    public static RequestValidator validAndRegularApi(API api) {
        Optional.ofNullable(api).orElseThrow(API_BAD_REQUEST::getException);

        if (api.getId() == null) {
            api.setId(APIServiceImpl.getAPIID());
        }

        String pattern = Optional.ofNullable(api.getPattern())
                .map(String::trim)
                .map(s -> s.replaceAll("/+", "/"))
                .map(s -> s.startsWith("/") ? s : "/" + s)
                .map(s -> s.length() > 1 && s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .orElseThrow(API_MISS_PATTERN::getException);
        api.setPattern(pattern);

        String method = Optional.ofNullable(api.getMethod())
                .map(String::trim)
                .map(String::toUpperCase)
                .orElseThrow(API_MISS_METHOD::getException);
        api.setMethod(method);

        String name = Optional.ofNullable(api.getName()).orElse(pattern);
        api.setName(name);

        api.setParams(Optional.ofNullable(api.getParams())
                .map(params -> {
                    Set<String> paramSet = Stream.of(params)
                            .map(String::trim)
                            .filter(obj -> !obj.isEmpty())
                            .collect(Collectors.toSet());
                    if (paramSet.size() != params.length) {
                        throw API_INVALID_PARAMS.getException();
                    }
                    for (int i = 0; i < params.length; i++) {
                        params[i] = params[i].trim();
                    }
                    return params;
                })
                .orElse(new String[0])
        );


        api.setParamTypes(Optional.ofNullable(api.getParamTypes())
                .map(types -> {
                    List<String> typeList = Stream.of(types)
                            .map(String::trim)
                            .filter(obj -> !obj.isEmpty())
                            .map(String::toLowerCase)
                            .filter(s -> s.equals("string") || s.equals("number") || s.equals("bool") || s.equals("date"))
                            .collect(Collectors.toList());
                    if (typeList.size() != types.length || typeList.size() != api.getParams().length && api.getParams().length > 0) {
                        throw API_INVALID_PARAM_TYPES.getException();
                    }

                    for (int i = 0; i < types.length; i++) {
                        types[i] = typeList.get(i);
                    }
                    return types;
                })
                .orElse(new String[0])
        );

        api.setHeaders(Optional.ofNullable(api.getHeaders())
                .map(headers -> {
                    List<String> list = Stream.of(headers)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .filter(s -> Pattern.matches("\\w+:[^:]+", s))
                            .collect(Collectors.toList());
                    if (list.size() != headers.length) {
                        throw API_INVALID_HEADERS.getException();
                    }
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = list.get(i);
                    }
                    return headers;
                })
                .orElse(new String[0])
        );

        api.setConsumes(Optional.ofNullable(api.getConsumes()).orElse(new String[0]));
        api.setProduces(Optional.ofNullable(api.getProduces()).orElse(new String[0]));
        Long latency = api.getLatency();
        if (latency != null && latency < 0) {
            throw API_INVALID_LATENCY.getException();
        }

        RequestValidator requestValidator = new RequestValidator();

        try {
            RequestParamsValidator requestParamsValidator = new RequestParamsValidator();
            requestParamsValidator.configuration(String.join(",", api.getParamTypes()));
            requestValidator.setRequestParamsValidator(requestParamsValidator);
        } catch (ParseException e) {
            throw API_INVALID_PARAM_TYPES.getException(e);
        }

        if (api.getBodyRule() != null) {
            try {
                RequestBodyValidator requestBodyValidator = new RequestBodyValidator();
                requestBodyValidator.configuration(api.getBodyRule());
                requestValidator.setRequestBodyValidator(requestBodyValidator);
            } catch (Exception e) {
                throw API_INVALID_REQUEST_BODY_RULE.getException(e);
            }
        }
        return requestValidator;
    }
}
