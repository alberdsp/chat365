/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Servidor;
import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ServerForm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase controladora del chat de servidor
 * @author Alber
 */
public class ServidorController implements Runnable{
    
    private Map<String, Usuario> usuarios;
    private ServerForm serverForm;

    public ServidorController(ServerForm serverForm) {
        this.serverForm = serverForm;
        this.usuarios = new HashMap<>();
    }

    @Override
    public void run() {
        serverForm.setVisible(true);

        // Crear usuario de la sala principal
        Usuario chatGeneral = new Usuario("CHATGENERAL", "192.168.1.10", 9990);
        usuarios.put(chatGeneral.getNick(), chatGeneral);

        try {
            
          
            ServerSocket ss = new ServerSocket(9990);
            serverForm.getjTextAreaChatGeneral().setText("Servidor " +
                    ss.getInetAddress() + " escuchando en el puerto " +
                    ss.getLocalPort() + "...");
            
            while (true) {
                Socket s = ss.accept();
                serverForm.getjTextAreaChatGeneral().setText("Se ha conectado un cliente " + s.getInetAddress() + " al puerto " + s.getPort());
                new Thread(new ClienteHandler(s)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClienteHandler implements Runnable {
        private Socket socket;

        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream())
            ) {
                String datoCliente;
                while (!(datoCliente = dis.readUTF()).equals("exit")) {
                    System.out.println("Mensaje recibido del cliente: " + datoCliente);
                    dos.writeUTF("Mensaje del servidor: " + datoCliente);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            
    }}
