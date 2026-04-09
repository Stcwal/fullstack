package backend.fullstack.alcohol.domain;

/**
 * Method used to verify a guest's age during alcohol service.
 */
public enum VerificationMethod {
    ID_CHECKED,
    PASSPORT_CHECKED,
    DRIVING_LICENSE_CHECKED,
    KNOWN_REGULAR,
    VISUALLY_OVER_AGE
}
