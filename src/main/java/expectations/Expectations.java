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
import common.Util;
import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;
import model.Model;
import model.ModelProvider;

/**
 *
 * @author 802996013
 */
public class Expectations {

    private List<Expectation> expectations = new ArrayList<>();
    private String[] paths;
    private boolean logProperties;

    public List<Expectation> getExpectations() {
        return expectations;
    }

    public void setExpectations(List<Expectation> expectations) {
        this.expectations = expectations;
    }

    public String[] getPaths() {
        return paths;
    }


    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isLogProperies() {
        return logProperties;
    }

    public void setLogProperties(boolean logProperties) {
        this.logProperties = logProperties;
    }

    public Expectations addExpectation(Expectation expectation) {
        expectations.add(expectation);
        ExpectationManager.testExpectations(this);
        return this;
    }
    
    public Expectations addExpectation(int index, Expectation expectation) {
        expectations.add(index, expectation);
        ExpectationManager.testExpectations(this);
        return this;
    }

    public static Expectations fromString(String json) {
        Expectations ex = (Expectations) JsonUtils.beanFromJson(Expectations.class, json);
        ExpectationManager.testExpectations(ex);
        return ex;
    }

    public static Expectations fromFile(String fileName) {
        return fromString(Util.readFile(fileName).getContent());
    }

    @JsonIgnore
    public int size() {
        if (expectations == null) {
            expectations = new ArrayList<>();
        }
        return expectations.size();
    }

}
