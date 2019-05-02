package com.herolds.discreenkt.gui.enums;

import java.util.Optional;

public enum SynchronizationInterval {
    NONE("No sync"),
	DAILY("Every day"),
    WEEKLY_MONDAY("Every monday"),
    WEEKLY_SUNDAY("Every sunday"),
    MONTHLY_FIRST("Every month (first day)"),
	MONTHLY_LAST("Every month (last day)");

    private String label;

    SynchronizationInterval(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
    
    public static Optional<SynchronizationInterval> getEnumValue(String value) {
  		for(SynchronizationInterval enumValue : SynchronizationInterval.values()) {
  			if (enumValue.name().equalsIgnoreCase(value)) {
  				return Optional.of(SynchronizationInterval.valueOf(value));
  			}
  		}
  		
  		return Optional.empty();
  	}
}