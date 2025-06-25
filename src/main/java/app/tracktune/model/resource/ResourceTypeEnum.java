package app.tracktune.model.resource;

import java.util.Arrays;

/**
 * Enumeration representing different resource types,
 * each mapped to an integer value typically stored in the database.
 * <p>
 * Supported types include common media and document formats such as mp3, mp4, pdf, midi, jpg, png,
 * and a special 'link' type which does not have a file extension.
 */
public enum ResourceTypeEnum {
    mp3(1),
    mp4(2),
    pdf(3),
    midi(4),
    jpg(5),
    png(6),
    link(7);

    private final int value;

    /**
     * Constructs a ResourceTypeEnum with the specified integer value.
     *
     * @param value the integer value representing this resource type
     */
    ResourceTypeEnum(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this resource type.
     *
     * @return integer value of this resource type
     */
    public int getValue() {
        return value;
    }

    /**
     * Converts an integer value to the corresponding ResourceTypeEnum.
     *
     * @param i the integer value representing a resource type
     * @return the matching ResourceTypeEnum
     * @throws IllegalArgumentException if the value does not correspond to any ResourceTypeEnum
     */
    public static ResourceTypeEnum fromInt(int i){
        for (ResourceTypeEnum type : ResourceTypeEnum.values()) {
            if (type.getValue() == i) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }

    /**
     * Returns an array of file extension patterns for resource types,
     * excluding the 'link' type since it does not have an extension.
     * <p>
     * Example output: ["*.mp3", "*.mp4", "*.pdf", "*.midi", "*.jpg", "*.png"]
     *
     * @return an array of file extension filters as strings
     */
    public static String[] getExtensions() {
        return Arrays.stream(ResourceTypeEnum.values())
                .filter(type -> type != link)   // 'link' does not have a file extension
                .map(type -> "*." + type.name())
                .toArray(String[]::new);
    }
}
