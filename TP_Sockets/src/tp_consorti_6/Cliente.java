package tp_consorti_6;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Cliente {
    private static final String IP_SERVIDOR = "127.0.0.1";
    private static final int PUERTO = 6767;
    private static final int CLAVE_ENCRIPTACION = 4;

   
    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String AZUL = "\u001B[34m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String ROJO = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    public static void main(String[] args) {
        log("SISTEMA", AMARILLO, "Conectando al servidor " + IP_SERVIDOR + ":" + PUERTO + "...");

        try (Socket socket = new Socket(IP_SERVIDOR, PUERTO);
             DataInputStream entrada = new DataInputStream(socket.getInputStream());
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            log("CONEXION", VERDE, "¡Conexión establecida con éxito!");

           
            String saludoServidor = desencriptar(entrada.readUTF());
            log("SERVIDOR", AZUL, saludoServidor);

            boolean activo = true;

            while (activo) {
                
                System.out.print("\n" + CYAN + "[Ignacio Cordoba - Cliente] > " + RESET);
                String mensaje = scanner.nextLine();

                if (mensaje.trim().isEmpty()) continue;

                salida.writeUTF(encriptar(mensaje));
                log("ENVIADO ", VERDE, "Mensaje cifrado [" + encriptar(mensaje) + "] -> Enviado: \"" + mensaje + "\"");

                // Lógica de subir comprobante
                if (mensaje.startsWith("subir comprobante")) {
                    System.out.print("Ingrese la ruta del archivo a enviar (ej: src/Servidor.java): ");
                    String ruta = scanner.nextLine();
                    File archivo = new File(ruta);
                    if (archivo.exists() && archivo.isFile()) {
                        salida.writeLong(archivo.length());
                        FileInputStream fis = new FileInputStream(archivo);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            salida.write(buffer, 0, bytesRead);
                        }
                        fis.close();
                        log("SISTEMA", AMARILLO, "Transferencia de archivo completada.");
                    } else {
                        log("ERROR", ROJO, "Archivo no encontrado. Operación cancelada.");
                        salida.writeLong(0);
                    }
                }

                String respuestaCifrada = entrada.readUTF();
                String respuesta = desencriptar(respuestaCifrada);

                log("RECIBIDO", AZUL, "Respuesta desencriptada:\n" + respuesta);

                if (mensaje.equalsIgnoreCase("salir")) {
                    activo = false;
                    log("SISTEMA", AMARILLO, "Cerrando sesión y liberando recursos...");
                }
            }

        } catch (IOException e) {
            log("ERROR", ROJO, "Error de red: " + e.getMessage());
        }
    }

    private static String encriptar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) sb.append((char) (c + CLAVE_ENCRIPTACION));
        return sb.toString();
    }

    private static String desencriptar(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) sb.append((char) (c - CLAVE_ENCRIPTACION));
        return sb.toString();
    }

    private static void log(String tipo, String color, String mensaje) {
        String hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.printf("[%s] %s%-12s%s \t| %s\n", hora, color, tipo, RESET, mensaje);
    }
}