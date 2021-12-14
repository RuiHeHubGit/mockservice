package mockservice.exception;


public class ValidateException extends RuntimeException {
    private String field;
    private Object value;
    private String description;
    private Detail detail;

    static class Detail {
        private String field;
        private Object value;
        private String description;

        public Detail(String field, Object value, String description) {
            this.field = field;
            this.value = value;
            this.description = description;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public ValidateException(String field, Object value, String description) {
        super(description);
        this.field = field;
        this.value = value;
        this.description = description;
        this.detail = new Detail(field, value, description);
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Detail getDetail() {
        return detail;
    }
}
