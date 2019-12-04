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
public class Expectations implements ModelProvider {

    private List<Expectation> expectations = new ArrayList<>();
    private String[] paths;
    private boolean logProperies;

    public List<Expectation> getExpectations() {
        return expectations;
    }

    public void setExpectations(List<Expectation> expectations) {
        this.expectations = expectations;
    }

    public String[] getPaths() {
        return paths;
    }

    @JsonIgnore
    public int size() {
        if (expectations == null) {
            expectations = new ArrayList<>();
        }
        return expectations.size();
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isLogProperies() {
        return logProperies;
    }

    public void setLogProperies(boolean logProperies) {
        this.logProperies = logProperies;
    }

    public Expectations withLogProperies(boolean listMap) {
        this.setLogProperies(listMap);
        return this;
    }

    public Expectations withPaths(String[] paths) {
        this.setPaths(paths);
        return this;
    }

    public Expectations addExpectation(String json) {
        return addExpectation((Expectation) JsonUtils.beanFromJson(Expectation.class, json));
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

    public static Expectations newExpectation(String json) {
        Expectations expectations = new Expectations();
        expectations.addExpectation(json);
        ExpectationManager.testExpectations(expectations);
        return expectations;
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
    @Override
    public int getModelIndex(String modelName) {
        for (int i=0; i<size(); i++) {
            if (expectations.get(i).getName().equals(modelName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addModel(Model model) {
        expectations.add((Expectation) model);
    }

    @Override
    public void addModel(int pos, Model model) {
        expectations.add(pos, (Expectation) model);
    }

    @JsonIgnore
    @Override
    public Model getModel(int index) {
        return expectations.get(index);
    }

    @JsonIgnore
    @Override
    public Model getModel(String modelName) {
        for (Expectation exp : expectations) {
            if (exp.getName().equals(modelName)) {
                return exp;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean deleteModel(String modelName) {
        boolean deleted = false;
        List<Expectation> expList = new ArrayList<>();
        for (Expectation exp : expectations) {
            if (!exp.getName().equals(modelName)) {
                expList.add(exp);
            } else {
                deleted = true;
            }
        }
        expectations = expList;
        return deleted;
    }

    public synchronized void removeAll() {
        expectations.clear();
    }
    
    @Override
    public synchronized boolean replaceModel(String withThisModelJson) {
        Expectation exp = (Expectation) JsonUtils.beanFromJson(Expectations.class, withThisModelJson);
        return replaceModel(exp.getName(), exp);
    }
    
    @Override
    public synchronized boolean replaceModel(String replaceModelName, Model withThisModel) {
        boolean replaced = false;
        List<Expectation> expList = new ArrayList<>();
        for (Expectation exp : expectations) {
            if (!exp.getName().equals(replaceModelName)) {
                expList.add(exp);
            } else {
                replaced = true;
                expList.add((Expectation) withThisModel);
            }
        }
        expectations = expList;
        return replaced;
    }


}
