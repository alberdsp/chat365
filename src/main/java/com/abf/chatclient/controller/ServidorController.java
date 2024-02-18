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
 * @author Alber
 */
public class ServidorController implements Runnable{
    
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
        
        chat.getChat().put(chatGeneral, "");
      
        
        

        try {
            
          
            ServerSocket ss = new ServerSocket(9990);
            serverForm.getjTextAreaChatGeneral().setText("Servidor " +
                    ss.getInetAddress() + " escuchando en el puerto " +
                    ss.getLocalPort() + "\n");
            
            while (true) {
                Socket s = ss.accept();
                serverForm.getjTextAreaChatGeneral().setText("Se ha conectado un cliente " + s.getInetAddress() + " al puerto " + s.getPort()+"\n");
                
               // Crear ObjectOutputStream utilizando el OutputStream del socket
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
              // Escribir el objeto en el flujo de salida
            oos.writeObject(chat);

            // Realizar un flush para asegurarse de que todos los datos se envíen
            oos.flush();

            Object objetoRecibido= null; 
                try {
                    objetoRecibido = ois.readObject();
                    
                       // Procesar el objeto recibido
            if (objetoRecibido instanceof Mensaje) {
                String mensajeRecibido = ((Mensaje) objetoRecibido).getMensaje();
                serverForm.getjTextAreaChatGeneral().setText("Mensaje recibido del cliente: " + mensajeRecibido+"\n");
            } else if
            
                  // Procesar el objeto recibido
                (objetoRecibido instanceof Usuario) {
                  Usuario usuario = (Usuario) objetoRecibido;
                
                  usuario.setSocket(s);
                  
                 this.chat.getChat().put(usuario ,"");
                 oos.writeObject(chat);
                 
            }   
                    
                    
                    
                    
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                  
       
        
                
                /*
                String datoCliente;
                while (!(datoCliente = dis.readUTF()).equals("exit")) {
             //        serverForm.getjTextAreaChatGeneral().append("Mensaje recibido del cliente: " + datoCliente+"\n");
             //         dos.writeUTF("Mensaje del servidor: " + " recibido " 
            //          + "\n");
                      
                }
           */
        
        }   } catch (IOException e) {
                e.printStackTrace();
            }
     

   
    }
    
    private void actualizarUsuarios(){
        
               
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
