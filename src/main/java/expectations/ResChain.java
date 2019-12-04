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

public class ResChain {
    ResponseContent responseContent = new ResponseContent();

    public ResChain() {
        responseContent.setBodyTemplate(null);
        responseContent.setBody(null);
        responseContent.setHeaders(null);
        responseContent.setStatus(0);
    }

    public ResponseContent getResponseContent() {
        return responseContent;
    }
      
    public ResChain withTemplate(String template) {
        responseContent.setBodyTemplate(template);
        return this;
    }
    
    public ResChain withHeader(String key, String value) {
        if (responseContent.getHeaders()==null) {
            Map<String, String> map = new HashMap();
            responseContent.setHeaders(map);
        }
        responseContent.getHeaders().put(key, value);
        return this;
    }

    ResChain withStatus(int status) {
        responseContent.setStatus(status);
        return this;
    }

    ResChain withBody(String body) {
        responseContent.setBody(body);
        return this;
    }

}
