/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.modelo;
import java.util.TreeMap;
/**
 * Clase que contiene los chats en la parte del cliente
 * @author Alber
 */
public class Chats {
    
   TreeMap<String, String> chats;
    
   
   // constructor por defecto
   public Chats(){
       
       this.chats = new TreeMap<String, String>();
  
   }
   
   
   // getter para devolver el chat.
   public TreeMap<String, String> getChats(){
   
       return this.chats;
   
   }
   
    
}
