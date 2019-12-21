package common;

import java.beans.BeanProperty;

public class ConfigSettingsDummy implements PropertyDataWithAnnotations {
    private String packagedRequestsFile;
    private String selectedPackagedRequestName;
    private String timeFormat;
    private int defaultPort;
    private boolean includeHeaders = true;
    private boolean includeBody = true;

    private ConfigData configData;

    public ConfigSettingsDummy(ConfigData configData) {
        this.configData = configData;
        this.packagedRequestsFile = configData.getPackagedRequestsFile();
        this.selectedPackagedRequestName = configData.getSelectedPackagedRequestName();
        this.timeFormat = configData.getTimeFormat();
        this.defaultPort = configData.getDefaultPort();
        this.includeHeaders = configData.isIncludeHeaders();
        this.includeBody = configData.isIncludeBody();
    }

    public void commit() {
        configData.setPackagedRequestsFile(this.packagedRequestsFile);
        configData.setSelectedPackagedRequestName(this.selectedPackagedRequestName);
        configData.setTimeFormat(this.timeFormat);
        configData.setDefaultPort(this.defaultPort);
        configData.setIncludeHeaders(this.includeHeaders); 
        configData.setIncludeBody(this.includeBody);
    }
    
    @BeanProperty(description = "Packaged Requests File | validation=pak,type=file,desc=Json Expectation File,ext=json")
    public String getPackagedRequestsFile() {
        return packagedRequestsFile;
    }

    public void setPackagedRequestsFile(String packagedRequestsFile) {
        this.packagedRequestsFile = packagedRequestsFile;
    }

    @BeanProperty(description = "Packaged Requests Name")
    public String getSelectedPackagedRequestName() {
        return selectedPackagedRequestName;
    }

    public void setSelectedPackagedRequestName(String selectedPackagedRequestName) {
        this.selectedPackagedRequestName = selectedPackagedRequestName;
    }

    @BeanProperty(description = "Log Date Time format (eg HH:mm:ss.SSS)")
    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @BeanProperty(description = "Default Port | min=1, max=999999 ")
    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    @BeanProperty(description = "Include header data in logs")
    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public void setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
    }

    @BeanProperty(description = "Include http body data in logs")
    public boolean isIncludeBody() {
        return includeBody;
    }

    public void setIncludeBody(boolean includeBody) {
        this.includeBody = includeBody;
    }

}
