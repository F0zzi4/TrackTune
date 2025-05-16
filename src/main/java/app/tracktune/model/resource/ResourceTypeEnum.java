package app.tracktune.model.resource;

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
}
