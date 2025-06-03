package app.tracktune;

import org.junit.jupiter.api.Test;

/**
 * A test runner class that provides information about all available tests in the project.
 * This class doesn't actually run the tests, but it provides information about them.
 * 
 * To run all tests, use the IDE's built-in test runner or a build tool like Maven.
 */
public class AllTestsRunner {

    /**
     * This method provides information about the available tests in the project.
     * It doesn't actually run the tests.
     */
    @Test
    public void testInfo() {
        System.out.println("Available tests in the project:");
        System.out.println("1. AuthorDAOTest - Tests for the AuthorDAO class");
        System.out.println("2. GenreDAOTest - Tests for the GenreDAO class");
        System.out.println("3. TrackGenreDAOTest - Tests for the TrackGenreDAO class");
        System.out.println("4. TrackDAOTest - Tests for the TrackDAO class");
        System.out.println("5. TrackAuthorDAOTest - Tests for the TrackAuthorDAO class");
        System.out.println("\nTo run all tests, use the IDE's built-in test runner or a build tool like Maven.");
    }
}
