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

public class Exp {

    public static ExpChain withPath(String path) {
        return new ExpChain().withPath(path);
    }

    public static ExpChain withProperty(String key, String value) {
        return new ExpChain().withProperty(key, value);
    }

    public static ExpChain withGetMethod() {
        return new ExpChain().withGetMethod();
    }
    
    public static ExpChain withName(String name) {
        return new ExpChain().withName(name);
    }

    public static ExpChain withAnyMethod() {
        return new ExpChain().withAnyMethod();
    }

    public static ExpChain withPostMethod() {
        return new ExpChain().withPostMethod();
    }

    public static ExpChain withPutMethod() {
        return new ExpChain().withPutMethod();
    }

    public static ExpChain withPatchMethod() {
        return new ExpChain().withPatchMethod();
    }

    public static ExpChain withDeleteMethod() {
        return new ExpChain().withDeleteMethod();
    }

    public static ExpChain withEmptyBody() {
        return new ExpChain().withEmptyBody();
    }

    public static ExpChain withJsonBody() {
        return new ExpChain().withJsonBody();
    }

    public static ExpChain withXmlBody() {
        return new ExpChain().withXmlBody();
    }
}
