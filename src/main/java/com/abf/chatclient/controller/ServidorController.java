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
import java.io.EOFException;

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
    private Usuario usuario = null;
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
                                    serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " ha salido del chat.\n");

                                    //  chat.put(usuario, "sale");
                                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                                }
                                if ("cerrar".equals(mensajetxt)) {
                                    serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " cerró conexión .\n");
                                    break; // Sale del bucle while, lo que lleva al cierre de recursos
                                }

                                // Procesamiento normal de mensajes no relacionados con "salir"
                                serverForm.getjTextAreaChatGeneral().append(mensaje.getOrigen().getNick() + " : " + mensajetxt + "\n");
                                // enviamos el mensaje a todos

                                Thread enviarMensajes = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        enviarMensajesSala(mensaje);
                                    }
                                });

                                enviarMensajes.start();
                                try {
                                    enviarMensajes.join();  // Espera a que el hilo enviarMensajes termine
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt(); // Restablece el estado de interrupción
                                }

                            } // Procesamiento de otros tipos de objetos como Usuario, etc.
                            else if (objetoRecibido instanceof Usuario) {
                                usuario = (Usuario) objetoRecibido;

                                serverForm.getjTextAreaChatGeneral().append("Se ha conectado " + usuario.getNick()
                                        + " por la IP :"
                                        + socket.getInetAddress() + " al puerto " + socket.getPort() + "\n");

                                usuario.setSocket(socket);

                                this.chat.getChat().put(usuario, "");
                                // servidorcontroller.getChat().chat.put(usuario, "entra");
                                
                              
                                oos.writeObject(chat);
                                oos.flush();

                                //  servidorcontroller.enviarListaUsuarios();
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

    // método para enviar a todos los usuarios del chat
    public void enviarMensajesSala(Mensaje mensaje) {

        LinkedHashMap<Usuario, String> usuariosChat = chat.getChat();

        for (Usuario usuario : usuariosChat.keySet()) {
            // Verificar que no enviamos al mismo origen o a la "SALA_CHAT"
            //    if (!usuario.getNick().equals("SALA_CHAT") && !mensaje.getOrigen().equals(usuario)) {

            if (!usuario.getNick().equals("SALA_CHAT") && !mensaje.getOrigen().getNick().equals(usuario.getNick())) {
                Socket socket = null;
                ObjectOutputStream oos = null;

                try {
                    // Establecer una nueva conexión con cada usuario
                    socket = new Socket(usuario.getIp(), usuario.getPuerto());
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    // Enviar el mensaje
                    oos.writeObject(mensaje);
                    oos.flush();
                } catch (IOException e) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, "Error al enviar mensaje a " + usuario.getNick(), e);
                    // Manejo de errores, como intentos de reconexión o eliminación de usuario inactivo
                } finally {
                    // Cerrar los recursos
                    try {
                        if (oos != null) {
                            oos.close();
                        }
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión con " + usuario.getNick(), ex);
                    }
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
