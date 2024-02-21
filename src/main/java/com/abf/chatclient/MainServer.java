/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient;

import com.abf.chatclient.controller.ServidorController;
import com.abf.chatclient.modelo.vista.ServidorForm;

/**
 *
 * @author Alber
 */
public class MainServer {
    
    public static void main(String[] args) {
        
           ServidorForm serverForm = new ServidorForm();
           ServidorController servidorController = new ServidorController(serverForm);
           
            Thread hilochat = new Thread(servidorController);
           
            hilochat.start();
        
        
        
    }
    
}
