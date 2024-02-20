/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;
import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Clase usuario que puede usar el chat, la hacemos comparable
 * para que sea clave de las listas y diferenciarse por el nick
 * @author Alber
 */
public class Usuario implements Comparable<Usuario>,Serializable {
    
    private static final long serialVersionUID = 1L;
    
    String nick;
    String ip;
    int puerto;
    Boolean online;
    
    
    /**
     *  Constructor con parámetros
     * @param nick    nick del usuario
     * @param ip      ip del usuario
     * @param puerto  puerto del usuario
     */
    
    public Usuario ( String nick, String ip, int puerto, Boolean online) {
    this.nick = nick;
    this.ip = ip;
    this.puerto = puerto;
    this.online = online;
    
    }

    public Usuario() {
       
    }
    
     @Override
    public int compareTo(Usuario otroUsuario) {
        return this.nick.compareTo(otroUsuario.getNick());
    }
    
    public String getNick(){
    return this.nick;
    }
    
    public void setNick(String nick){
    this.nick = nick;
    }
    
    public String getIp(){
    return this.ip;
    }
    
    public void setIP(String ip){
    this.ip = ip;
    }
    
    public int getPuerto(){
    return this.puerto;
    }
    
    public void setPuerto( int puerto){
    this.puerto = puerto;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }
    
    
    
    /**
     * método que devuelve un socket en base a la ip y puerto del usuario
     * @return  socket   
     */
    public  Socket getSocket() {
       
          
            Socket socketCliente;
        try {
            socketCliente = new Socket(this.ip, this.puerto);
            
            return socketCliente;
        } catch (IOException ex) {
            Logger.getLogger(Usuario.class.getName()).log(Level.SEVERE, null, ex);
        }
            
       
      
        return null;
    }

    
    /**
     *  Método que recibe un socket y guarda ip y puerto
     * @param socket 
     */
    public void setSocket(Socket socket){
        
        InetAddress addres = socket.getInetAddress();
        this.ip = addres.getHostAddress();
        this.puerto = socket.getPort();
       
    }
    
    
    
    
    
}
