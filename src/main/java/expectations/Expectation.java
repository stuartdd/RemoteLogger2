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
package expectations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import model.Model;

public class Expectation implements Model {

    private String name;
    private String method;
    private String path;
    private String bodyType;
    private Map<String, String> asserts;

    @JsonIgnore
    private Map<String, StringMatcher> stringMatchers;

    private ResponseContent response;
    private ForwardContent forward;

    @JsonIgnore
    private MultiStringMatch multiPathMatcher;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.multiPathMatcher = new MultiStringMatch(path, '/');
    }

    public boolean multiPathMatch(Object path) {
        return multiPathMatcher.match(path);
    }

    public Map<String, String> getAsserts() {
        return asserts;
    }

    public void setAsserts(Map<String, String> asserts) {
        this.asserts = asserts;
        this.stringMatchers = mapAssertsToStringMatchers(asserts);
    }

    public boolean assertMatch(String key, String value) {
        StringMatcher sm = stringMatchers.get(key);
        if (sm == null) {
            return false;
        }
        return sm.match(value);
    }

    public ResponseContent getResponse() {
        return response;
    }

    public void setResponse(ResponseContent response) {
        this.response = response;
    }

    public ForwardContent getForward() {
        return forward;
    }

    public void setForward(ForwardContent forward) {
        this.forward = forward;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    @Override
    public String toString() {
        return "Expectation{name='" + (name == null ? "Undefined" : name) + "', method=" + (method == null ? "Undefined" : method) + ", path=" + (path == null ? "Undefined" : path) + ", response=" + (response == null ? "Undefined" : response) + ", forward=" + (forward == null ? "Undefined" : forward) + '}';
    }

    private Map<String, StringMatcher> mapAssertsToStringMatchers(Map<String, String> stringAsserts) {
        Map<String, StringMatcher> map = new HashMap<>();
        for (Map.Entry<String, String> e : stringAsserts.entrySet()) {
            map.put(e.getKey(), new StringMatcher(e.getValue()));
        }
        return map;
    }

}
