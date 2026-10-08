package tp_consorti_6;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Servidor {
    private static final int PUERTO = 6767;
    private static final int CLAVE_ENCRIPTACION = 4;

   
    private static final String RESET = "\u001B[0m";
    private static final String VERDE = "\u001B[32m";
    private static final String AZUL = "\u001B[34m";
    private static final String AMARILLO = "\u001B[33m";
    private static final String ROJO = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    
    private static String[] productos = {"Laptop", "Mouse", "Teclado", "Monitor"};
    private static int[] cantidades = {10, 25, 15, 8};

    public static void main(String[] args) {
        log("SISTEMA", VERDE, "Iniciando Servidor TCP en puerto " + PUERTO + "...");

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            log("SISTEMA", VERDE, "Servidor a la espera de conexión de cliente...");

            
            while (true) {
                Socket socket = serverSocket.accept();
                log("CONEXION", AMARILLO, "Cliente conectado desde: " + socket.getInetAddress());

                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

              
                salida.writeUTF(encriptar("¡Hola! Soy Ignacio Cordoba (Servidor de Soporte). ¿En qué puedo ayudarte?"));

                StringBuilder historialChat = new StringBuilder();
                boolean autenticado = false;
                boolean clienteConectado = true;

                while (clienteConectado) {
                    try {
                        String mensajeCifrado = entrada.readUTF();
                        String mensaje = desencriptar(mensajeCifrado);

                        log("RECIBIDO", CYAN, "Mensaje cifrado [" + mensajeCifrado + "] -> Desencriptado: \"" + mensaje + "\"");
                        historialChat.append("[Cliente]: ").append(mensaje).append("\n");

                        String respuesta = "";

                      
                        if (mensaje.startsWith("subir comprobante")) {
                            long tamano = entrada.readLong();
                            String nombreArchivo = "comprobante_recibido_" + System.currentTimeMillis() + ".dat";
                            FileOutputStream fos = new FileOutputStream(nombreArchivo);
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            long totalRead = 0;
                            while (totalRead < tamano && (bytesRead = entrada.read(buffer, 0, (int)Math.min(buffer.length, tamano - totalRead))) != -1) {
                                fos.write(buffer, 0, bytesRead);
                                totalRead += bytesRead;
                            }
                            fos.close();
                            respuesta = "Comprobante recibido y guardado correctamente en el servidor.";
                            log("ARCHIVO", VERDE, "Comprobante de " + tamano + " bytes guardado como " + nombreArchivo);
                        } 
                       
                        else if (!autenticado && !mensaje.equalsIgnoreCase("autenticar")) {
                            respuesta = "Acceso denegado. Debe ejecutar el comando 'autenticar' para continuar.";
                        } else {
                            switch (mensaje.toLowerCase().trim()) {
                                case "autenticar":
                                    autenticado = true;
                                    respuesta = "Usuario autenticado con éxito. Hola Ignacio Cordoba, sesión iniciada.";
                                    break;
                                case "ayuda":
                                    respuesta = "Menú de Soporte Chatbot:\n" +
                                                "  1. Estado de Envío | 2. Devoluciones | 3. Reclamos\n" +
                                                "  Comandos disponibles:\n" +
                                                "  - autenticar\n" +
                                                "  - ayuda\n" +
                                                "  - ver stock de un producto\n" +
                                                "  - subir comprobante\n" +
                                                "  - ver historial de chat\n" +
                                                "  - salir";
                                    break;
                                case "ver historial de chat":
                                    respuesta = "--- HISTORIAL DE CHAT DE LA SESION ---\n" + historialChat.toString();
                                    break;
                                case "salir":
                                    respuesta = "Conexión finalizada. ¡Hasta luego!";
                                    clienteConectado = false;
                                    break;
                                default:
                                    if (mensaje.toLowerCase().startsWith("ver stock de ")) {
                                        String prodBuscado = mensaje.substring(13).trim();
                                        int stockEncontrado = -1;
                                        for (int i = 0; i < productos.length; i++) {
                                            if (productos[i].equalsIgnoreCase(prodBuscado)) {
                                                stockEncontrado = cantidades[i];
                                                break;
                                            }
                                        }
                                        if (stockEncontrado != -1) {
                                            respuesta = "Stock disponible de '" + prodBuscado + "': " + stockEncontrado + " unidades.";
                                        } else {
                                            respuesta = "El producto '" + prodBuscado + "' no se encuentra en el inventario.";
                                        }
                                    } else if (mensaje.equalsIgnoreCase("Hola, tengo un problema con mi pedido")) {
                                        respuesta = "Menú de Soporte: 1. Estado de Envío | 2. Devoluciones | 3. Reclamos. Por favor, introduzca un comando válido.";
                                    } else {
                                        respuesta = "Comando no reconocido. Escriba 'ayuda' para desplegar el menú de opciones.";
                                    }
                                    break;
                            }
                        }


                        salida.writeUTF(encriptar(respuesta));
                        historialChat.append("[Servidor]: ").append(respuesta).append("\n");
                        log("ENVIADO ", VERDE, "Respuesta enviada: \"" + respuesta.replace("\n", " ") + "\"");

                    } catch (EOFException e) {
                        log("DESCONEXION", ROJO, "El cliente cerró la conexión.");
                        clienteConectado = false;
                    }
                }

                entrada.close();
                salida.close();
                socket.close();
                log("SISTEMA", AMARILLO, "Recursos liberados. Esperando siguiente cliente...");
            }
        } catch (IOException e) {
            log("ERROR", ROJO, "Error en el servidor: " + e.getMessage());
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