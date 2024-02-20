/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
import com.abf.chatclient.modelo.Mensaje;
import com.abf.chatclient.modelo.Servidor;
import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ClientForm;
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
import java.io.EOFException;
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

    private ClientForm clientForm;
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

    public ClienteController(ClientForm chatClientForm) {

        this.clientForm = chatClientForm;
        this.chatcliente = new Chat();
        this.servidor = new Servidor();

    }

    @Override
    public void run() {

        initComponents();

    }

    private void iniciarChat() {

        String ip = clientForm.getjTextFieldIPServidor().getText();
        int puerto = Integer.parseInt(clientForm.getjTextFieldPuerto().getText());
        servidor.setIp(ip);
        servidor.setPuerto(puerto);
        usuario = new Usuario();
        destino = new Usuario("CHATGENERAL", servidor.getIp(), servidor.getPuerto(), true);
        String nick = clientForm.getjTextFieldNick().getText();
        usuario.setNick(nick);
        enviar(usuario);

        jToggleConectar.setText("ON");
        
        // iniciamos la escucha
        servidorEscucha();

    }

    private void initComponents() {

        jButtonEnviar = clientForm.getjButtonEnviar();
        jTextAreaChat = clientForm.getjTextAreaSala();
        jListUsuarios = clientForm.getjListUsuarios();
        jTextEnviar = clientForm.getjTextFieldTextoAenviar();
        jToggleConectar = clientForm.getjToggleButtonConectar();

        clientForm.setVisible(true);
        clientForm.getjTextFieldIPServidor().setText("192.168.100.134");
        clientForm.getjTextFieldPuerto().setText("9990");

        jToggleConectar = clientForm.getjToggleButtonConectar();

        // listener de enviar mensajes
        jButtonEnviar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Construye el mensaje a enviar
                Mensaje mensajenv = new Mensaje();
                mensajenv.setDestino(destino);
                mensajenv.setOrigen(usuario);
                mensajetxt = clientForm.getjTextFieldTextoAenviar().getText();
                mensajenv.setMensaje(mensajetxt);

                // Envía el mensaje
                enviar(mensajenv);
                clientForm.getjTextAreaSala().append("yo: " + mensajetxt + "\n");

                // Limpia el campo de texto
                clientForm.getjTextFieldTextoAenviar().setText("");

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
          
            Usuario usuariobusc = entry.getKey();

            String nick = usuariobusc.getNick();
            listaNicks.addElement(nick);
            
            // cuando repasemos por el nick recogemos el puerto asignado
            if (nick.equals(usuario.getNick())){
            
              usuario.setPuerto(usuariobusc.getPuerto());
            
                
            }
            
            

        }

        // Crear el JList con el modelo
        JList<String> jlist = new JList<>(listaNicks);

        clientForm.getjListUsuarios().setModel(listaNicks);

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
                    ServerSocket serverSocket = null;

        try {
            // Creamos el servidor que escucha siempre en el puerto especificado
            serverSocket = new ServerSocket(usuario.getPuerto());
           

            while (true) {
                // Aceptar una nueva conexión
                Socket socket = serverSocket.accept();

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
                    while (true) {

                        Object objetoRecibido;
                        try {
                            objetoRecibido = ois.readObject();

                            // Verificar si el objeto recibido es un Mensaje
                            if (objetoRecibido instanceof Mensaje) {
                                Mensaje mensaje = (Mensaje) objetoRecibido;
                                String mensajetxt = mensaje.getMensaje();

                                // Si el mensaje es "salir", cerrar el socket y salir del método run
                                if ("salir".equals(mensajetxt)) {
                                    clientForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " ha salido del chat.\n");

                                    //  chat.put(usuario, "sale");
                                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                                }
                                if ("cerrar".equals(mensajetxt)) {
                                    clientForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " cerró conexión .\n");
                                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                                }

                                // Procesamiento normal de mensajes no relacionados con "salir"
                                clientForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " : " + mensajetxt + "\n");
                                // enviamos el mensaje a todos

                              

                 

                            } // Procesamiento de otros tipos de objetos como Usuario, etc.
                            else if (objetoRecibido instanceof Usuario) {
                                usuario = (Usuario) objetoRecibido;

                                clientForm.getjTextAreaSala().append("Se ha conectado " + usuario.getNick()
                                       );

                            }else if (objetoRecibido instanceof Chat) {

                                Chat newchat = new Chat();
                                newchat = (Chat) objetoRecibido;

                                // actualizamos nuestra lista
                                cargarNicks(newchat);

                            }
 

                        } catch (EOFException e) {
                            System.out.println("Cliente ha cerrado la conexión.");
                            break; // Salir del bucle
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
              
        });

        hiloEscucha.start();
    }

}
