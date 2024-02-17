/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;
import java.io.Serializable;
/**
 * Clase usuario que puede usar el chat, la hacemos comparable
 * para que sea clave de las listas y diferenciarse por el nick
 * @author Alber
 */
public class Usuario implements Comparable<Usuario>,Serializable {
    
    String nick;
    String ip;
    int puerto;
    
    
    /**
     *  Constructor con parámetros
     * @param nick    nick del usuario
     * @param ip      ip del usuario
     * @param puerto  puerto del usuario
     */
    
    public Usuario ( String nick, String ip, int puerto) {
    this.nick = nick;
    this.ip = ip;
    this.puerto = puerto;
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
}
