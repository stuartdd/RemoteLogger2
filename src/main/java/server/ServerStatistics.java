/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author stuart
 */
public class ServerStatistics {

    public enum STAT {
        REQUEST, RESPONSE, MISSMATCH, MATCH
    }
    int requestCount = 0;
    int responseCount = 0;
    int notMatchedCount = 0;
    int matchedCount = 0;

    public synchronized int inc(STAT statToInc, boolean inc) {
        switch (statToInc) {
            case REQUEST:
                if (inc) {
                    requestCount++;
                }
                return requestCount;
            case RESPONSE:
                if (inc) {
                    responseCount++;
                }
                return responseCount;
            case MISSMATCH:
                if (inc) {
                    notMatchedCount++;
                }
                return notMatchedCount;
            case MATCH:
                if (inc) {
                    matchedCount++;
                }
                return matchedCount;
        }
        return requestCount+responseCount+matchedCount+notMatchedCount;
    }

    @Override
    public String toString() {
        return "ServerStatistics{" + "request=" + requestCount + ", response=" + responseCount + ", missmatch=" + notMatchedCount + ", match=" + matchedCount + '}';
    }

}
