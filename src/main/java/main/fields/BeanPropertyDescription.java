package main.fields;

public class BeanPropertyDescription {
    private final String propertyName;
    private final String description;
    private final Class parameterType;

    public BeanPropertyDescription(String propertyName, String description, Class parameterType) {
        this.propertyName = propertyName;
        this.description = description;
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

    public String getDescription() {
        return description;
    }
}
