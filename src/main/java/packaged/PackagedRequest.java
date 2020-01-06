/*
 * Copyright (C) 2018 stuartdd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package packaged;

import common.FileData;
import common.PropertyDataWithAnnotations;
import common.Util;
import json.JsonUtils;
import model.Model;
import template.Template;

import java.beans.BeanProperty;
import java.util.HashMap;
import java.util.Map;

public class PackagedRequest implements Model, PropertyDataWithAnnotations {

    private String name;
    private String host;
    private Integer port;
    private String path;
    private String method;
    private String body;
    private String bodyTemplate;
    private Map<String, String> headers;

    public PackagedRequest clone() {
        PackagedRequest clone = new PackagedRequest();
        clone.setHost(getHost());
        clone.setPort(getPort());
        clone.setPath(getPath());
        clone.setMethod(getMethod());
        clone.setBody(getBody());
        clone.setBodyTemplate(getBodyTemplate());
        clone.setHeaders(clone(getHeaders()));
        return clone;
    }

    private Map<String, String> clone(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        HashMap<String, String> clone = new HashMap<>();
        for (Map.Entry<String, String> h : headers.entrySet()) {
            clone.put(h.getKey(), h.getValue());
        }
        return clone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @BeanProperty(description = "Host URL | validation=url")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @BeanProperty(description = "Host Port number | min=10")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @BeanProperty(description = "Request Path (appended to URL)")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @BeanProperty(description = "Request Method")
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @BeanProperty(description = "Request Body (If NO file defined)")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @BeanProperty(description = "Request Body Template (File) | type=file,desc=Request Body Template,ext=json ")
    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String tojSON() {
        return JsonUtils.toJsonFormatted(this);
    }

    public String getBodyFinal(HashMap<String, Object> properties) {
        String bodyText = getBody();
        if ((bodyText == null) || (bodyText.trim().length() == 0)) {
            String fileName = getBodyTemplate();
            if ((fileName == null) || (fileName.trim().length() == 0)) {
                FileData fd = Util.readFile(getBodyTemplate());
                if (fd != null) {
                    bodyText = fd.getContent();
                }
            }
        }
        bodyText = bodyText.trim();
        if (bodyText.length() > 1) {
            if (properties != null) {
                bodyText = Template.parse(bodyText, properties, true);
            }
            bodyText = Template.parse(bodyText, System.getProperties(), true);
        }
        return bodyText;
    }

    @Override
    public String toString() {
        return name;
    }

}
