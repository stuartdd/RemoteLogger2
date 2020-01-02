package server;

import common.CommonLogger;
import common.LogLine;

import java.util.HashMap;
import java.util.Map;

public class ServerExpectationManager {
    private static Map<String, ServerExpectations> expectationsCache = new HashMap<>();

    private ServerExpectationManager() {
    }

    public static ServerExpectations getExpectations(String fileName) {
        ServerExpectations se = expectationsCache.get(fileName);
        if (se == null) {
            throw new ServerConfigException("Could not locate '"+fileName+"' expectations in file cache");
        }
        return se;
    }

    public static void loadExpectation(int port, String fileName, CommonLogger logger) {
        ServerExpectations se = expectationsCache.get(fileName);
        if (se == null) {
            se = new ServerExpectations(fileName, logger);
            expectationsCache.put(fileName, se);
        }
        if ((logger != null) && se.hasNoExpectations()) {
            logger.log(new LogLine(port, "Server on " + port + " does not have any expectations defined. 404 will be returned"));
        }
    }
}
