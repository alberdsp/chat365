/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;

import java.io.Serializable;



/**
 *  Clase que implementa el mensaje que será transmitido entre
 * servidor y clientes
 * @author Alber
 */
public class Mensaje implements Serializable {
    


    private Usuario origen;
    private Usuario destino;
    private String mensaje;
    
    
    /**
     *  Constructor con parámetros
     * @param origen  objeto usuario que envia
     * @param destino  objeto usuario que debe recibir el mensaje
     * @param mensaje  String mensaje
     */
    public Mensaje (Usuario origen,Usuario destino, String mensaje){
    
    this.origen = origen;
    this.destino = destino;
    this.mensaje = mensaje;
    
    }
    
    // constructor por defecto
    public Mensaje(){
    
    
    }
    
    
    // getters y setters
    public Usuario getOrigen(){
    
    return this.origen;
    }
    
    public void setOrigen( Usuario origen){
    
    this.origen = origen;
    }
    
    public Usuario getDestino(){
    return this.destino;
    }
    
    public void setDestino(Usuario destino){
    
    this.destino = destino;
    }
    
    public String getMensaje(){
    return this.mensaje; 
    }
    
    public void setMensaje(String mensaje){
    this.mensaje = mensaje;
    }
    
    
    
}
