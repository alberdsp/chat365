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
    private Usuario usuario;
    private Boolean refrescarusuarios = false;
    private String texto = "";
    
    public ClienteHandler(Socket socket, Chat chat, ServerForm serverForm, ServidorController servidorcontroller) {
        this.socket = socket;
        this.chat = chat;
        this.serverForm = serverForm;
        this.servidorcontroller = servidorcontroller;
        
    }

    
    
    public Usuario getUsuario() {
          
         return  this.usuario ;
           
    }
    
    public Socket getSocket(){
    
          return this.socket;
    }

    
    
@Override
public void run() {
    // Declaración de los recursos fuera del try para poder cerrarlos en el finally
    ObjectOutputStream oos = null;
    ObjectInputStream ois = null;

    try {
        // Instanciación de los streams. Es importante primero instanciar y abrir el ObjectOutputStream
        // para evitar bloqueos en la instanciación del ObjectInputStream en el otro extremo de la conexión.
        oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush(); // Asegúrate de vaciar el buffer tras la creación para evitar bloqueos en el otro extremo.
        ois = new ObjectInputStream(socket.getInputStream());

        // Bucle principal del hilo para leer objetos enviados al servidor
        while (!Thread.currentThread().isInterrupted()) {
            
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
                // enviamos el mensaje a todos
                servidorcontroller.enviarMensajesSala(mensaje);
                
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
                    
                    
                      
                    
                    this.chat = servidorcontroller.getChat();
                    oos.writeObject(chat);
                    oos.flush();
                    
                  //  servidorcontroller.enviarListaUsuarios();
               
        }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }   catch (ClassNotFoundException ex) {
            Logger.getLogger(ClienteHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        // Cerrar los recursos en el bloque finally para asegurar que siempre se liberen
        try {
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    System.out.println("Hilo del manejador de cliente terminado.");
}

    
    
    
    
    
    
    
    
}
