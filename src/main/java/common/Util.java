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
package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import json.JsonUtils;
import xml.MappedXml;

public class Util {

    public static final String NL = System.getProperty("line.separator");

    public static String trimmedNull(final Object s) {
        if (s == null) {
            return null;
        }
        String st = s.toString().trim();
        if (st.length() == 0) {
            return null;
        }
        return st;
    }

    public static boolean isEmpty(final Object s) {
        if (s == null) {
            return true;
        }
        String st = s.toString().trim();
        if (st.length() == 0) {
            return true;
        }
        return false;
    }

    public static String firstN(String s, int n) {
        if (Util.isEmpty(s)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= ' ') {
                sb.append(s.charAt(i));
            } else {
                sb.append(' ');
            }
            if (sb.length() >= n) {
                return sb.toString();
            }
        }
        return sb.toString();
    }

    public static String asString(final List<String> list) {
        StringBuilder sb = new StringBuilder();
        int len = 0;
        for (String s : list) {
            sb.append(s);
            len = sb.length();
            sb.append(';');
        }
        sb.setLength(len);
        return sb.toString();
    }

    public static FileData readFile(final String name) {
        File f = new File(name);
        f = new File(f.getAbsolutePath());
        if (f.exists()) {
            try {
                return new FileData(name, true, new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new FileException("File [" + f.getAbsolutePath() + "] could not be read", ex);
            }
        } else {
            return readResource(name);
        }
    }

    public static FileData readResource(final String resourceName) {
        InputStream is = Util.class.getResourceAsStream(resourceName);
        if (is == null) {
            is = Util.class.getResourceAsStream("/" + resourceName);
            if (is == null) {
                throw new ServerException("Resource (or file) [" + resourceName + "] could not be found.", 500);
            }
        }
        return new FileData(resourceName, false, readStream(is));
    }

    public static String readStream(final InputStream iStream) {
        long time = System.currentTimeMillis();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(NL);
            }
            return trimmedNull(sb.toString());
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
            }
        }

    }

    public static BodyType detirmineBodyType(String bodyTrimmed) {
        char cN = bodyTrimmed.charAt(bodyTrimmed.length() - 1);
        if ((bodyTrimmed.charAt(0) == '<') && (cN == '>')) {
            return BodyType.XML;
        }
        if ((cN == '}') && (bodyTrimmed.indexOf(" {") < bodyTrimmed.length() - 1)) {
            return BodyType.JSON;
        }
        return BodyType.TXT;
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public static String cleanString(String in) {
        return cleanString(in, Integer.MAX_VALUE);
    }

    public static String cleanString(String in, int max) {
        if (in == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : in.trim().toCharArray()) {
            if ((c >= ' ') && (c < 127)) {
                sb.append(c);
            }
        }
        if (sb.length() > max) {
            sb.setLength(max);
        }
        return sb.toString();
    }

    public static List<String> split(String s, char delim) {
        StringBuilder sb = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (char c : s.toCharArray()) {
            if (c == delim) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list;
    }

    public static String locateResponseFile(String fileName, String type, String[] paths, Notifier notifier) {
        if (fileName == null) {
            throw new FileException(type + "File for " + type + " is not defined");
        }
        paths = Arrays.copyOf(paths, paths.length + 2);
        paths[paths.length - 2] = System.getProperty("user.dir", "");
        paths[paths.length - 1] = System.getProperty("user.home", "");

        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path.length() > 0) {
                sb.append('"').append(path).append('"').append(',');
                Path p = Paths.get(path, fileName);
                if (Files.exists(p)) {
                    try {
                        if (notifier != null) {
                            notifier.log(new LogLine(-1, type + " File found: " + p.toString()));
                        }
                        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                    } catch (IOException ex) {
                        throw new FileException(type + " File [" + fileName + "] Not readable", ex);
                    }
                } else {
                    if (notifier != null) {
                        notifier.log(new LogLine(-1, type + " File NOT found: " + p.toString()));
                    }
                }
            }
        }
        try {
            return readResource(paths, fileName, type, sb.toString(), notifier);
        } catch (IOException ex) {
            throw new FileException(type + " File [" + fileName + "] Not readable", ex);
        }
    }

    private static String readResource(String[] paths, String file, String type, String list, Notifier notifier) throws IOException {
        for (String path : paths) {
            String resPath = path + '/' + file;
            if (!resPath.startsWith("/")) {
                resPath = '/' + resPath;
            }
            InputStream is = Util.class.getResourceAsStream(resPath);
            if (is == null) {
                if (notifier != null) {
                    notifier.log(new LogLine(-1, type + " Resource CHECK: " + resPath));
                }
            } else {
                return readResource(resPath, type, list, notifier);
            }
        }
        return readResource(file, type, list, notifier);
    }

    private static String readResource(String file, String type, String list, Notifier notifier) throws IOException {
        InputStream is = Util.class.getResourceAsStream(file);
        if (is == null) {
            is = FileException.class.getResourceAsStream("/" + file);
        }
        if (is == null) {
            if (notifier != null) {
                notifier.log(new LogLine(-1, type + " Resource file NOT found: " + file));
            }
            throw new FileException(type + " Resource [" + file + "] Not Found in path(s) [" + list + "] or on the class path");
        }
        if (notifier != null) {
            notifier.log(new LogLine(-1, type + " Resource found: " + file));
        }
        StringBuilder sb = new StringBuilder();
        int content;
        while ((content = is.read()) != -1) {
            sb.append((char) content);
        }
        return sb.toString();
    }

    public static void loadPropertiesFromBody(Map map, String bodyTrimmed) {
        if ((bodyTrimmed == null) || (bodyTrimmed.isEmpty())) {
            return;
        }
        BodyType bodyType = (BodyType) map.get("BODY-TYPE");
        String bodyTypeName = bodyType.name();
        Map<String, Object> tempMap = mapBodyContent(bodyTrimmed, bodyType);
        for (Map.Entry<String, Object> ent : tempMap.entrySet()) {
            map.put(bodyTypeName + "." + ent.getKey(), ent.getValue());
        }
    }

    private static Map<String, Object> mapBodyContent(String body, BodyType bodyType) {
        Map<String, Object> tempMap = new HashMap<>();
        try {
            switch (bodyType) {
                case XML:
                    MappedXml mappedXml = new MappedXml(body, null);
                    tempMap.putAll(mappedXml.getMap());
                    break;
                case JSON:
                    tempMap.putAll(JsonUtils.flatMap(body));
            }
        } catch (Exception pe) {
            throw new ExpectationException("Failed to parse body text: Type:" + bodyType, 500, pe);
        }
        return tempMap;
    }

}
