package mockservice.validator;

import mockservice.exception.ValidateException;

import java.text.ParseException;

public class NotNullValidator implements Validator<Object> {


    @Override
    public Validator<Object> configuration(String expression) throws ParseException {
        if (!"notNull".equals(expression)) {
            throw new ParseException("configuration NotNullValidator failed, invalid expression: " + expression, 0);
        }
        return this;
    }

    public static boolean isSupported(String rule) {
        return "notNull".endsWith(rule);
    }

    @Override
    public void exec(Object value) {
        if (value == null) {
            throw new ValidateException("", "", "must not null");
        }
    }
}
