package com.herolds.discreenkt.gui.validators;

import java.util.regex.Pattern;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;

import javafx.scene.control.Control;

public class TimeValidator implements DiscreenKTValidator<String> {

	private static final String TIME_REGEX = "^(0[0-9]|1[0-9]|2[0-3]|[0-9]):[0-5][0-9]$";
	
	@Override
	public ValidationResult apply(Control control, String value) {
		return ValidationResult.fromMessageIf( control, "Wrong time syntax! Should be: hh:mm", Severity.ERROR, validate(value));
	}
	
	@Override
    public boolean validate(String value) {
        return value == null || !Pattern.matches(TIME_REGEX, value);
    }
}
