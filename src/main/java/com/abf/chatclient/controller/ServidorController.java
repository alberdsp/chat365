/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
import com.abf.chatclient.modelo.ClienteHandler;
import com.abf.chatclient.modelo.Mensaje;

import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ServerForm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Map;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TreeMap;

/**
 * Clase controladora del chat de servidor
 *
 * @author Alber
 */
public class ServidorController implements Runnable {

    private Chat chat;
    private final ServerForm serverForm;
    private int puerto = 9990 ;

    public ServidorController(ServerForm serverForm) {
        this.serverForm = serverForm;
        this.chat = new Chat();
    }

  @Override
    public void run() {
        serverForm.setVisible(true);
        ServerSocket serverSocket = null;

        try {
            // Crear el socket servidor en el puerto especificado
            serverSocket = new ServerSocket(9990);
            serverForm.getjTextAreaChatGeneral().setText("Servidor escuchando en el puerto " + puerto + "\n");

            while (true) {
                // Aceptar una nueva conexión
                Socket socket = serverSocket.accept();
                serverForm.getjTextAreaChatGeneral().append("Se ha conectado un cliente " + socket.getInetAddress() + " al puerto " + socket.getPort() + "\n");

                // Crear un nuevo hilo para manejar la conexión
                Thread thread = new Thread(new ClienteHandler(socket, chat, serverForm));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void actualizarUsuarios() {

        Chat chatactual = this.chat;

        LinkedHashMap<Usuario, String> conversacion = chatactual.getChat();

        // Recorrer el TreeMap para enviar el mensaje a cada usuario
        for (Map.Entry<Usuario, String> entry : conversacion.entrySet()) {
            ObjectOutputStream toos = null;
            try {
                Usuario usuario = entry.getKey();
                String conversacionUsuario = entry.getValue();
                Socket ts = usuario.getSocket();
                toos = new ObjectOutputStream(ts.getOutputStream());
                toos.writeObject(chatactual);
            } catch (IOException ex) {
                Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    toos.close();
                } catch (IOException ex) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

}
