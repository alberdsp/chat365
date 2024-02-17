/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;
import java.util.TreeMap;
import java.io.Serializable;
/**
 * Clase que contiene los chats, contiene un 
 * @author Alber
 */
public class Chat implements Serializable{
    
   TreeMap<Usuario, String> chat;
    
   
   // constructor por defecto
   public Chat(){
       
       this.chat = new TreeMap<>();
  
   }
   
   
   // getter para devolver el chat.
   public TreeMap<Usuario, String> getChat(){
   
       return this.chat;
   
   }
   
    
}
