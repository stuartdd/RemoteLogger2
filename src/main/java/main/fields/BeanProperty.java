package main.fields;

import java.util.HashMap;
import java.util.Map;

public class BeanProperty {
    private final String propertyName;
    private final Object initialValue;
    private final String description;
    private final Map<String, String> flags;
    private final Class parameterType;
    private Object updatedValue;

    /**
     * <pre>
     * A single property on a single bean instance.
     *
     * A property must have a:
     * <code>
     *     @BeanProperty(description = "Some text")
     * </code>
     * </pre>
     *
     * @param propertyName  The property name without the set or ger or is prefix
     * @param intialValue   The initial value of the property.
     * @param description   The description (from @BeanProperty)
     * @param parameterType The property type
     */
    public BeanProperty(String propertyName, Object intialValue, String description, Class parameterType) {
        this.propertyName = propertyName;
        this.initialValue = intialValue;
        this.updatedValue = null;
        this.flags = new HashMap<>();
        if (description != null) {
            int pos = description.indexOf('|');
            if (pos > 0) {
                this.description = description.substring(0, pos);
                String remainder = description.substring(pos + 1).trim();
                if (remainder.length() > 0) {
                    String[] csv = remainder.split("\\,");
                    for (String v : csv) {
                        int equals = v.indexOf('=');
                        if (equals > 0) {
                            flags.put(v.substring(0, equals).trim().toLowerCase(), v.substring(equals + 1));
                        }
                    }
                }
            } else {
                this.description = description;
            }
        } else {
            this.description = null;
        }
        this.parameterType = parameterType;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Map<String, String> getFlags() {
        return flags;
    }


    public String getType() {
        return getFlag("type", "");
    }


    public String getValidationId() {
        return getFlag("validation", "");
    }

    public boolean isValidationId(String iid) {
        return getValidationId().equals(iid);
    }

    public boolean isTypeId(String iid) {
        return getType().equals(iid);
    }

    public int getIntFlag(String name, int defaultValue) {
        String v = getFlag(name, null);
        if (v == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return defaultValue;
        }
    }

    public Object getUpdatedValue() {
         return updatedValue;
    }

    public boolean isUpdated() {
        return (updatedValue != null);
    }

    public void setUpdatedValue(Object newValue) {
        if (initialValue == newValue) {
            updatedValue = null;
        } else {
            if ((initialValue!=null) && (newValue!=null) && (initialValue.toString().equals(newValue.toString()))) {
                updatedValue = null;
            } else {
                this.updatedValue = newValue;
            }
        }
    }

    public Object getInitialValue() {
        return initialValue;
    }

    public String getFlag(String name, String defaultValue) {
        String v = getFlags().get(name);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    public String getDescription() {
        if (description == null) {
            return null;
        }
        return description.trim();
    }

}
