package UI;
import ReceiveData. *;

import java.io.IOException;
import java.net.Socket;

public class AdminClientContext {
    private Socket socket;
    private SendDataViaNetwork sendData;
    private ReceiveDataViaNetwork receiveData;
    private AdminUI adminUI;

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

    public Socket getSocket() { return socket; }
    public SendDataViaNetwork getSendData() { return sendData; }
    public ReceiveDataViaNetwork getReceiveData() { return receiveData; }
    public AdminUI getAdminUI() { return adminUI; }
    // En tu archivo DoctorClientContext.java

}
