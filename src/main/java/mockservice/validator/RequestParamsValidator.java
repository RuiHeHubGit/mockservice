package mockservice.validator;

import java.text.ParseException;
import java.util.Map;

public class RequestParamsValidator implements Validator<Map<String, Object>> {
    @Override
    public Validator<Map<String, Object>> configuration(String expression) throws ParseException {
        return null;
    }

    @Override
    public void exec(Map<String, Object> value) {

    }
}
