/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
import com.abf.chatclient.modelo.Mensaje;
import com.abf.chatclient.modelo.Servidor;
import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ChatClientForm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.AWTEvent.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;

/**
 *
 * @author Alber
 */
public class ClienteController implements Runnable {

    private Chat chat;

    private ChatClientForm chatClientForm;
    private Servidor servidor;
    private Mensaje mensaje;
    private JButton jButtonEnviar;
    private JTextArea jTextAreaChat;
    private JList<String> jListUsuarios;
    private JTextField jTextEnviar;
    private JToggleButton jToggleConectar;
    private JTextField jTextNick;
    private Boolean conectado = false;
    private Boolean cerrarconexion = false;
    private String mensajetxt = "";

    public ClienteController(ChatClientForm chatClientForm) {

        this.chatClientForm = chatClientForm;
        this.chat = new Chat();

    }

    @Override
    public void run() {

        initComponents();

    }

    private void iniciarChat()  {

        try {

            // Mediante el OutputStream mando el mensaje escrito al servidor
            //	dos.writeUTF(dato_cliente);
            String ipserver = chatClientForm.getjTextFieldIPServidor().getText();
            int puerto = Integer.parseInt(chatClientForm.getjTextFieldPuerto().getText());
            servidor = new Servidor(ipserver, puerto);

            // Lanzo el socket del cliente para conectar al "localhost" servidor por el puerto 7040
            Socket s = new Socket(servidor.getIp(), servidor.getPuerto());
            // Muestro el mensaje de conexi�n
            chatClientForm.getjTextAreaSala().setText("Conectado al servidor por el puerto " + s.getPort() + "\n");
            // Inicializo los flujos de entrada/salida a trav�s del Socket "s"
            // Crear un ObjectInputStream utilizando el InputStream del socket cliente
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());


            DataInputStream dis = new DataInputStream(s.getInputStream());
            jToggleConectar.setText("ON");

            // Mientras que el cliente no mande el mensaje "exit" sigue pidiendo datos.
            // Pido al cliente que escriba un mensaje
            //chatClientForm.getjTextAreaSala().setText("Escriba mensaje > ");
            //   Método listener al hacer clic
            jButtonEnviar.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        mensajetxt = chatClientForm.getjTextFieldTextoAenviar().getText();
                        mensaje.setMensaje(mensajetxt);
                        oos.writeObject(mensaje);
                        mensajetxt = "";
                        chatClientForm.getjTextFieldTextoAenviar().setText("");
                    } catch (IOException ex) {
                        Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

          
            while (conectado) {
            // Leer el objeto enviado por el cliente
            Object objetoRecibido = ois.readObject();

           
            // Procesar el objeto recibido
            if (objetoRecibido instanceof Mensaje) {
                String mensajeRecibido = ((Mensaje) objetoRecibido).getMensaje();
                chatClientForm.getjTextAreaSala().setText("Mensaje recibido del cliente: " + mensajeRecibido+"\n");
            } else if
            
                  // Procesar el objeto recibido
                (objetoRecibido instanceof Chat) {
                 this.chat = (Chat) objetoRecibido;
                 cargarNicks(chat);
            } 
            
            
            
            
            
            
           
           //      chatClientForm.getjTextAreaSala().setText(dis.readUTF() + "\n");

            }
            // Una vez finalizado cierro los flujos de entrada/salida y el socket "s"
             
            
          
            oos.close();
            ois.close();
            s.close();
            
            


            jToggleConectar.setText("OFF");
            
            cerrarconexion = true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void initComponents() {

        jButtonEnviar = chatClientForm.getjButtonEnviar();
        jTextAreaChat = chatClientForm.getjTextAreaSala();
        jListUsuarios = chatClientForm.getjListUsuarios();
        jTextEnviar = chatClientForm.getjTextFieldTextoAenviar();
        jToggleConectar = chatClientForm.getjToggleButtonConectar();

        chatClientForm.setVisible(true);
        chatClientForm.getjTextFieldIPServidor().setText("192.168.100.134");
        chatClientForm.getjTextFieldPuerto().setText("9990");

        jToggleConectar = chatClientForm.getjToggleButtonConectar();

        // detectamos si pulsamos a conectar el chat o no
        jToggleConectar.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                conectado = e.getStateChange() == ItemEvent.SELECTED;
                if(conectado){
                cerrarconexion=false;
                }
                // cargamos en un hilo el chat
                Thread hiloChat = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (!cerrarconexion) {
                            iniciarChat();
                        }
                    }
                });
                hiloChat.start();

          
            }
        });

    }

    // método para cargar la lista de usuarios 
    
    private void cargarNicks(Chat chat)
    {
    
               
        // Obtenenemos el TreeMap de chat
        TreeMap<Usuario, String> chats = chat.getChat();
        
        // Crear un modelo para el JList
        DefaultListModel<String> listaNicks = new DefaultListModel<>();
        
        // Recorremos el TreeMap y agregamos los valores al modelo
        for (Entry<Usuario, String> entry : chats.entrySet()) {
            Usuario usuario = entry.getKey();
           
            String nick = usuario.getNick();
            listaNicks.addElement(nick);
        }
        
        // Crear el JList con el modelo
        JList<String> jlist = new JList<>(listaNicks);
        
        chatClientForm.getjListUsuarios().setModel(listaNicks);
        
        
    
    }    
    
}
