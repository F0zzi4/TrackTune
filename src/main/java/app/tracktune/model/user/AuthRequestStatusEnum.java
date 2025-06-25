package app.tracktune.model.user;

/**
 * Enum representing the possible statuses of an authentication request.
 * <p>
 * Each status has an associated integer value for storage or interoperability purposes.
 * </p>
 */
public enum AuthRequestStatusEnum {
    /**
     * The authentication request has been created but not yet processed.
     */
    CREATED(0),

    /**
     * The authentication request has been accepted.
     */
    ACCEPTED(1),

    /**
     * The authentication request has been rejected.
     */
    REJECTED(2);

    /**
     * Integer value representing the status.
     */
    private final int value;

    /**
     * Constructs the enum with the specified integer value.
     *
     * @param value the integer value of the status
     */
    AuthRequestStatusEnum(int value) {
        this.value = value;
    }

    /**
     * Returns the integer value associated with this status.
     *
     * @return the integer value of the status
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the enum constant corresponding to the given integer value.
     *
     * @param i the integer value to convert
     * @return the matching {@code AuthRequestStatusEnum}
     * @throws IllegalArgumentException if the value does not correspond to any status
     */
    public static AuthRequestStatusEnum fromInt(int i) {
        for (AuthRequestStatusEnum status : AuthRequestStatusEnum.values()) {
            if (status.getValue() == i) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unexpected value: " + i);
    }
}
