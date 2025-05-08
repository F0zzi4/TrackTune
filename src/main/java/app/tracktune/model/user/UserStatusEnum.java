package app.tracktune.model.user;

import javafx.fxml.FXML;

public enum UserStatusEnum{
    CREATED(0),
    REMOVED(1),
    SUSPENDED(2);

    private final int value;

    UserStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserStatusEnum fromInt(int i){
        for (UserStatusEnum status : UserStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
