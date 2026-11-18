package examen18_11;

//Código único otorgado por el servidor del profesor: ClaveSecreta2026

import java.net.*;
import java.io.*;

import java.net.*;
import java.io.*;

public class Cliente {

    // Colores de consola
    static final String RESET = "\u001B[0m";
    static final String VERDE = "\u001B[32m";
    static final String CIAN = "\u001B[36m";
    static final String MORADO = "\u001B[35m";
    static final String AMARILLO = "\u001B[33m";

    public static void main(String[] args) {

        try {
            // Solicitamos IP y puerto
            BufferedReader teclado = new BufferedReader(
                new InputStreamReader(System.in)
            );

            System.out.print("Ingrese IP: ");
            String ip = teclado.readLine();

            System.out.print("Ingrese puerto: ");
            int puerto = Integer.parseInt(teclado.readLine());

            // Conexion con el servidor
            Socket socket = new Socket(ip, puerto);

            System.out.println(VERDE +
                "Conectado al servidor" + RESET);

            // Canales de comunicacion
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(), "UTF-8"
                )
            );

            PrintWriter salida = new PrintWriter(
                new OutputStreamWriter(
                    socket.getOutputStream(), "UTF-8"
                ), true
            );

           
            String bienvenida = entrada.readLine();

            System.out.println(CIAN +
                bienvenida + RESET);

            
            Thread receptor = new Thread(() -> {

                try {
                    String mensaje;

                    while ((mensaje = entrada.readLine()) != null) {

                        System.out.println(
                            MORADO + "\n" + mensaje + RESET
                        );
                    }

                } catch (IOException e) {
                    
                }

            });

           
            Thread emisor = new Thread(() -> {

                try {
                    String mensaje;

                    while ((mensaje = teclado.readLine()) != null) {

                        salida.println(mensaje);

                        if (mensaje.equals("/logout")) {
                            socket.close();
                            break;
                        }
                    }

                } catch (IOException e) {
                    
                }

            });

           
            
            receptor.start();
            emisor.start();

          
            emisor.join();
            receptor.join();

            System.out.println(AMARILLO +
                "Conexion finalizada" + RESET);

            teclado.close();

        } catch (Exception e) {

            System.out.println(AMARILLO +
                "No se pudo conectar con el servidor" + RESET);
        }
    }
}

