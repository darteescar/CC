package protocols.udp;

import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvioML {

    // Classe para as Mensagens Pendentes
    private static class MensagemPendente {
        byte[] data;
        InetAddress dest;
        int port;
        long momentoEnvio;

        MensagemPendente(byte[] data, InetAddress dest, int port) {
            this.data = data;
            this.dest = dest;
            this.port = port;
            this.momentoEnvio = System.currentTimeMillis();
        }
    }

    private final DatagramSocket socket;
    private final int TIMEOUT = 2*1000; // 2 segundos
    private volatile boolean running = true;

    // Mensagens pendentes para retransmissão
    private final Map<String, MensagemPendente> pendentes = new ConcurrentHashMap<>();

    /* ====== Construtor ====== */
    public EnvioML(DatagramSocket socket) {
        this.socket = socket;
        startRetransmissor();
    }

    /* ====== Métodos ====== */

    public void sendMensagem(byte[] mensagem, 
                            InetAddress ip, 
                            int porta,
                            String chave
                            ) throws Exception {
                                
        DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length, ip, porta);
        socket.send(pacote);
        if(chave != null){ 
            pendentes.put(chave, new MensagemPendente(mensagem, ip, porta));
        }
    }

    public void confirmarRecebimento(String chave) {
        pendentes.remove(chave);
    }

    private void startRetransmissor() {
        Thread t = new Thread(() -> {
            while (running) {
                try {
                    for (Map.Entry<String, MensagemPendente> e : pendentes.entrySet()) {
                        MensagemPendente p = e.getValue();
                        long tempoPassado = System.currentTimeMillis() - p.momentoEnvio;
                        if (tempoPassado > TIMEOUT) {
                            System.out.println("[ENVIO - ML] TIMEOUT -> Retransmitindo " + e.getKey());
                            DatagramPacket pacote = new DatagramPacket(p.data, p.data.length, p.dest, p.port);
                            socket.send(pacote);
                            p.momentoEnvio = System.currentTimeMillis();
                        }
                    }
                    Thread.sleep(100); // verificação periódica
                } catch (Exception ignored) {}
            }
        }, "Thread - Envio ML");
        t.start();
    }

    public void stop() {
        running = false;
    }
}
