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
import java.util.HashMap;
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
    private int puerto = 9990;
    // mapa que contrendrá los hilos abiertos.
 

    public ServidorController(ServerForm serverForm) {
        this.serverForm = serverForm;
        this.chat = new Chat();
        Usuario sala = new Usuario("SALA_CHAT", "192.168.1.137", 9990, true);
        chat.getChat().put(sala, "");
    }
    
   
    @Override
    public void run() {
        serverForm.setVisible(true);
        ServerSocket serverSocket = null;

        try {
            // Creamos el servidor que escucha siempre en el puerto especificado
            serverSocket = new ServerSocket(9990);
            serverForm.getjTextAreaChatGeneral().setText("Servidor escuchando en el puerto " + puerto + "\n");

            while (true) {
                // Aceptar una nueva conexión
                Socket socket = serverSocket.accept();

                // Crear un nuevo hilo para manejar la conexión
                ClienteHandler clienteHandler = new ClienteHandler(socket, chat, serverForm, this);
                Thread thread = new Thread(clienteHandler);
                thread.start();

                // Llamar al método para enviar la lista de usuarios a todos los clientes
            //    enviarListaUsuarios();

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

    // método para enviar a todos los hilos la lista nueva de usuarios
   public void enviarListaUsuarios() {
    // Asumiendo que chat.getChat() devuelve un mapa con Usuario como clave
    LinkedHashMap<Usuario, String> usuariosChat = chat.getChat();

    // Preparar el objeto o mensaje a enviar. Si es solo una lista de usuarios,
    // necesitarás crear este objeto basado en los usuarios en usuariosChat.
    // Por simplicidad, aquí asumiré que simplemente reenvías el chat actualizado.
    Object objetoAEnviar = this.chat; // O cualquier objeto que represente la lista de usuarios.

    for (Usuario usuario : usuariosChat.keySet()) {
        Socket socket = usuario.getSocket();
        if (socket != null && socket.isConnected()) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.reset(); // Asegura que el objeto se envíe incluso si no ha cambiado.
                oos.writeObject(objetoAEnviar);
                oos.flush();
            } catch (IOException e) {
                Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, e);
                // Considera manejar adecuadamente la desconexión de un usuario aquí.
            }
        }
    }
}
   
   
   
    // método para enviar a todos los hilos la lista nueva de usuarios
   public void enviarMesajeSala( Mensaje mensaje) {
    // Asumiendo que chat.getChat() devuelve un mapa con Usuario como clave
    LinkedHashMap<Usuario, String> usuariosChat = chat.getChat();

 
    Object objetoAEnviar = mensaje; // O cualquier objeto que represente la lista de usuarios.

    for (Usuario usuario : usuariosChat.keySet()) {
        
         Usuario origen = mensaje.getOrigen();
        if (usuario.getNick().equals("SALA_CHAT") || origen==usuario){
        
            // no reeviamos al mismo destinO O LA SALA
            
        }else{
        Socket socketcli;
        
        try {
            socketcli = new Socket(usuario.getIp(), usuario.getPuerto());
              //Socket socket = usuario.getSocket();
        if (socketcli != null && socketcli.isConnected()) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socketcli.getOutputStream());
                oos.reset(); // Asegura que el objeto se envíe incluso si no ha cambiado.
                oos.writeObject(objetoAEnviar);
                oos.flush();
            } catch (IOException e) {
                Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, e);
                // Considera manejar adecuadamente la desconexión de un usuario aquí.
            }
        }    
            
            
            
        } catch (IOException ex) {
            Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }
}
   }
   
   
   
   
   


    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
    
    
    
    
    
    
}


        /*

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
         */
    
