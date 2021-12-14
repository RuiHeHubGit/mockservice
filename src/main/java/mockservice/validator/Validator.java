package mockservice.validator;

import java.text.ParseException;

public interface Validator<T> {
    Validator<T> configuration(String expression) throws ParseException;

    void exec(T value);
}
