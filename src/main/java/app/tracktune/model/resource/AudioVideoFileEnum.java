package app.tracktune.model.resource;

import java.util.Arrays;

public enum AudioVideoFileEnum {
    MP3("mp3"),
    MP4("mp4"),
    WAV("wav"),
    M4A("m4a"),
    AIFF("aiff"),
    M4V("m4v");

    private final String extension;

    AudioVideoFileEnum(String extension) {
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
        return Arrays.stream(AudioVideoFileEnum.values()).anyMatch(
                fileExt -> fileExt.getExtension().equalsIgnoreCase(ext));
    }
}
