/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alber
 */
public class ClienteController implements Runnable {

    private HashMap<String, Usuario> usuarios;
    private TreeMap<String, String> chats;
    private ChatClientForm chatClientForm;
    private Servidor servidor;

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
        this.usuarios = new HashMap<>();

    }

    @Override
    public void run() {

        initComponents();

    }

    private void iniciarChat() {

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
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            DataInputStream dis = new DataInputStream(s.getInputStream());
            jToggleConectar.setText("ON");

            // Mientras que el cliente no mande el mensaje "exit" sigue pidiendo datos.
            // Pido al cliente que escriba un mensaje
            //chatClientForm.getjTextAreaSala().setText("Escriba mensaje > ");
            //   Método listener al hacer clic
            jButtonEnviar.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mensajetxt = chatClientForm.getjTextFieldTextoAenviar().getText();
                    try {
                        dos.writeUTF(mensajetxt);
                        mensajetxt = "";
                        chatClientForm.getjTextFieldTextoAenviar().setText("");
                    } catch (IOException ex) {
                        Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            });

            String prueba;
            while (conectado) {

                // Muestro por pantalla el mensaje que me env�a el servidor mediante el InputStream
          
           
                chatClientForm.getjTextAreaSala().setText(dis.readUTF() + "\n");

            }
            // Una vez finalizado cierro los flujos de entrada/salida y el socket "s"
             
            
            dos.writeUTF("exit");
            dos.close();
            dis.close();
            s.close();
            
            


            jToggleConectar.setText("OFF");
            
            cerrarconexion = true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

}
