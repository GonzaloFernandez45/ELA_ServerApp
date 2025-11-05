package org.example;

import ReceiveData.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import jdbc.*;
import ReceiveData.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

        private static int activeClients = 0;
        /** The server's running state */
        private static boolean running = true;

        public static void main(String[] args) throws IOException {
            ConnectionManager manager = new ConnectionManager();
            ServerSocket serverSocket = new ServerSocket(8000);


        }
}
