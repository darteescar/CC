import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String[] args) {
        String ipServidor = "10.0.1.20";
        int porta = 5000;

        try (Socket socket = new Socket(ipServidor, porta)) {
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
            saida.println("Hello world");
            System.out.println("Mensagem enviada: Hello world");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

