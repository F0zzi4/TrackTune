package app.tracktune.model.resource;

import java.util.Arrays;

/**
 * Enum representing supported audio and video file extensions.
 * Each enum constant corresponds to a specific file extension.
 */
public enum AudioVideoFileEnum {
    MP3("mp3"),
    MP4("mp4"),
    WAV("wav"),
    M4A("m4a"),
    AIFF("aiff"),
    M4V("m4v");

    private final String extension;

    /**
     * Constructs an enum constant with the given file extension.
     *
     * @param extension the file extension string (e.g., "mp3")
     */
    AudioVideoFileEnum(String extension) {
        this.extension = extension;
    }

    /**
     * Returns the file extension associated with this enum constant.
     *
     * @return the file extension string
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Checks if the given file extension is supported by this enum.
     * The check is case-insensitive.
     *
     * @param ext the file extension to check (e.g., "MP3", "wav")
     * @return true if the extension is supported, false otherwise
     */
    public static boolean isSupported(String ext) {
        return Arrays.stream(AudioVideoFileEnum.values())
                .anyMatch(fileExt -> fileExt.getExtension().equalsIgnoreCase(ext));
    }
}
