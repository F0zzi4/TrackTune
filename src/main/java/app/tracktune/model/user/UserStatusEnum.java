package app.tracktune.model.user;

/**
 * Enum representing the possible statuses of a user.
 */
public enum UserStatusEnum {
    /** User is active and can use the system normally. */
    ACTIVE(0),

    /** User has been removed (deleted or deactivated). */
    REMOVED(1),

    /** User is suspended and temporarily blocked from using the system. */
    SUSPENDED(2);

    private final int value;

    /**
     * Constructs a UserStatusEnum with the specified integer value.
     *
     * @param value the integer representation of the user status
     */
    UserStatusEnum(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this status.
     *
     * @return the integer representation of the status
     */
    public int getValue() {
        return value;
    }

    /**
     * Converts an integer to its corresponding UserStatusEnum.
     *
     * @param i the integer value representing a user status
     * @return the matching UserStatusEnum
     * @throws IllegalArgumentException if the integer does not correspond to any status
     */
    public static UserStatusEnum fromInt(int i) {
        for (UserStatusEnum status : UserStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
