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
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author Alber
 */
public class ClienteController implements Runnable {

    private Chat chatcliente;

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
    private Usuario usuario;
    private Usuario destino;

    public ClienteController(ChatClientForm chatClientForm) {

        this.chatClientForm = chatClientForm;
        this.chatcliente = new Chat();
        this.servidor = new Servidor();

    }

    @Override
    public void run() {

        initComponents();

    }

    private void iniciarChat() {

        String ip = chatClientForm.getjTextFieldIPServidor().getText();
        int puerto = Integer.parseInt(chatClientForm.getjTextFieldPuerto().getText());
        servidor.setIp(ip);
        servidor.setPuerto(puerto);
        usuario = new Usuario();
        destino = new Usuario("CHATGENERAL", servidor.getIp(), servidor.getPuerto(), true);
        String nick = chatClientForm.getjTextFieldNick().getText();
        usuario.setNick(nick);
        enviar(usuario);

        jToggleConectar.setText("ON");

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

        // listener de enviar mensajes
        jButtonEnviar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Construye el mensaje a enviar
                Mensaje mensajenv = new Mensaje();
                mensajenv.setDestino(destino);
                mensajenv.setOrigen(usuario);
                mensajetxt = chatClientForm.getjTextFieldTextoAenviar().getText();
                mensajenv.setMensaje(mensajetxt);

                // Envía el mensaje
                enviar(mensajenv);
                chatClientForm.getjTextAreaSala().append("yo: " + mensajetxt + "\n");

                // Limpia el campo de texto
                chatClientForm.getjTextFieldTextoAenviar().setText("");

            }

        });

        // detectamos si pulsamos a conectar el chat on o off
        jToggleConectar.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                conectado = e.getStateChange() == ItemEvent.SELECTED;
                if (conectado) {
                    cerrarconexion = false;
                }

                if (!cerrarconexion) {
                    iniciarChat();
                }
            }
        });

    }

    // método para cargar la lista de usuarios 
    private void cargarNicks(Chat chat) {

        // Obtenenemos el TreeMap de chat
        LinkedHashMap<Usuario, String> chats = chat.getChat();

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

    public void enviar(Object objeto) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket socket = null;

        try {
            // Establecer la conexión con el servidor
            socket = new Socket(servidor.getIp(), servidor.getPuerto());
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            // Envía el objeto (Mensaje o Usuario)
            oos.writeObject(objeto);

            if (objeto instanceof Mensaje) {

            } else if (objeto instanceof Usuario) {

                // mandamos el usuario y esperamos a recibir la lista del chat
                try {
                    Object objetoRecibido = ois.readObject();
                    if (objetoRecibido instanceof Chat) {

                        Chat newchat = new Chat();
                        newchat = (Chat) objetoRecibido;

                        // actualizamos nuestra lista
                        cargarNicks(newchat);

                    }

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        } catch (IOException ex) {
            Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                // Cierra los recursos
                if (oos != null) {
                    oos.close();
                }
                if (ois != null) {
                    ois.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ClienteController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * método para lanzar el servidor de escucha en el cliente, recibirá
     * notificaciones y mensajes cuando sea necesario
     */
    private void servidorEscucha() {

        Thread hiloEscucha;
        hiloEscucha = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket ssocket = new ServerSocket(9000);
                    Socket socket = ssocket.accept();
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    // Escuchamos los mensajes entrantes del servidor
                    while (!cerrarconexion) {

                        try {

                            Object objeto = ois.readObject();

                            // si recibimos mensaje lo lleemos y lo pasamos al chat de destino.
                            if (objeto instanceof Mensaje) {

                                Usuario remitente = new Usuario();
                                remitente = ((Mensaje) objeto).getOrigen();
                                String mensajetxt = ((Mensaje) objeto).getMensaje();
                                chatClientForm.getjTextAreaSala().append(remitente + " : " + mensajetxt);

                                // Actualiza la UI si es necesario, por ejemplo, mostrar el mensaje en el área de texto
                            } else if (objeto instanceof Usuario) {
                                System.out.println("Usuario enviado: " + ((Usuario) objeto).getNick());
                                // Realizar acciones adicionales si es necesario

                            } else if (objeto instanceof Chat) {

                                Chat newchat = new Chat();
                                newchat = (Chat) objeto;

                                // actualizamos nuestra lista
                                cargarNicks(newchat);

                            }

                            System.out.println("Mensaje recibido del servidor: " + mensaje);
                            // Aquí puedes actualizar la UI o manejar el mensaje como sea necesario
                        } catch (ClassNotFoundException e) {
                            System.out.println("Error al leer el objeto del servidor: " + e.getMessage());
                        }
                    }

                    oos.close();
                    ois.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al conectar o escuchar al servidor: " + e.getMessage());
                }
            }
        });

        hiloEscucha.start();
    }

}
