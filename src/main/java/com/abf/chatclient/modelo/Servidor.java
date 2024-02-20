/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;

/**
 *  Clase que comtempla un objeto servidor de conexiones
 * @author Alber
 */
public class Servidor {
    
    String ip;
    int puerto;


    /**
     * constructor con parametros
     * @param ip    ip del servidor ejem. 102.168.1.10)
     * @param puerto  puerto ejemplo (9990)
     */
    public Servidor (String ip, int puerto) {
    
        this.ip = ip;
        this.puerto = puerto;
    
    }

    public Servidor() {
    }
    
    // getters y setters
    public void setIp( String ip){
    this.ip = ip;
    }
    
     public String getIp(){   
     return this.ip;
     }
     
     public void setPuerto(int puerto){
     this.puerto = puerto;
     }
     
     public int getPuerto(){
     return this.puerto;
     }
    
 
    
    
    
    
}
