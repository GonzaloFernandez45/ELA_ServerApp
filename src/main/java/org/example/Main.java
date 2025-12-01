package org.example;

import java.io.IOException;

import ReceiveData.*;

/**
 * Entry point of the telemedicine server application.
 * Creates the Server on port 8888 and starts listening for client connections.
 */
public class Main {


    public static void main(String[] args) throws IOException {
        // Create server socket bound to port 8888
        Server server = new Server(8888);

        // Start accepting and handling client connections
        server.start();
    }



}

