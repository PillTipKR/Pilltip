package com.oauth2.Drug.Review.Domain;

public enum TagType {
    EFFICACY,
    SIDE_EFFECT,
    OTHER;

    public static TagType from(String key) {
        return switch (key.toLowerCase()) {
            case "efficacy" -> EFFICACY;
            case "sideeffect", "side_effect" -> SIDE_EFFECT;
            case "other" -> OTHER;
            default -> throw new IllegalArgumentException("Unknown tag type: " + key);
        };
    }
}

