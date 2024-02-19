/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;

import com.abf.chatclient.modelo.vista.ServerForm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Clase que maneja los puertos abiertos.
 * @author Propietario
 */
public class ClienteHandler implements Runnable {
    private Socket socket;
    private Chat chat;
    private ServerForm serverForm;

    public ClienteHandler(Socket socket, Chat chat, ServerForm serverForm) {
        this.socket = socket;
        this.chat = chat;
        this.serverForm = serverForm;
    }

    @Override
    public void run() {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Procesar la conexión del cliente
            while (true) {
                Object objetoRecibido = ois.readObject();

                // Procesar el objeto recibido
                if (objetoRecibido instanceof Mensaje) {
                    Mensaje mensaje = (Mensaje) objetoRecibido;
                    String mensajetxt = mensaje.getMensaje();
                    serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick()
                            + " : " + mensajetxt + "\n");
                } else if (objetoRecibido instanceof Usuario) {
                    Usuario usuario = (Usuario) objetoRecibido;

                    usuario.setSocket(socket);

                    this.chat.getChat().put(usuario, "");
                    oos.writeObject(chat);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (ois != null) {
                    ois.close();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
