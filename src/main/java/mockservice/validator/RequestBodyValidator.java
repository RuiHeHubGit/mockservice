package mockservice.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import mockservice.exception.ValidateException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RequestBodyValidator implements Validator<Map<String, Object>> {
    static class ValidatorNode {
        String fieldName;
        List<Validator> validators;
        List<ValidatorNode> nodes;
        boolean isObj;
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private ValidatorNode validatorNode;

    @Override
    public Validator<Map<String, Object>> configuration(String expression) throws ParseException {
        if (expression == null) {
            return this;
        }

        ValidatorNode rootValid = new ValidatorNode();
        try {
            Map<String, Object> validatorMap = mapper.readValue(expression, Map.class);
            parser(rootValid, validatorMap);
        } catch (Exception e) {
            throw new ParseException("failed to configuration NotNullValidator, invalid expression: " + e.getMessage(), 0);
        }
        validatorNode = rootValid;
        return this;
    }

    private void parser(ValidatorNode validatorNode, Map<String, Object> validatorMap) throws ParseException {
        if (validatorMap == null) {
            return;
        }
        List<ValidatorNode> nodes = new ArrayList<>();
        Set<String> keys = validatorMap.keySet();
        for (String key : keys) {
            ValidatorNode node = new ValidatorNode();
            node.validators = new ArrayList<>();
            Object value = validatorMap.get(key);

            if (value instanceof Map) {
                if (key.endsWith(".notNull")) {
                    NotNullValidator validator = new NotNullValidator();
                    key = key.substring(0, key.lastIndexOf("."));
                    node.fieldName = key;
                    node.validators.add(validator.configuration("notNull"));
                }
                node.isObj = true;
                parser(node, (Map<String, Object>) value);
            } else {
                String[] rules = value.toString().split(",");
                if (rules.length == 1 && rules[0].trim().isEmpty()) {
                    throw new ParseException("invalid rule", 0);
                }

                node.fieldName = key;

                StringValidator stringValidator = null;
                for (String rule : rules) {
                    if (TypeValidator.isSupported(rule)) {
                        node.validators.add(new TypeValidator().configuration(rule));
                    } else if (StringValidator.isSupported(rule)) {
                        if (stringValidator == null) {
                            stringValidator = new StringValidator();
                        }
                        stringValidator.configuration(rule);
                    } else if (NotNullValidator.isSupported(rule)) {
                        node.validators.add(new NotNullValidator().configuration(rule));
                    }
                }

                if (stringValidator != null) {
                    node.validators.add(stringValidator);
                }
            }
            nodes.add(node);
        }
        validatorNode.nodes = nodes;
    }

    @Override
    public void exec(Map<String, Object> value) {
        validator(validatorNode, value);
    }

    private void validator(ValidatorNode validatorNode, Map<String, Object> map) {
        if (validatorNode == null || validatorNode.nodes == null || validatorNode.nodes.isEmpty()) {
            return;
        }

        if (map == null && validatorNode.validators != null && !validatorNode.validators.isEmpty()) {
            validatorNode.validators.get(0).exec(null);
        }

        if(map == null) {
            throw new ValidateException("body", null, "Validate failed, required request body is missing");
        }

        for (ValidatorNode node : validatorNode.nodes) {
            Object value = map.get(node.fieldName);
            for (Validator validator : node.validators) {
                try {
                    validator.exec(value);
                } catch (Exception e) {
                    if (e instanceof ValidateException) {
                        throw new ValidateException(node.fieldName, value, ((ValidateException) e).getDescription());
                    } else {
                        throw new ValidateException(node.fieldName, validator, "validate failed");
                    }
                }
            }
            if (value instanceof Map && node.isObj) {
                validator(node, (Map<String, Object>) value);
            }
        }
    }
}
