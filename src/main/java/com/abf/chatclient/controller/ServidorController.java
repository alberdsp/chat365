/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
import com.abf.chatclient.modelo.Mensaje;
import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ServidorForm;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Clase controladora del chat de servidor
 *
 * @author Alber
 */
public class ServidorController implements Runnable {

    private Chat chat;
    private final ServidorForm serverForm;
    private int puerto = 9990;
    private Usuario usuario = null;
 

    // constructor
    public ServidorController(ServidorForm serverForm) {
        this.serverForm = serverForm;
        this.chat = new Chat();
        Usuario sala = new Usuario("SALA_CHAT", "localhost", 9990, true);
        chat.getChat().put(sala, "");
    }

    @Override
    public void run() {
        serverForm.setVisible(true);
        ServerSocket serverSocket = null;

        try {
            // Creamos el servidor que escucha siempre en el puerto especificado
            serverSocket = new ServerSocket(9990);
            String ipserver = serverSocket.getInetAddress().getHostAddress();
            serverForm.getjTextAreaChatGeneral().append("Servidor escuchando en el puerto "
                    + puerto + "\n");
            serverForm.getjTextAreaChatGeneral().append("Ips a la escucha :"
                    + "\n");
            
            // listamos las ips que tiene el servidor.
            listarIpsServidor();
            

            
            // quedamos a la escucha
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
                           

                                 oos.writeObject(chat);
                                 oos.flush();
                                
                                try {
                                    Thread.sleep(05);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                  
                                  
                                  
                                        Thread enviarUsuarios = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        enviarListaUsuarios();
                                    }
                                });

                                enviarUsuarios .start();
                                try {
                                    enviarUsuarios.join();  // Espera a que el hilo enviarMensajes termine
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt(); // Restablece el estado de interrupción
                                }
                                

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
        
        // cargamos el mapa del chat
        
        LinkedHashMap<Usuario, String> usuariosChat = chat.getChat();

      for (Usuario usuario : usuariosChat.keySet()) {
            // Verificar que no enviamos al mismo origen o a la "SALA_CHAT"
            //    if (!usuario.getNick().equals("SALA_CHAT") && !mensaje.getOrigen().equals(usuario)) {

            if (!usuario.getNick().equals("SALA_CHAT")) {
                Socket socket = null;
                ObjectOutputStream oos = null;

                try {
                    // Establecer una nueva conexión con cada usuario
                    socket = new Socket(usuario.getIp(), usuario.getPuerto());
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    // Enviar chat con los usuarios
                    oos.writeObject(chat);
                    oos.flush();

                    try {
                        // interrumpo el hilo 1 milisegundos 
                        // para que de tiempo a recibir bien el paquete.
                        // de este modo funciona perfectamente
                        Thread.sleep(01);
                    } catch (InterruptedException e) {
                        // El hilo ha sido interrumpido 
                        Thread.currentThread().interrupt(); // Restablece el estado de interrupción
                        System.err.println("Interrupción durante la pausa: " + e.getMessage());
                    }
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

    // método para enviar a todos los usuarios del chat
    public void enviarMensajesSala(Mensaje mensaje) {

        LinkedHashMap<Usuario, String> usuariosChat = chat.getChat();

        for (Usuario usuario : usuariosChat.keySet()) {
            // Verificar que no enviamos al mismo origen o a la "SALA_CHAT"
            //    if (!usuario.getNick().equals("SALA_CHAT") && !mensaje.getOrigen().equals(usuario)) {

            if (!usuario.getNick().equals("SALA_CHAT") &&
                    !mensaje.getOrigen().getNick().equals(usuario.getNick())&&
                   mensaje.getDestino().getNick().equals("CHATGENERAL")) {
                Socket socket = null;
                ObjectOutputStream oos = null;

                try {
                    // Establecer una nueva conexión con cada usuario
                    socket = new Socket(usuario.getIp(), usuario.getPuerto());
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    // Enviar el mensaje
                    oos.writeObject(mensaje);
                    oos.flush();

                    try {
                        // interrumpo el hilo 1 milisegundos 
                        // para que de tiempo a recibir bien el paquete.
                        // de este modo funciona perfectamente
                        Thread.sleep(01);
                    } catch (InterruptedException e) {
                        // El hilo ha sido interrumpido durante el sueño
                        Thread.currentThread().interrupt(); // Restablece el estado de interrupción
                        System.err.println("Interrupción durante la pausa: " + e.getMessage());
                    }
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
    
    public void listarIpsServidor (){
        

        
    try {
        
        
          
        
            // Lista para guardar las direcciones IP como String
            List<String> ipAddresses = new ArrayList<>();

            // Obtenemos una enumeración de todas las interfaces de red
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // Ignoramos las interfaces que estén desactivadas 
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        // Agregar la dirección IP a la lista, excluyendo direcciones IPv6 si se desea
                        if (address.getHostAddress().indexOf(':') == -1) { // Excluir direcciones IPv6
                            ipAddresses.add(address.getHostAddress());
                        }
                    }
                }
            }

            // Imprimir las direcciones IP disponibles el textarea
            for (String ipAddress : ipAddresses) {
                
                serverForm.getjTextAreaChatGeneral().append(" - "+ ipAddress + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
