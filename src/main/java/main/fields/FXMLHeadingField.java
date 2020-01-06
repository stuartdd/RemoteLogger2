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
package main.fields;

import java.io.IOException;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Stuart
 */
public class FXMLHeadingField extends FXMLField {

    
    public FXMLHeadingField(Stage stage, VBox vbox, String id, String text, String entityName, FXMLFieldChangeListener changeListener) throws IOException {
        super(stage, vbox, id,"Heading", null, text, entityName, true, null);
        setControlBackgroundColor(null, HEADING_COLOR);
    }

    @Override
    protected void doRevert() {
    }

    @Override
    public void doLayout() {
        if (getLabel() == null) {
            return;
        }
        super.doLayout();
        setControlWidth(getLabel(), getStage().getWidth());
    }

    @Override
    public void destroy() {
        removeCommonNodes();
    }

}
