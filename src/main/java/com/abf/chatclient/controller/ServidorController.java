/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
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
import java.io.ObjectOutputStream;

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
            ObjectOutputStream outputStream = new ObjectOutputStream(s.getOutputStream());

              // Escribir el objeto en el flujo de salida
            outputStream.writeObject(chat);

            // Realizar un flush para asegurarse de que todos los datos se envíen
            outputStream.flush();

                DataInputStream dis = new DataInputStream(s.getInputStream());
        
                
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
}
