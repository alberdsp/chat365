/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Chat;
import com.abf.chatclient.modelo.Mensaje;
import com.abf.chatclient.modelo.Servidor;
import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ClienteForm;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import java.awt.event.*;
import java.io.EOFException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Clase que controla el chat en la parte del cliente
 *
 * @author Alber
 */
public class ClienteController implements Runnable {

    private Chat chatcliente;
    private ClienteForm clienteForm;
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

    // constructor
    public ClienteController(ClienteForm chatClientForm) {

        this.clienteForm = chatClientForm;
        this.chatcliente = new Chat();
        this.servidor = new Servidor();

    }

    @Override
    public void run() {

        initComponents();

    }

    private void iniciarChat() {

        String ip = clienteForm.getjTextFieldIPServidor().getText();
        int puerto = Integer.parseInt(clienteForm.getjTextFieldPuerto().getText());
        servidor.setIp(ip);
        servidor.setPuerto(puerto);
        usuario = new Usuario();
        destino = new Usuario("SALA_CHAT", servidor.getIp(), servidor.getPuerto(), true);
        String nick = clienteForm.getjTextFieldNick().getText();
        usuario.setNick(nick);
        enviar(usuario);

        jToggleConectar.setText("ON");

        // iniciamos la escucha
        servidorEscucha();

    }

    private void initComponents() {

        jButtonEnviar = clienteForm.getjButtonEnviar();
        jTextAreaChat = clienteForm.getjTextAreaSala();
        jListUsuarios = clienteForm.getjListUsuarios();
        jTextEnviar = clienteForm.getjTextFieldTextoAenviar();
        jToggleConectar = clienteForm.getjToggleButtonConectar();

        clienteForm.setVisible(true);
        clienteForm.getjTextFieldIPServidor().setText("192.168.100.134");
        clienteForm.getjTextFieldPuerto().setText("9990");

        jToggleConectar = clienteForm.getjToggleButtonConectar();

        // listener de enviar mensajes
        jButtonEnviar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // establece el usuario clicado para el destino
                usuarioClicado();

                // Construye el mensaje a enviar
                Mensaje mensajenv = new Mensaje();
                mensajenv.setDestino(destino);
                mensajenv.setOrigen(usuario);
                mensajetxt = clienteForm.getjTextFieldTextoAenviar().getText();
                mensajenv.setMensaje(mensajetxt);
                // Envía el mensaje
                enviar(mensajenv);

                // actualizamos el mensaje para que salga en mi ventana
                mensajenv.setMensaje(mensajetxt);
                // mandamos a procesar el mensaje         
                procesarMensaje(mensajenv);

                try {
                    Thread.sleep(01);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                }

                //    clienteForm.getjTextAreaSala().append("yo: " + mensajetxt + "\n");
                // Limpia el campo de texto
                clienteForm.getjTextFieldTextoAenviar().setText("");

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

        jListUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Añadir ListSelectionListener a la lista
        jListUsuarios.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {

                    // detectamos el clic y ejecutamos   
                    actualizarVentanaChat();

                    // compribamos el usuario clicado
                    usuarioClicado();

                }
            }
        });

    }

    /**
     * método para cargar los nicks al chat en el formulario y en la sala de
     * chat
     *
     * @param chatrecibido recibe un objeto chat, solo tenemos en cuenta los
     * usuarios sin texto.
     */
    private void cargarNicks(Chat chatrecibido) {

        // Obtenenemos el Map de chat
        LinkedHashMap<Usuario, String> chats = chatrecibido.getChat();

        // Crear un modelo para el JList
        DefaultListModel<String> listaNicks = new DefaultListModel<>();

        // Recorremos el Map y agregamos los valores al modelo
        for (Entry<Usuario, String> entry : chats.entrySet()) {

            Usuario usuariobusc = entry.getKey();

            String nick = usuariobusc.getNick();

            if (!nick.equals(usuario.getNick())) {

                listaNicks.addElement(nick);

            }

            // Verificar si este.nick ya existe en nuestro chat
            //   boolean existe = chatcliente.getChat().containsKey(usuariobusc);
            boolean existe = false;
            for (Usuario usuario : chatcliente.getChat().keySet()) {
                if (usuario.getNick().equals(usuariobusc.getNick())) {
                    existe = true;
                    break; // Salir del bucle una vez encontrado el usuario
                }
            }

            // Si no existe, lo añadimos con la conversación vacia
            if (!existe) {

                chatcliente.getChat().put(usuariobusc, ""); // Asumiendo que el valor es un String vacío o el valor adecuado
            }

            // cuando repasemos por el nick recogemos el puerto asignado
            if (nick.equals(usuario.getNick())) {

                usuario.setPuerto(usuariobusc.getPuerto());

            }

        }

        // Crear el JList con el modelo
        if (!listaNicks.isEmpty()) { // Verificar que la lista no esté vacía

            jListUsuarios.setModel(listaNicks);
            jListUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            clienteForm.getjListUsuarios().setSelectedIndex(0);
        }

    }

    /**
     * Método para enviar objetos al servidor
     *
     * @param objeto puede ser un ensaje o un usuario
     */
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
     * notificaciones y mensajes cuando sea necesario del servidor
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
                                            clienteForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " ha salido del chat.\n");

                                            //  chat.put(usuario, "sale");
                                            break; // Sale del bucle while, lo que lleva al cierre de recursos
                                        }
                                        if ("cerrar".equals(mensajetxt)) {
                                            clienteForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " cerró conexión .\n");
                                            break; // Sale del bucle while, lo que lleva al cierre de recursos
                                        }

                                        // Procesamiento normal de mensajes no relacionados con "salir"
                                        //    clienteForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " : " + mensajetxt + "\n");
                                        // enviamos el mensaje a todos
                                        procesarMensaje(mensaje);

                                        try {
                                            Thread.sleep(01);
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    } // Procesamiento de otros tipos de objetos como Usuario, etc.
                                    else if (objetoRecibido instanceof Usuario) {
                                        usuario = (Usuario) objetoRecibido;

                                        clienteForm.getjTextAreaSala().append("Se ha conectado " + usuario.getNick()
                                        );

                                    } else if (objetoRecibido instanceof Chat) {

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

                            System.out.println(e);
                        }

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

    /**
     * actualizar los chats
     */
    public void actualizarVentanaChat() {

        String nickdestino = clienteForm.getjListUsuarios().getSelectedValue();

        // declaramos mapa para recorrer
        LinkedHashMap<Usuario, String> chat = chatcliente.getChat();

        // iteramos para buscar el ususario destino clicado
        for (Entry<Usuario, String> entrada : chat.entrySet()) {

            Usuario usuario = entrada.getKey(); //traemos usuario
            String conversacion = entrada.getValue();// traemos conversación
            // si encontramos nick en la sala 

            // si no es nulo
            if (nickdestino != null) {
                if (nickdestino.equals(usuario.getNick())) {

                    clienteForm.getjTextAreaSala().setText(conversacion);

                }
            }
        }

    }

    /**
     * Método que establece el destino según el item jList clicado.
     *
     */
    public void usuarioClicado() {

        Usuario usuarioitera = new Usuario();

        String nickclicado = clienteForm.getjListUsuarios().getSelectedValue();

        // declaramos mapa para recorrer
        LinkedHashMap<Usuario, String> chat = chatcliente.getChat();

        // iteramos para buscar el ususario  clicado
        for (Entry<Usuario, String> entrada : chat.entrySet()) {

            usuarioitera = entrada.getKey(); //traemos usuario

            String nickitera = usuarioitera.getNick();

            // si no es nulo
            if (nickclicado != null) {
                if (nickclicado.equals(nickitera)) {

                    this.destino = usuarioitera; // establecemos el destino
                    break;
                }
            }
        }

    }

    /**
     * Método para procesar los mensajes
     *
     * @param mensaje recibe un objeto tipo mensaje
     */
    public void procesarMensaje(Mensaje mensaje) {

        String mensajetxt = mensaje.getMensaje();
        String nickdestino = mensaje.getDestino().getNick();
        Usuario destinomensaje = mensaje.getDestino();
        Usuario origenmensaje = mensaje.getOrigen();
        String nickorigen = mensaje.getOrigen().getNick();
        String minick = this.usuario.getNick();

        // vemos quien manda el mensaje
        if (nickorigen.equalsIgnoreCase(minick)) {
            //  ponemos yo:
            mensajetxt = "yo : " + mensajetxt;
        } else {
            // si el mensaje no lo ha mandado el usuario ponemos nick
            mensajetxt = nickorigen + " : " + mensajetxt;

        }

        // declaramos mapa para recorrer
        LinkedHashMap<Usuario, String> chat = new LinkedHashMap<>(chatcliente.getChat());

        // iteramos para buscar el ususario destino clicado
        for (Entry<Usuario, String> entrada : chat.entrySet()) {

            Usuario usuarioiterado = entrada.getKey(); //traemos usuario
            String conversacion = entrada.getValue();// traemos conversación
            String nickiterado = usuarioiterado.getNick();

            // si encontramos nick en la sala 
            // si no es nulo, buscamos en el chat el nick que nos escribió
            if (nickdestino.equals("SALA_CHAT")) {

                if (nickorigen != null) {
                    if (nickdestino.equals(nickiterado)) {

                        String valorActual = entrada.getValue();
                        String nuevoValor = "";

                        // si es el primer mensaje no hay retorno de carro
                        if (valorActual.equals("")) {

                            nuevoValor = mensajetxt;

                        } else {

                            nuevoValor = valorActual + "\n" + mensajetxt;

                        }

                        chatcliente.getChat().put(destinomensaje, nuevoValor);

                    }

                }

            } else {

                if (nickorigen != null) {
                    if (nickorigen.equals(nickiterado)) {

                        String valorActual = entrada.getValue();
                        String nuevoValor = "";

                        // si es el primer mensaje no hay retorno de carro
                        if (valorActual.equals("")) {

                            nuevoValor = mensajetxt;

                        } else {

                            nuevoValor = valorActual + "\n" + mensajetxt;

                        }

                        chatcliente.getChat().put(destinomensaje, nuevoValor);
                    }

                }
            }
        }

        // Procesamiento normal de mensajes no relacionados con "salir"
        // clienteForm.getjTextAreaSala().append(mensaje.getOrigen().getNick() + " : " + mensajetxt + "\n");
        actualizarVentanaChat();

    }

}
