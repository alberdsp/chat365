/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient;

import com.abf.chatclient.controller.ClienteController;
import com.abf.chatclient.modelo.vista.ClienteForm;

/**
 *
 * @author Alber
 */
public class MainClient {
    
      
    public static void main(String[] args) {
        
           ClienteForm chatClientForm = new ClienteForm();
           ClienteController clienteController = new ClienteController(chatClientForm);
           
            Thread hilochatcliente = new Thread(clienteController);
           
            hilochatcliente.start();
        
        
        
    }
    
    
}
