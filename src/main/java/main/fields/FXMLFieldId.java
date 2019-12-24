package main.fields;

public class FXMLFieldId {
    private final String stringId;
    private final int intId;

    public FXMLFieldId(String stringId) {
        this.stringId = stringId;
        this.intId = Integer.MAX_VALUE;
    }
    public FXMLFieldId(int intId) {
        this.stringId = ""+intId;
        this.intId = intId;
    }

    public String getStringId() {
        return stringId;
    }

    public int getIntId() {
        return intId;
    }

    public String getKey() {
            return stringId;
    }
}
