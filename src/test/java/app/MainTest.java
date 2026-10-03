package app;

import org.junit.jupiter.api.Test;

import app.src.main.java.app.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Test
    void appReportsStatus() {
        Main classUnderTest = new Main();
        assertEquals("Backend is running", classUnderTest.getStatus());

    }
}
