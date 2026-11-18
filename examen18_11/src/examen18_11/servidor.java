package examen18_11;


//Código único otorgado por el servidor del profesor: ClaveSecreta2026
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class servidor {

    // Colores de consola
    static final String RESET = "\u001B[0m";
    static final String VERDE = "\u001B[32m";
    static final String CIAN = "\u001B[36m";
    static final String AMARILLO = "\u001B[33m";

    // Lista de clientes conectados
    static Set<PrintWriter> clientes =
        ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {

        try {
            // Creamos el servidor
            ServerSocket servidor = new ServerSocket(5000);

            System.out.println(VERDE +
                "Servidor iniciado en puerto 5000" + RESET);

            // Esperamos clientes continuamente
            while (true) {

                Socket socket = servidor.accept();

                System.out.println(CIAN +
                    "Nuevo cliente conectado: " +
                    socket.getInetAddress() + RESET);

                // Creamos un hilo para cada cliente
                Thread hilo = new Thread(
                    new ManejadorCliente(socket)
                );

                hilo.start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor");
        }
    }

    
    public static void broadcast(String mensaje) {

        for (PrintWriter cliente : clientes) {

            synchronized (cliente) {
                cliente.println(mensaje);
            }
        }
    }

   
    static class ManejadorCliente implements Runnable {

        Socket socket;
        BufferedReader entrada;
        PrintWriter salida;

        ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                // Preparamos los canales de comunicacion
                entrada = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(), "UTF-8"
                    )
                );

                salida = new PrintWriter(
                    new OutputStreamWriter(
                        socket.getOutputStream(), "UTF-8"
                    ), true
                );

              
                clientes.add(salida);

              
                salida.println("BIENVENIDO AL SERVIDOR LOCAL");

                String mensaje;

               
                while ((mensaje = entrada.readLine()) != null) {

                    System.out.println(
                        AMARILLO + "Mensaje recibido: " +
                        mensaje + RESET
                    );

                  
                    if (mensaje.equals("/hi")) {

                        salida.println(
                            "BIENVENIDO AL SERVIDOR LOCAL"
                        );

                    }

                   
                    else if (mensaje.equals("/logout")) {

                        salida.println("Desconectando...");
                        break;

                    }

                  
                    else if (mensaje.startsWith("/")) {

                        salida.println("COMANDO DESCONOCIDO");

                    }

                    // Mensajes normales
                    else {

                        broadcast(mensaje);

                    }
                }

            } catch (IOException e) {

                System.out.println("Cliente desconectado");

            } finally {

                
                if (salida != null) {
                    clientes.remove(salida);
                }

             
                try {
                    socket.close();
                } catch (IOException e) {
                    
                }
            }
        }
    }
}


	



