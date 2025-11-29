package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import interfaces.*;
import jdbc.*;
import ReceiveData.*;
import pojos.*;
import jdbc.JDBCPatientManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.List;

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

