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
import java.util.Calendar;

import json.JsonUtils;

/**
 *
 * @author Stuart
 */
public class PackagedManager {
    private final String filaName;
    private PackagedRequests packagedRequests;

    public PackagedManager(String filaName) {
        this.filaName = filaName;
        File file = new File(this.filaName);
        PackagedRequests pr;
        if (file.exists()) {
            try {
                pr = (PackagedRequests) JsonUtils.beanFromJson(PackagedRequests.class, file);
            } catch (Exception ex) {
                throw new PackagedException("Package file '"+file.getAbsolutePath()+"' is not a valid PackagedRequests json file");
            }
            if (pr.getPackagedRequests().size() == 0) {
                throw new PackagedException("Package does not contain any packaged requests");
            }
            this.packagedRequests = pr;
        } else {
            throw new PackagedException("Package file '"+file.getAbsolutePath()+"' does not exist");
        }
    }
    
}
