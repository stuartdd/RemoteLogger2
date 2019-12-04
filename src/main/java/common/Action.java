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

public enum Action {
    LOG,
    LOG_BODY,
    LOG_HEADER,
    LOG_REFRESH,
    CONFIG_SAVE_ERROR,
    CLEAR_MAIN_LOGS,
    CLEAR_LOGS,
    SCROLL_TO_END,
    SERVER_STATE,
    EXPECTATION_TEXT_CHANGED,
    SAVE_EXPECTATIONS,
    DELETE_EXPECTATION,
    RELOAD_EXPECTATIONS,
    EXPECTATION_SELECTED,
    SERVER_SELECTED,
    RENAME_EXPECTATION,
    ADD_EXPECTATION, 
    PACKAGE_REQUEST_SELECTED, 
    SEND_PACKAGED_REQUEST, 
    RELOAD_PACKAGED_REQUEST, 
    PACKAGED_REQUEST_TEXT_CHANGED,
    SAVE_PACKAGED_REQUEST, 
    DELETE_PACKAGED_REQUEST, 
    RENAME_PACKAGED_REQUEST, 
    ADD_PACKAGED_REQUEST
}
