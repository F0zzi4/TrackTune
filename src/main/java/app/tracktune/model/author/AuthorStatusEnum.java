package app.tracktune.model.author;

public enum AuthorStatusEnum {
    ACTIVE(0),
    REMOVED(1);

    private final int value;

    AuthorStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthorStatusEnum fromInt(int i){
        for (AuthorStatusEnum status : AuthorStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
