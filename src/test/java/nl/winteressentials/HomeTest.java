package nl.winteressentials;

import nl.winteressentials.models.Home;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeTest {

    @Test
    void testHomeCreation() {
        Home home = new Home("base", "world_nether", 100.5, 64.0, -200.5, 90.0f, 0.0f, 123456789L);
        assertEquals("base", home.name());
        assertEquals("world_nether", home.worldName());
        assertEquals(100.5, home.x());
        assertEquals(64.0, home.y());
        assertEquals(-200.5, home.z());
        assertEquals(90.0f, home.yaw());
        assertEquals(0.0f, home.pitch());
        assertEquals(123456789L, home.createdAt());
    }

    @Test
    void testHomeNullChecks() {
        assertThrows(NullPointerException.class, () -> new Home(null, "world", 0, 0, 0, 0, 0, 0));
        assertThrows(NullPointerException.class, () -> new Home("home", null, 0, 0, 0, 0, 0, 0));
    }
}
