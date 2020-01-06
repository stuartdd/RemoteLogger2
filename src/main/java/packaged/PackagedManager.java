/*
 * Copyright (C) 2019 Stuart Davies
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
 *
 */
package packaged;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

/**
 *
 * @author Stuart
 */
public class PackagedManager {

    private static String instanceFileName;

    private static PackagedRequests packagedRequests;

    public static PackagedRequests getPackagedRequests() {
        if ((instanceFileName == null) || (packagedRequests == null)) {
            throw new PackagedException("Package manager is not defined!");
        }
        return packagedRequests;
    }
    
    public static PackagedRequest getPackagedRequest(String name) {
        for (PackagedRequest pr:getPackagedRequests().getPackagedRequests()) {
            if (pr.getName().equals(name)) {
                return pr;
            }
        }
        return null;
    }

    public static List<String> getRequestNamesList(){
        List<String> list = new ArrayList<>();
        for (PackagedRequest pr : getPackagedRequests().getPackagedRequests()) {
            list.add(pr.getName());
        }
        return list;
    }
    
    public static String getInstanceFileName() {
        return instanceFileName;
    }

    public static void setInstanceFileName(String fileName) {
        packagedRequests = packagedManagerLoad(fileName);
        instanceFileName = fileName;
    }

    public static PackagedRequests packagedManagerLoad(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new PackagedException("Package file '" + file.getAbsolutePath() + "' does not exist");            
        }
        try {
            PackagedRequests pr = (PackagedRequests) JsonUtils.beanFromJson(PackagedRequests.class, file);
            if (pr.getPackagedRequests().size() == 0) {
                throw new PackagedException("Package does not contain any packaged requests");
            }
            return pr;
        } catch (Exception ex) {
            throw new PackagedException("Package file '" + file.getAbsolutePath() + "' is not a valid PackagedRequests json file");
        }
    }

}
