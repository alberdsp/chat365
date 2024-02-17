/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.abf.chatclient.controller;

import com.abf.chatclient.modelo.Usuario;
import com.abf.chatclient.modelo.vista.ChatClientForm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Alber
 */
public class ClienteController implements Runnable{
    
       private Map<String, Usuario> usuarios;
       private ChatClientForm chatClientForm;
       
       
      public ClienteController(ChatClientForm chatClientForm){
      
      this.chatClientForm = chatClientForm;
      this.usuarios = new HashMap<>();
      
      
      }
       
  

    @Override
    public void run() {
        
        // Inicializamos el Scanner para tomar datos de teclado
		Scanner sc=new Scanner(System.in);
		// Aqu� recojo lo que escribe el cliente
		String dato_cliente="";
		try {
			// Lanzo el socket del cliente para conectar al "localhost" servidor por el puerto 7040
			Socket s=new Socket("192.168.100.134",9990);
			// Muestro el mensaje de conexi�n
			System.out.println("Conectado al servidor por el puerto "+s.getPort());
			// Inicializo los flujos de entrada/salida a trav�s del Socket "s"
			DataOutputStream dos=new DataOutputStream(s.getOutputStream());
			DataInputStream dis=new DataInputStream(s.getInputStream());
			// Mientras que el cliente no mande el mensaje "exit" sigue pidiendo datos.
			while(!dato_cliente.equals("exit")){
				// Pido al cliente que escriba un mensaje
				System.out.print("Escriba mensaje > ");
				dato_cliente=sc.nextLine();
				// Mediante el OutputStream mando el mensaje escrito al servidor
				dos.writeUTF(dato_cliente);
				// Muestro por pantalla el mensaje que me env�a el servidor mediante el InputStream
				System.out.println(dis.readUTF());
			}
			// Una vez finalizado cierro los flujos de entrada/salida y el socket "s"
			dos.close();
			dis.close();
			s.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
        
        
        
       
    }
    
    
    
    
    
    
