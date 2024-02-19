/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
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

    public ServidorController(ServerForm serverForm) {
        this.serverForm = serverForm;
        this.chat = new Chat();
    }

    @Override
    public void run() {
        serverForm.setVisible(true);

        // Crear usuario de la sala principal
        Usuario chatGeneral = new Usuario("CHATGENERAL", "192.168.1.137", 9990);
        //lo añadimos al la sala
        chat.getChat().put(chatGeneral, "");

        try {
             
             // creamos el socket servidor
            ServerSocket ss = new ServerSocket(9990);
            serverForm.getjTextAreaChatGeneral().setText("Servidor "
                    + ss.getInetAddress() + " escuchando en el puerto "
                    + ss.getLocalPort() + "\n");
            
            // habilitamos la escucha
            Socket s = ss.accept();
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            // notificamos la conexión de un cliente
            serverForm.getjTextAreaChatGeneral().append("Se ha conectado un cliente " + s.getInetAddress() + " al puerto " + s.getPort() + "\n");
                
            // enviamos al cliente la sala de chat con los usuarios.
                oos.writeObject(chat);

            // Realizamos un flush para aseguranos de que todos los datos se envíen
                oos.flush();
                
                // nos quedamos a la escucha
            while (true) {

           

                Object objetoRecibido = null;
                try {
                    objetoRecibido = ois.readObject();

                    // Procesar el objeto recibido
                    if (objetoRecibido instanceof Mensaje) {
                        Mensaje mensaje  =  ((Mensaje) objetoRecibido);
                        String mensajetxt = mensaje.getMensaje();
                        serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick()
                                + " : " + mensajetxt + "\n");
                    } else if // Procesar el objeto recibido
                            (objetoRecibido instanceof Usuario) {
                        Usuario usuario = (Usuario) objetoRecibido;

                        usuario.setSocket(s);

                        this.chat.getChat().put(usuario, "");
                        oos.writeObject(chat);

                    }

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                }

            
            }
        } catch (IOException e) {
            e.printStackTrace();

            System.out.print(e);
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
