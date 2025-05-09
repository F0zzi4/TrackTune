package app.tracktune.model.user;

public enum AuthRequestStatusEnum {
    CREATED(0),
    ACCEPTED(1),
    REJECTED(2);

    private final int value;

    AuthRequestStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AuthRequestStatusEnum fromInt(int i){
        for (AuthRequestStatusEnum status : AuthRequestStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
