import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import de.hhs.GroundStation;
import de.hhs.RobotSession;
import org.junit.jupiter.api.*;

import java.net.Socket;
import java.util.Set;
import java.util.HashSet;

class GroundStationTest {

    private GroundStation groundStation;
    private RobotSession mockRobotSession;

    @BeforeEach
    void setUp() {
        groundStation = new GroundStation(9000);
        mockRobotSession = mock(RobotSession.class);
        when(mockRobotSession.getName()).thenReturn("TestRobot");
    }

    @Test
    void testNewRobot() {
        groundStation.newRobot(mockRobotSession);

        assertTrue(groundStation.getRobots().contains(mockRobotSession));
    }

    @Test
    void testSendToRobot() {
        groundStation.newRobot(mockRobotSession);
        groundStation.sendToRobot("TestRobot", "move");

        verify(mockRobotSession, times(1)).send("move");
    }

    @Test
    void testAbandonRobot() {
        groundStation.newRobot(mockRobotSession);
        groundStation.abandonRobot(mockRobotSession);

        assertFalse(groundStation.getRobots().contains(mockRobotSession));
    }

    @Test
    void testShutdown() {
        Set<RobotSession> robots = new HashSet<>();
        robots.add(mockRobotSession);
        groundStation.shutdown();

        assertTrue(groundStation.getRobots().isEmpty());
        verify(mockRobotSession, times(1)).close();
    }
}