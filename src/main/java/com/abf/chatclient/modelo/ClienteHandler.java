/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;

import com.abf.chatclient.controller.ServidorController;
import com.abf.chatclient.modelo.vista.ServerForm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que maneja los puertos abiertos.
 * @author Propietario
 */
public class ClienteHandler implements Runnable {
    private Socket socket;
    private Chat chat;
    private ServerForm serverForm;
    private ServidorController servidorcontroller;
    private Map<String, ClienteHandler> clienteHandlersPorNick;
    private Usuario usuario;
    private Boolean refrescarusuarios = false;
    private String texto = "";
    
    public ClienteHandler(Socket socket, Chat chat, ServerForm serverForm, Map<String, ClienteHandler> clienteHandlersPorNick, ServidorController servidorcontroller) {
        this.socket = socket;
        this.chat = chat;
        this.serverForm = serverForm;
        this.clienteHandlersPorNick = clienteHandlersPorNick;
        this.servidorcontroller = servidorcontroller;
        
    }

    
    
    @Override
public void run() {
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;

    try {
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        while (true) {
            Object objetoRecibido = ois.readObject();

            // Verificar si el objeto recibido es un Mensaje
            if (objetoRecibido instanceof Mensaje) {
                Mensaje mensaje = (Mensaje) objetoRecibido;
                String mensajetxt = mensaje.getMensaje();

                // Si el mensaje es "salir", cerrar el socket y salir del método run
                if ("salir".equals(mensajetxt)) {
                    serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " ha salido del chat.\n");
                    
                    servidorcontroller.getChat().chat.put(usuario, "sale");
                    
                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                }if ("cerrar".equals(mensajetxt)) {
                    serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " cerró conexión .\n");
                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                }

                // Procesamiento normal de mensajes no relacionados con "salir"
                serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " : " + mensajetxt + "\n");
            }
            // Procesamiento de otros tipos de objetos como Usuario, etc.
             else if (objetoRecibido instanceof Usuario) {
                         usuario = (Usuario) objetoRecibido;
                    
                  serverForm.getjTextAreaChatGeneral().append("Se ha conectado " + usuario.getNick()
                          + " por la IP :" + 
                          socket.getInetAddress() + " al puerto " + socket.getPort() + "\n");

                    usuario.setSocket(socket);
                    
                  //  this.chat.getChat().put(usuario, "");
                    servidorcontroller.getChat().chat.put(usuario, "entra");
                    
                    
                // Agregar el ClienteHandler al mapa usando el nick del usuario como clave
                    clienteHandlersPorNick.put(usuario.getNick(), this);
                    
                    this.chat = servidorcontroller.getChat();
                    oos.writeObject(chat);
                    oos.flush();
               
        }}}
          catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
    } finally {
        try {
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    
    

    public Usuario getUsuario() {
          
         return  this.usuario ;
           
    }
    
    public Socket getSocket(){
    
          return this.socket;
    }

    
}
