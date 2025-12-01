package SendData;

import org.junit.jupiter.api.Test;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.*;

public class SocketConnectionTest {

    @Test
    void testSocketConnection() {
        System.out.println("\n=== SOCKET CONNECTION UNIT TEST ===");

        try {
            // Intento de conexión al servidor por socket
            Socket socket = new Socket("localhost", 8888);

            assertNotNull(socket, "Socket no creado");
            assertTrue(socket.isConnected(), "El socket NO está conectado → ¿ServerMain en ejecución?");

            System.out.println("✔ Conexión establecida correctamente con el servidor");

            socket.close();

        } catch (Exception e) {
            fail("Error de conexión → ¿El servidor está en localhost:8888? \n" + e.getMessage());
        }
    }
}

