package app.tracktune.model.resource;

import java.util.Arrays;

public enum ImageFileEnum {
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    GIF("gif"),
    BMP("bmp"),
    PDF("pdf"),
    WBMP("wbmp");

    private final String extension;

    ImageFileEnum(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Checks if a given extension is supported.
     *
     * @param ext the file extension (case-insensitive)
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(String ext) {
        return ext != null && Arrays.stream(ImageFileEnum.values())
                .anyMatch(fileExt -> fileExt.getExtension().equalsIgnoreCase(ext));
    }
}
