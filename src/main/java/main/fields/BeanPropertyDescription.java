package main.fields;

public class BeanPropertyDescription {
    private final String propertyName;
    private final String description;
    private final String additional;
    private final Class parameterType;

    public BeanPropertyDescription(String propertyName, String description, Class parameterType) {
        this.propertyName = propertyName;
        if (description!= null) {
            int pos = description.indexOf('|');
            if (pos > 0) {
                this.description = description.substring(0, pos);
                this.additional = description.substring(pos+1);
            } else {
                this.description = description;
                this.additional = null;
            }
        } else {
            this.description = null;
            this.additional = null;
        }
        this.parameterType = parameterType;
    }

    public boolean isDefined() {
        return description != null;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getAdditional() {
        return additional;
    }

    public String getDescription() {
        return description;
    }
}
