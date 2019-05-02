package com.herolds.discreenkt.gui.validators;

import java.io.File;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;

import javafx.scene.control.Control;

/**
 * Created by herold on 2018. 01. 24..
 */
public class PathValidator implements DiscreenKTValidator<String> {

    @Override
    public ValidationResult apply(Control control, String value) {
        boolean condition = false;

        condition = validate(value);

        return ValidationResult.fromMessageIf( control, "Wrong file path!", Severity.ERROR, condition );
    }

    @Override
    public boolean validate(String value) {
        if (value == null) {
            return true;
        } else {
            File directory = new File(value);
            return  !directory.exists();
        }
    }
}
