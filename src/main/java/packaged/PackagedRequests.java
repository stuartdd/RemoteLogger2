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
package packaged;

import java.util.ArrayList;
import java.util.List;
import json.JsonUtils;

public class PackagedRequests {
    private List<PackagedRequest> packagedRequests = new ArrayList<>();
    private String[] paths;
    private boolean verbose;

    public List<PackagedRequest> getPackagedRequests() {
        return packagedRequests;
    }

    public void setPackagedRequests(List<PackagedRequest> packagedRequests) {
        this.packagedRequests = packagedRequests;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String tojSON() {
        return JsonUtils.toJsonFormatted(this);
    }

    public boolean canNotDelete() {
        return packagedRequests.size() < 2;
    }

    public int size() {
        return packagedRequests.size();
    }

    public boolean replace(PackagedRequest validClonedPackagedRequest) {
        for (int i = 0; i< packagedRequests.size(); i++) {
            if (packagedRequests.get(i).getName().equals(validClonedPackagedRequest.getName())) {
                packagedRequests.remove(i);
                packagedRequests.add(i, validClonedPackagedRequest);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String currentPackagedRequestName) {
        for (int i = 0; i< packagedRequests.size(); i++) {
            if (packagedRequests.get(i).getName().equals(currentPackagedRequestName)) {
                packagedRequests.remove(i);
                return true;
            }
        }
        return false;
    }

}
