import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import de.hhs.GroundStation;
import de.hhs.RobotSession;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;

class RobotSessionTest {

    private GroundStation mockGroundStation;
    private Socket mockSocket;
    private PrintWriter mockWriter;
    private BufferedReader mockReader;
    private RobotSession robotSession;

    @BeforeEach
    void setUp() throws Exception {
        mockGroundStation = mock(GroundStation.class);
        mockSocket = mock(Socket.class);
        mockWriter = mock(PrintWriter.class);
        mockReader = mock(BufferedReader.class);

        when(mockSocket.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(mockSocket.getInputStream()).thenReturn(mock(InputStream.class));

        robotSession = new RobotSession(mockGroundStation, mockSocket);
    }

    @Test
    void testGetName() {
        assertNull(robotSession.getName()); // Name sollte anfangs null sein
    }

    @Test
    void testSendCommand() {
        robotSession.send("move");

        verify(mockWriter, times(1)).println("move");
    }

    @Test
    void testClose() throws Exception {
        doNothing().when(mockSocket).close();

        robotSession.close();

        verify(mockSocket, times(1)).close();
    }
}