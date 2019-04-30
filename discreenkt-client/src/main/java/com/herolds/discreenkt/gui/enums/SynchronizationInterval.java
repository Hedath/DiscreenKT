package com.herolds.discreenkt.gui.enums;

public enum SynchronizationInterval {
    DAILY("Every day"),
    WEEKLY("Every week"),
    MONTHLY("Every Month");

    private String label;

    SynchronizationInterval(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}