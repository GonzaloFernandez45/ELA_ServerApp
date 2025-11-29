package org.example;

import java.io.IOException;

import ReceiveData.*;

public class Main {

    private static int activeClients = 0;
    /**
     * The server's running state
     */
    private static boolean running = true;

    public static void main(String[] args) throws IOException {
        Server server = new Server(8888);
        server.start();
    }



}

