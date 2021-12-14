package mockservice.validator;

import mockservice.exception.ValidateException;

import javax.xml.crypto.Data;
import java.text.ParseException;

public class TypeValidator implements Validator<Object> {
    private final static String[] rules = new String[DataType.values().length];
    private DataType dataType;

    static {
        int index = 0;
        for (DataType dataType : DataType.values()) {
            rules[index++] = dataType.toString().toLowerCase();
        }
    }

    @Override
    public Validator<Object> configuration(String expression) throws ParseException {
        try {
            dataType = DataType.valueOf(expression.toUpperCase());
        } catch (Exception e) {
            throw new ParseException("configuration TypeValidator failed, invalid expression: " + expression, 0);
        }
        return this;
    }

    public static boolean isSupported(String rule) {
        for (String r : rules) {
            if (r.equals(rule)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exec(Object value) {

        if (value == null) {
            return;
        }

        if (dataType == DataType.STRING) {
            if (value instanceof String) {
                return;
            }
            throw new ValidateException("", String.valueOf(value), "requirement is string type");
        }

        if (dataType == DataType.LONG) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue >= Long.MIN_VALUE && doubleValue <= Long.MAX_VALUE) {
                    return;
                }
                throw new ValidateException("", String.valueOf(value), "value is outside the range of long");
            }
            throw new ValidateException("", String.valueOf(value), "requirement is long type");
        }

        if (dataType == DataType.DOUBLE) {
            if (value instanceof Number) {
                return;
            }
            throw new ValidateException("", String.valueOf(value), "requirement is double type");
        }

        if (dataType == DataType.FLOAT) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue >= Float.MIN_VALUE && doubleValue <= Float.MAX_VALUE) {
                    return;
                }
                throw new ValidateException("", String.valueOf(value), "value is outside the range of long");
            }
            throw new ValidateException("", String.valueOf(value), "requirement is float type");
        }

        if (dataType == DataType.INTEGER) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue >= Integer.MIN_VALUE && doubleValue <= Integer.MAX_VALUE) {
                    return;
                }
                throw new ValidateException("", String.valueOf(value), "value is outside the range of long");
            }
            throw new ValidateException("", String.valueOf(value), "requirement is integer type");
        }

        if (dataType == DataType.SHORT) {
            if (value instanceof Number) {
                double doubleValue = ((Number) value).doubleValue();
                if (doubleValue >= Short.MIN_VALUE && doubleValue <= Short.MAX_VALUE) {
                    return;
                }
                throw new ValidateException("", String.valueOf(value), "value is outside the range of long");
            }
            throw new ValidateException("", String.valueOf(value), "requirement is short type");
        }

        if (dataType == DataType.BOOLEAN) {
            if (value instanceof Boolean) {
                return;
            }
            throw new ValidateException("", String.valueOf(value), "requirement is boolean type");
        }

        if (dataType == DataType.DATE) {
            if (value instanceof Data) {
                return;
            }
            throw new ValidateException("", String.valueOf(value), "requirement is date type");
        }
    }
}
