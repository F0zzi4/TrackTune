package app.tracktune.model.resource;

import java.util.Arrays;

public enum ResourceTypeEnum {
    mp3(1),
    mp4(2),
    pdf(3),
    midi(4),
    jpg(5),
    png(6),
    link(7);

    private final int value;

    ResourceTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ResourceTypeEnum fromInt(int i){
        for (ResourceTypeEnum status : ResourceTypeEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }

    public static String[] getExtensions() {
        return Arrays.stream(ResourceTypeEnum.values())
                .filter(type -> type != link)   // link does not have file extension
                .map(type -> "*." + type.name())
                .toArray(String[]::new);
    }
}
