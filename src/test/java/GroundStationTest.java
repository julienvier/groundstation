import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.hhs.DatabaseManager;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.sql.*;

class DatabaseManagerTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;
    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        dbManager = new DatabaseManager() {
            protected Connection connect() {
                return mockConnection;
            }
        };
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @Test
    void testInsertPlanet() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        dbManager.insertPlanet("Pandora", 1000, 1000);

        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testInsertRobot() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        dbManager.insertRobot("ACTIVE");

        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testInsertPosition() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);

        dbManager.insertPosition(1, 1, 10, 20, "SAND");

        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testGetOrCreateRobot_NewRobot() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockStatement.executeUpdate()).thenReturn(1);

        int robotID = dbManager.getOrCreateRobot("Robot123", "ACTIVE");

        assertNotEquals(-1, robotID);
        verify(mockStatement, times(2)).executeQuery();
    }
}
