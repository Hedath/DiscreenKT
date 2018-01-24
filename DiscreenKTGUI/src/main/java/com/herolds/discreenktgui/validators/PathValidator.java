package com.herolds.discreenktgui.validators;

import javafx.scene.control.Control;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.io.File;

/**
 * Created by herold on 2018. 01. 24..
 */
public class PathValidator implements Validator<String> {

    @Override
    public ValidationResult apply(Control control, String value) {
        boolean condition = false;

        if (value == null) {
            condition = true;
        } else {
            File directory = new File(value);
            condition = !directory.exists();
        }

        return ValidationResult.fromMessageIf( control, "Wrong file path!", Severity.ERROR, condition );
    }
}
