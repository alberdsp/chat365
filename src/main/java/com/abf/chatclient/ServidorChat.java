package com.abf.chatclient;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */



import java.net.*;
import java.io.*;

public class ServidorChat {
	public static void main(String[] args) {
		// Aqu� guardo la cadena que me env�a el cliente.
		String dato_cliente="";
		try {
			// Creo un ServerSocket en el puerto 7040
			ServerSocket ss=new ServerSocket(7040);
			// Muestro un mensaje cuando pongo el servidor a la escucha
			System.out.println("Servidor "+ss.getInetAddress()+" escuchando en el puerto "+ss.getLocalPort()+"...");
			// Pongo el ServerSocket a la escucha en el puerto, y cuando se conecte un cliente guardo el Socket de conexi�n en "s"
			Socket s=ss.accept();
			// Muestro un mensaje cuando se conecta un cliente
			System.out.println("Se ha conectado un cliente "+s.getInetAddress()+" al puerto "+s.getPort());
			// Inicializo los flujos de entrada/salida con el Socket "s" del cliente.
			DataOutputStream dos=new DataOutputStream(s.getOutputStream());
			DataInputStream dis=new DataInputStream(s.getInputStream());
			// Mientras que el dato recibido del cliente sea distinto de "exit" sigo recibiendo datos
			while(!dato_cliente.equals("exit")) {
				// Leo desde el InputStream el dato enviado por el cliente
				dato_cliente = dis.readUTF();
				// Lo muestro por pantalla
				System.out.println(dato_cliente);
				// Env�o a trav�s del OutputStream un mensaje de respuesta al cliente.
				dos.writeUTF("Mensaje Servidor:"+dato_cliente);
			}
			// Una vez finalizado cierro los flujos de entrada/salida y los Sockets
			dos.close();
			dis.close();
			s.close();
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
