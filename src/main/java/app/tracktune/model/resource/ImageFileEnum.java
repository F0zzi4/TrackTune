package app.tracktune.model.resource;

import java.util.Arrays;

/**
 * Enum representing supported image file extensions.
 * Each enum constant corresponds to a specific image file extension.
 */
public enum ImageFileEnum {
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    GIF("gif"),
    BMP("bmp"),
    PDF("pdf"),
    WBMP("wbmp");

    private final String extension;

    /**
     * Constructs an enum constant with the given file extension.
     *
     * @param extension the image file extension string (e.g., "png")
     */
    ImageFileEnum(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the image file extension associated with this enum constant.
     *
     * @return the image file extension string
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Checks if a given file extension is supported by this enum.
     * The check is case-insensitive and returns false if the input is null.
     *
     * @param ext the file extension to check (e.g., "PNG", "jpeg")
     * @return true if the extension is supported, false otherwise
     */
    public static boolean isSupported(String ext) {
        return ext != null && Arrays.stream(ImageFileEnum.values())
                .anyMatch(fileExt -> fileExt.getExtension().equalsIgnoreCase(ext));
    }
}
