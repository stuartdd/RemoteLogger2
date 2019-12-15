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

import client.Client;
import client.ClientConfig;
import client.ClientResponse;
import com.sun.net.httpserver.HttpExchange;
import common.CommonLogger;
import common.ExpectationException;
import common.FileException;
import common.LogLine;
import common.Notifier;
import common.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import json.JsonUtils;
import mockServer.MockResponse;
import server.ServerStatistics;
import template.Template;

/**
 *
 * @author 802996013
 */
public class ExpectationManager {

    private static final String EXAMPLE_JSON = "{\n"
            + "  \"name\" : \"Unique Name for Expectation\",\n"
            + "  \"method\" : \"[4] Method Equals: GET, POST, PUT, DELETE etc.\",\n"
            + "  \"path\" : \"[1] Matching Path: E.g. /test/post/xml\",\n"
            + "  \"bodyType\" : \"[4] Body Type Equals: From: XML, JSON, HTML, TXT, EMPTY\",\n"
            + "  \"asserts\" : {\n"
            + "    \"Name of property 1\" : \"[1] Value of property 1 must match\",\n"
            + "    \"Name of property 2\" : \"[1] Value of property 2 must match\"\n"
            + "  },\n"
            + "  \"response\" : {\n"
            + "    \"body\" : \"[3] Response body text\",\n"
            + "    \"status\" : 200,\n"
            + "    \"bodyTemplate\" : \"[2] [3] File or Resource to read response body from\",\n"
            + "    \"headers\" : { "
            + "         \"Accept\": \"%{HEAD.Accept}\",\n"
            + "         \"Name of Header 2\" : \"[3] Value of Header 2\"\n"
            + "    }\n"
            + "  }, \"forward\" : {\n"
            + "    \"host\" : \"http://localhost\",\n"
            + "    \"path\" : null,\n"
            + "    \"port\" : 5003,\n"
            + "    \"method\" : null,\n"
            + "    \"body\" : null,\n"
            + "    \"bodyTemplate\" : null,\n"
            + "    \"headers\" : null\n"
            + "  }"
            + "}";

    private String F = "\"forward\" : {\n"
            + "    \"host\" : \"http://localhost\",\n"
            + "    \"path\" : null,\n"
            + "    \"port\" : 5003,\n"
            + "    \"method\" : null,\n"
            + "    \"body\" : null,\n"
            + "    \"bodyTemplate\" : null,\n"
            + "    \"headers\" : null\n"
            + "  }";
    private static final String BASIC_JSON = "{\n"
            + "  \"name\" : \"Unique Expectation Name\",\n"
            + "  \"method\" : \"GET\",\n"
            + "  \"path\" : \"/test\",\n"
            + "  \"bodyType\" : \"JSON\",\n"
            + "  \"asserts\" : {\n"
            + "    \"P1\" : \"V1\"\n"
            + "  },\n"
            + "  \"response\" : {\n"
            + "    \"body\" : \"{\\\"message\\\":\\\"Message\\\"}\",\n"
            + "    \"status\" : 200,\n"
            + "    \"bodyTemplate\" : null,\n"
            + "    \"headers\" : {\"Accept\": \"%{HEAD.Accept}\"\n"
            + "    }\n"
            + "  }\n"
            + "}";

    private static final String NL = System.getProperty("line.separator");
    private static final String LS = "-----------------------------------------" + NL;
    private Expectations expectations;
    private final int port;
    private final CommonLogger logger;
    private final ServerStatistics serverStatistics;
    private File expectationsFile;
    private long expectationsLoadTime;
    private boolean logProperties;
    private boolean verbose;
    private boolean loadedFromAFile;

    public ExpectationManager(int port, Expectations expectations, CommonLogger logger, ServerStatistics serverStatistics, boolean verbose, boolean logProperties) {
        this.port = port;
        this.logger = logger;
        this.serverStatistics = serverStatistics;
        this.expectations = expectations;
        this.logProperties = logProperties;
        this.verbose = verbose;
        this.loadedFromAFile = false;
        testExpectations(expectations);
    }

    public ExpectationManager(int port, String fileName, CommonLogger logger, ServerStatistics serverStatistics, boolean verbose, boolean logProperties) {
        this.port = port;
        this.logger = logger;
        this.serverStatistics = serverStatistics;
        if ((fileName == null) || (fileName.trim().length() == 0)) {
            expectations = null;
            expectationsFile = null;
            return;
        }
        this.logProperties = logProperties;
        this.verbose = verbose;
        this.loadedFromAFile = false;
        loadExpectations(fileName);
    }

    public Expectations getExpectations() {
        return expectations;
    }

    public int getPort() {
        return port;
    }

    public void setLogProperties(boolean logProperties) {
        this.logProperties = logProperties;
    }

    public void remove(Expectation expectation) {
            expectations.deleteModel(expectation.getName());
    }

    public Expectation replace(Expectation oldExpectation, Expectation newExpectation) {
        if (expectations.replaceModel(oldExpectation.getName(), newExpectation)) {
            return newExpectation;            
        }
        return oldExpectation;
    }

    public static Expectation getExampleExpectation() {
        return (Expectation) JsonUtils.beanFromJson(Expectation.class, EXAMPLE_JSON);
    }

    public static Expectation getBasicExpectationWithName(String name) {
        Expectation exp = (Expectation) JsonUtils.beanFromJson(Expectation.class, BASIC_JSON);
        exp.setName(name);
        return exp;
    }

    public void getResponse(HttpExchange he, Map<String, Object> map, Map<String, String> headers, Map<String, String> queries, Expectation foundExpectation) {
        getResponseData(map, foundExpectation).respond(he, map);
    }

    public MockResponse getResponseData(Map<String, Object> map, Expectation foundExpectation) {
        MockResponse mockResponse = MockResponse.notFound();

        if ((expectations.isLogProperies() || logProperties) && (logger != null)) {
            logMap(System.currentTimeMillis(), map, "REQUEST PROPERTIES");
        }

        if (foundExpectation != null) {
            try {
                serverStatistics.inc(ServerStatistics.STAT.MATCH, true);
                if (logger != null) {
                    logger.log(new LogLine(getPort(), "MATCHED " + foundExpectation));
                }
                if (foundExpectation.getForward() == null) {
                    mockResponse = createMockResponse(foundExpectation, map);
                } else {
                    mockResponse = createMockResponseViaForward(foundExpectation, map);
                }
            } catch (ExpectationException | FileException ee) {
                if (logger != null) {
                    logger.log(new LogLine(getPort(), new IOException("Read file failed for expectation: " + foundExpectation.getName() + ". " + ee.getMessage(), ee)));
                }
            }
        } else {
            serverStatistics.inc(ServerStatistics.STAT.MISSMATCH, true);
            if (logger != null) {
                logger.log(new LogLine(getPort(), "Expectation not met. Returning Not Found (404)"));
            }
        }
        map.put("STATUS", "" + mockResponse.getStatus());
        logResponse(port, mockResponse.getResponseBody(), mockResponse.getStatus(), "RESP");
        return mockResponse;
    }

    public boolean hasNoExpectations() {
        return ((expectations == null) || (expectations.getExpectations().isEmpty()));
    }

    public boolean isLoadedFromAFile() {
        return loadedFromAFile;
    }

    private MockResponse createMockResponseViaForward(Expectation foundExpectation, Map<String, Object> map) {
        ForwardContent forward = foundExpectation.getForward();
        String body = "";
        if (Util.isEmpty(forward.getBodyTemplate())) {
            if (Util.isEmpty(forward.getBody())) {
                body = "";
            } else {
                body = Template.parse(forward.getBody(), map, true);
            }
        } else {
            String templateName = Template.parse(forward.getBodyTemplate(), map, true);
            body = Util.locateResponseFile(templateName, "Expectation", expectations.getPaths(), logger);
        }
        Map<String, String> headers = new HashMap<>();
        if (forward.isForwardHeaders()) {
            for (Map.Entry<String, Object> s : map.entrySet()) {
                if (s.getKey().startsWith("HEAD.")) {
                    headers.put(s.getKey().substring(5), Template.parse(s.getValue().toString(), map, true));
                }
            }
        }
        if (forward.getHeaders() != null) {
            for (Map.Entry<String, String> s : forward.getHeaders().entrySet()) {
                headers.put(s.getKey(), Template.parse(s.getValue(), map, true));
            }
        }
        ClientConfig clientConfig = new ClientConfig(Template.parse(forward.getHost(), map, true), forward.getPort(), headers);
        if (logger != null) {
            logger.log(new LogLine(getPort(), "FORWARDING TO: " + forward.toString()));
        }
        Client client = new Client(clientConfig, logger);
        try {
            ClientResponse resp = client.send(Template.parse(forward.getPath(), map, true), body, forward.getMethod());
            return new MockResponse(resp.getBody(), resp.getStatus(), resp.getHeaders());
        } catch (Exception e) {
            if (logger != null) {
                logger.log(new LogLine(getPort(), e));
            }
            return new MockResponse("Forward failed:" + e.getMessage(), 500, null);
        }
    }

    private MockResponse createMockResponse(Expectation expectation, Map<String, Object> map) {
        String response;
        int statusCode;
        Map<String, String> responseHeaders;
        if (expectation.getResponse() != null) {
            ResponseContent responseContent = expectation.getResponse();
            if (Util.isEmpty(responseContent.getBodyTemplate())) {
                if (Util.isEmpty(responseContent.getBody())) {
                    response = "Body is undefined";
                } else {
                    response = Template.parse(responseContent.getBody(), map, true);
                }
            } else {
                String templateName = Template.parse(responseContent.getBodyTemplate(), map, true);
                response = Util.locateResponseFile(templateName, "Expectation", expectations.getPaths(), logger);
            }
            statusCode = responseContent.getStatus();
            responseHeaders = responseContent.getHeaders();
        } else {
            return MockResponse.undefined(expectation.getMethod());
        }
        return new MockResponse(response, statusCode, responseHeaders);
    }

    public Expectation findMatchingExpectation(Map<String, Object> map) {
        if (expectations == null) {
            if (logger != null) {
                logger.log(new LogLine(getPort(), "No Expectation have been set!"));
            }
            return null;
        }
        reloadExpectations(false);
        Expectation found;
        for (Expectation exp : expectations.getExpectations()) {
            found = testExpectationMatches(exp, map);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Expectation testExpectationMatches(Expectation exp, Map<String, Object> map1) {
        if (doesNotMatchStringOrNullExp(exp.getMethod(), map1.get("METHOD"))) {
            if (logger != null) {
                logger.log(new LogLine(getPort(), "MIS-MATCH:'" + exp.getName() + "' METHOD:'" + exp.getMethod() + "' != '" + map1.get("METHOD") + "'"));
            }
            return null;
        }
        if (doesNotMatchStringOrNullExp(exp.getBodyType(), map1.get("BODY-TYPE"))) {
            if (logger != null) {
                logger.log(new LogLine(getPort(), "MIS-MATCH:'" + exp.getName() + "' BODY-TYPE:'" + exp.getBodyType() + "' != '" + map1.get("BODY-TYPE") + "'"));
            }
            return null;
        }
        if (!exp.multiPathMatch(map1.get("PATH"))) {
            if (logger != null) {
                logger.log(new LogLine(getPort(), "MIS-MATCH:'" + exp.getName() + "' PATH:'" + exp.getPath() + "' != '" + map1.get("PATH") + "'"));
            }
            return null;
        }
        if ((exp.getAsserts() != null) && (!exp.getAsserts().isEmpty())) {
            if (doesNotMatchAllAssertions(exp, map1)) {
                return null;
            }
        }
        return exp;
    }

    private boolean doesNotMatchAllAssertions(Expectation exp, Map<String, Object> map) {
        for (Map.Entry<String, String> ass : exp.getAsserts().entrySet()) {
            Object actual = map.get(ass.getKey());
            if (actual == null) {
                if (logger != null) {
                    logger.log(new LogLine(getPort(), "MIS-MATCH:'" + exp.getName() + "': ASSERT:'" + ass.getKey() + "' Not Found"));
                }
                return true;
            }
            if (!exp.assertMatch(ass.getKey(), actual.toString())) {
                if (logger != null) {
                    logger.log(new LogLine(getPort(), "MIS-MATCH:'" + exp.getName() + "': ASSERT:' " + ass.getKey() + "'='" + ass.getValue() + "'. Does not match '" + actual + "'"));
                }
                return true;
            }
        }
        return false;
    }

    private static boolean doesNotMatchStringOrNullExp(String exp, Object subject) {
        if ((exp == null) || (exp.trim().length() == 0)) {
            return false;
        }
        return doesNotMatchString(exp, subject.toString());
    }

    private static boolean doesNotMatchString(String exp, Object subject) {
        return (!exp.equalsIgnoreCase(subject.toString()));
    }

    private void logMap(long time, Map<String, Object> map, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("START: ------ " + id).append(": ").append(LS);
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey().equals("BODY")) {
                sb.append("-    ").append(e.getKey()).append('=').append("<-- Excluded. See elsewhere in the logs -->").append(NL);
            } else {
                sb.append("-    ").append(e.getKey()).append('=').append(e.getValue()).append(NL);
            }
        }
        sb.append("END: -------- " + id).append(": ").append(LS);
        if (logger != null) {
            logger.log(new LogLine(getPort(), NL + sb.toString().trim()));
        }
    }

    private void logResponse(int port, String resp, int statusCode, String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("-    ").append(id).append(' ').append(LS);
        sb.append("-    ").append(id).append(" From PORT ").append(port).append(" With STATUS:").append(statusCode).append(' ').append(NL);
        sb.append(resp).append(NL);
        sb.append("-    ").append(id).append(' ').append(LS);
        if (logger != null) {
            logger.log(new LogLine(getPort(), NL + sb.toString().trim()));
        }
    }

    public void save() {
        if (expectationsFile != null) {
            String exString = JsonUtils.toJsonFormatted(expectations);

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(expectationsFile);
                fos.write(exString.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                Logger.getLogger(ExpectationManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ExpectationManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private void loadExpectations(String expectationsFileName) {
        File file = new File(expectationsFileName);
        if (file.exists()) {
            expectationsFile = file;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, file);
            expectationsLoadTime = file.lastModified();
            loadedFromAFile = true;
        } else {
            InputStream is = ExpectationManager.class.getResourceAsStream(expectationsFileName);
            if (is == null) {
                is = ExpectationManager.class.getResourceAsStream("/" + expectationsFileName);
                if (is == null) {
                    throw new ExpectationException("Expectations file: " + expectationsFileName + " not found (File or classpath)", 500);
                }
            }
            expectationsFile = null;
            expectations = (Expectations) JsonUtils.beanFromJson(Expectations.class, is);
            expectationsLoadTime = 0;
            loadedFromAFile = false;
        }
        testExpectations(expectations);
    }

    public void reloadExpectations(boolean force) {
        if (loadedFromAFile) {
            if (force || (expectationsFile.lastModified() != expectationsLoadTime)) {
                Expectations temp = (Expectations) JsonUtils.beanFromJson(Expectations.class, expectationsFile);
                try {
                    testExpectations(temp);
                    expectations = temp;
                    expectationsLoadTime = expectationsFile.lastModified();
                } catch (ExpectationException ex) {
                    if (logger != null) {
                        logger.log(new LogLine(getPort(), "Reload of expectation failed " + expectationsFile.getAbsolutePath(), ex));
                    } else {
                        ex.printStackTrace();
                    }
                    expectationsFile = null;
                }
            }
        }
    }

    public static void testExpectations(Expectations expectations) {
        if (expectations == null) {
            throw new ExpectationException("Expectations are null.", 500);
        }
        if (expectations.getExpectations() == null) {
            throw new ExpectationException("Expectations are empty.", 500);
        }
        Map<String, String> map = new HashMap<>();
        for (Expectation e : expectations.getExpectations()) {
            if (map.containsKey(e.getName())) {
                throw new ExpectationException("Duplicate Expectation name found: " + e.getName(), 500);
            }
            if (e.getAsserts() == null) {
                e.setAsserts(new HashMap<>());
            }
            map.put(e.getName(), e.getName());
        }
        if ((expectations.getPaths()==null) || (expectations.getPaths().length == 0)) {
            expectations.setPaths(new String[] {""});
        }
    }

    public void ensureNotEmpty() {
        if (expectations == null) {
            expectations = new Expectations();
        }
        if (expectations.size() == 0) {
            expectations.addExpectation(getBasicExpectationWithName("Temp Name"));
        }
    }

    public void add(Expectation ex) {
        expectations.addExpectation(ex);
    }

    public boolean canNotDelete() {
        return expectations.size() < 2;
    }

    public Expectation get(int selectedIndex) {
        return (Expectation) expectations.getModel(selectedIndex);
    }

    public int size() {
        if (expectations == null) {
            return 0;
        }
        return expectations.size();
    }

    public int findIndexByName(String name) {
        return expectations.getModelIndex(name);
    }

}
