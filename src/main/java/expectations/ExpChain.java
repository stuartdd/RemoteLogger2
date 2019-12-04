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

import java.util.HashMap;
import java.util.Map;

public class ExpChain {
    Expectation expectation = new Expectation();
    private static int nameCounter = 0;
    
    public ExpChain() {
        expectation.setName("Exp:"+nameCounter++);
        expectation.setMethod(null);
        expectation.setBodyType(null);
        expectation.setForward(null);
        expectation.setPath(null);
        expectation.setResponse(null);
        expectation.setAsserts(new HashMap<>());
    }


    public Expectation getExpectation() {
        return expectation;
    }
    
    public ExpChain withPath(String path) {
        expectation.setPath(path);
        return this;
    }
    
    public ExpChain withResponse(ResChain res) {
        expectation.setResponse(res.responseContent);
        return this;
    }
    
    public ExpChain withGetMethod() {
        expectation.setMethod("GET");
        return this;
    }
    
    public ExpChain withPostMethod() {
        expectation.setMethod("POST");
        return this;
    }
    
    public ExpChain withPutMethod() {
        expectation.setMethod("PUT");
        return this;
    }
    
    public ExpChain withPatchMethod() {
        expectation.setMethod("PATCH");
        return this;
    }

    public ExpChain withDeleteMethod() {
        expectation.setMethod("DELETE");
        return this;
    }

    public ExpChain withEmptyBody() {
        expectation.setBodyType("EMPTY");
        return this;
    }
    public ExpChain withAnyBody() {
        expectation.setBodyType(null);
        return this;
    }

    public ExpChain withJsonBody() {
        expectation.setBodyType("JSON");
        return this;
    }

    public ExpChain withXmlBody() {
        expectation.setBodyType("XML");
        return this;
    }

    public ExpChain withAnyMethod() {
        expectation.setBodyType(null);
        return this;       
    }

    public ExpChain withName(String name) {
        expectation.setName(name);
        return this;   
    }

    /**
     * Add an assert. Must use setAsserts to ensure String Matchers are set up correctly
     * @param key The key value required in the map.
     * @param value The value from the map
     * @return The chain.
     */
    public ExpChain withProperty(String key, String value) {
        Map<String, String> asserts = expectation.getAsserts();
        asserts.put(key, value);
        expectation.setAsserts(asserts);
        return this;
    }
}
