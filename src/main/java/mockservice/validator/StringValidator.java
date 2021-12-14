package mockservice.validator;

import mockservice.exception.ValidateException;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class StringValidator implements Validator<String> {
    private final static Map<String, BiConsumer<String, StringValidator>> parserMap = new HashMap<>();
    private final static String[] rules = {"minLen", "maxLen", "p", "notBlack", "notEmpty"};

    static {
        parserMap.put("minLen", (s, v) -> {
            v.setMinLen(Integer.valueOf(s));
        });
        parserMap.put("maxLen", (s, v) -> {
            v.setMaxLen(Integer.valueOf(s));
        });
        parserMap.put("p", (s, v) -> {
            v.setPattern(Pattern.compile(s));
        });
        parserMap.put("notBlack", (s, v) -> {
            v.setNotBlack(true);
        });
        parserMap.put("notEmpty", (s, v) -> {
            v.setNotEmpty(true);
        });
    }

    private Integer minLen;
    private Integer maxLen;
    private Pattern pattern;
    private boolean notBlack;
    private boolean notEmpty;
    private String expression;

    @Override
    public Validator<String> configuration(String expression) throws ParseException {
        if (expression == null || (expression = expression.trim()).isEmpty()) {
            return this;
        }

        this.expression = expression;

        String[] expressionArr = expression.split(",");

        for (int i = 0; i < expressionArr.length; i++) {
            String[] rule = expressionArr[i].split("=");
            BiConsumer<String, StringValidator> parser = null;
            boolean exception = false;
            if (rule.length < 3) {
                parser = parserMap.get(rule[0]);
                if (parser != null) {
                    try {
                        parser.accept(rule.length == 1 ? "" : rule[1], this);
                    } catch (Exception e) {
                        exception = true;
                    }
                }
            }

            if (parser == null || exception) {
                throw new ParseException("configuration StringValidator failed, invalid expression: " + expressionArr[i], i);
            }
        }
        return this;
    }

    public static boolean isSupported(String rule) {
        for (String r : rules) {
            if(rule.startsWith(r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exec(String value) {
        if (notEmpty) {
            if (value == null || value.isEmpty()) {
                throw new ValidateException("", "", "must not empty");
            }
        }

        if (notBlack) {
            if (value == null || value.trim().isEmpty()) {
                throw new ValidateException("", value, "must not black");
            }
        }

        if (minLen != null) {
            if (value == null || value.length() < minLen) {
                throw new ValidateException("", value, "length must be greater than or equal to " + minLen);
            }
        }

        if (maxLen != null) {
            if (value != null && value.length() > maxLen) {
                throw new ValidateException("", value, "length must be less than or equal to " + maxLen);
            }
        }

        if (pattern != null) {
            if (value == null || pattern.matcher(value).matches()) {
                throw new ValidateException("", value, "cannot match the specified format: " + pattern.pattern());
            }
        }
    }

    public Integer getMinLen() {
        return minLen;
    }

    public void setMinLen(Integer minLen) {
        this.minLen = minLen;
    }

    public Integer getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(Integer maxLen) {
        this.maxLen = maxLen;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public boolean isNotBlack() {
        return notBlack;
    }

    public void setNotBlack(boolean notBlack) {
        this.notBlack = notBlack;
    }

    public boolean isNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public String getExpression() {
        return expression;
    }
}
