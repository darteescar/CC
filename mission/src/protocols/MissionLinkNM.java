package protocols;

import data.Estado;
import data.Mensagem;
import data.Missao;
import data.TipoMensagem;
import core.NaveMae;

import java.io.IOException;
import java.net.*;

public class MissionLinkNM {
    private final int porta;
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private volatile boolean running = true;

    /* ====== Construtor ====== */

    public MissionLinkNM(int porta) throws SocketException {
        this.porta = porta;
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        System.out.println("[NaveMae - ML]: À escuta de ML na porta: " + porta);
    }

    public Mensagem receiveMensagem(NaveMae nm) throws IOException {
        byte[] buffer = new byte[65507];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        Mensagem m = Mensagem.fromByteArray(buffer);

        String id = m.getIdOrg();
        if (!nm.conheceRover(id)) {
            InetAddress ip = m.getIpOrg();
            int porta = m.getPortaOrg();
            Estado e = new Estado();
            nm.adicionaRover(id, ip, porta, e);
        }
        return m;
    }

    public void startMLNaveMae(NaveMae nm) {
        Thread t = new Thread(() -> handlerMLNaveMae(nm), "Thread - MissionLinkNM");
        t.start();
    }

    public void handlerMLNaveMae(NaveMae nm) {
        while (running) {
            try {
                Mensagem m = this.receiveMensagem(nm);
                String idRover = m.getIdOrg();
                TipoMensagem tp = m.getTipo();
                InetAddress ip_org = nm.getIP();
                InetAddress ip_dest = m.getIpOrg();
                int porta_dest = m.getPortaOrg();

                System.out.println("[NaveMae - ML] Recebeu mensagem de: " + idRover + " (" + tp + ")");

                switch (tp) {
                    case ML_SYN -> {
                        System.out.println("[NaveMae - ML] SYN de: " + idRover);

                        Mensagem mSYNACK = new Mensagem(TipoMensagem.ML_SYNACK, 
                                                "NaveMae", 
                                                ip_org, 
                                                this.porta,
                                                idRover, 
                                                ip_dest, 
                                                porta_dest, 
                                                null);

                        // Enviar SYNACK e esperar REQUEST
                        envioML.sendMensagem(mSYNACK.toByteArray(), 
                                            ip_dest, 
                                            porta_dest,
                                            idRover + "_SYN"
                        );
                    }

                    case ML_REQUEST -> {
                        // Confirma o SYNACK (parar retransmissão)
                        envioML.confirmarRecebimento(idRover + "_SYN");

                        System.out.println("[NaveMae - ML] REQUEST de: " + idRover);

                        // Enviar missão
                        Missao missao = nm.getMissaoQueue();
                        Mensagem mDATA = new Mensagem(TipoMensagem.ML_DATA, 
                                              "NaveMae", 
                                              ip_org, 
                                              this.porta,
                                              idRover, 
                                              ip_dest, 
                                              porta_dest, 
                                              missao.toByteArray());

                        // Enviar DATA e esperar CONFIRM
                        envioML.sendMensagem(mDATA.toByteArray(), 
                                            ip_dest, 
                                            porta_dest,
                                            idRover + "_REQ"
                        );
                    }

                    case ML_CONFIRM -> {
                        envioML.confirmarRecebimento(idRover + "_REQ");
                        System.out.println("[NaveMae - ML] CONFIRM de: " + idRover);
                    }

                    default -> System.out.println("[ERRO] Tipo não existente: " + tp);
                }

            } catch (Exception e) {
                System.out.println("[ERRO NaveMae - ML] Handler: " + e.getMessage());
            }
        }
    }

    public void stopMLNaveMae() {
        running = false;
        socket.close();
        envioML.stop();
    }
}
