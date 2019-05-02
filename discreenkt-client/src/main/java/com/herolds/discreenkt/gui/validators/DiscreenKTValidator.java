package com.herolds.discreenkt.gui.validators;

import org.controlsfx.validation.Validator;

public interface DiscreenKTValidator<T> extends Validator<T> {
	
	boolean validate(T value);
	
}
