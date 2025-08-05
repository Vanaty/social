package iranga.mg.social.type;

public enum TypeMedia {
    IMAGE,
    VIDEO,
    AUDIO,
    FILE,
    DOCUMENT;

    public static TypeMedia fromString(String type) {
        try {
            return TypeMedia.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown media type: " + type);
        }
    }
}
