package main.fields;

import java.util.HashMap;
import java.util.Map;

public class BeanPropertyDescription {
    private final String propertyName;
    private final String description;
    private final String additional;
    private final Map<String, String> flags;
    private final Class parameterType;

    public BeanPropertyDescription(String propertyName, String description, Class parameterType) {
        this.propertyName = propertyName;
        this.flags = new HashMap<>();
        if (description!= null) {
            int pos = description.indexOf('|');
            if (pos > 0) {
                this.description = description.substring(0, pos);
                this.additional = description.substring(pos+1);
                String[] csv = this.additional.split("\\,");
                for (String v:csv) {
                    int equals = v.indexOf('=');
                    if (equals>0) {
                        flags.put(v.substring(0,equals).trim().toLowerCase(), v.substring(equals+1));
                    }
                }
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

    public Map<String, String> getFlags() {
        return flags;
    }

    public boolean isId(String iid) {
        return (getFlag("id", "").equals(iid));
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

    public String getFlag(String name, String defaultValue) {
        String v = getFlags().get(name);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    public String getAdditionalFlags() {
        if (additional == null) {
            return null;
        }
        return additional.trim();
    }

    public String getDescription() {
        if (description == null) {
            return null;
        }
        return description.trim();
    }
}
