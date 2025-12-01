package UI;
import ReceiveData. *;

import java.io.IOException;
import java.net.Socket;

/**
 * Helper context for the ADMIN client.
 * Opens the socket, wraps it with send/receive helpers, creates AdminUI,
 * and performs the initial handshake to identify as ADMIN to the server.
 */
public class AdminClientContext {
    private Socket socket;
    private SendDataViaNetwork sendData;
    private ReceiveDataViaNetwork receiveData;
    private AdminUI adminUI;

    /**
     * Connects to the server and verifies that the client is accepted as ADMIN.
     * Protocol:
     * - Open socket to (host, port).
     * - Send code 3 to identify as ADMIN client.
     * - Expect "ADMIN" as server response.
     *
     * @param host server IP/hostname.
     * @param port server port.
     * @throws IOException if connection fails or server does not accept ADMIN.
     */
    public AdminClientContext(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.sendData = new SendDataViaNetwork(socket);
        this.receiveData = new ReceiveDataViaNetwork(socket);
        this.adminUI = new AdminUI();

        // Identificar al servidor que somos ADMIN
        sendData.sendInt(3);
        String msg = receiveData.receiveString();

        if (!"ADMIN".equals(msg)) {
            throw new IOException("Server did not accept administrator client. Response: " + msg);
        }
    }

    /**
     * @return underlying socket connected to the server.
     */
    public Socket getSocket() { return socket; }

    /**
     * @return helper used to send data to the server.
     */
    public SendDataViaNetwork getSendData() { return sendData; }

    /**
     * @return helper used to receive data from the server.
     */
    public ReceiveDataViaNetwork getReceiveData() { return receiveData; }

    /**
     * @return admin UI helper for login, register, and admin actions.
     */
    public AdminUI getAdminUI() { return adminUI; }

}
