package com.herolds.discreenkt.gui.validators;

import java.util.regex.Pattern;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import javafx.scene.control.Control;

public class UserURLValidator implements Validator<String> {

	private static final String USER_URL_REGEX = "\\d+\\/.+";
	
    @Override
    public ValidationResult apply(Control control, String value) {
        return ValidationResult.fromMessageIf( control, "Wrong URL part syntax! Should be: ID/username", Severity.ERROR, validate(value));
    }

    public boolean validate(String value) {
        return value == null || !Pattern.matches(USER_URL_REGEX, value);
    }

}
