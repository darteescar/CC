package protocols.udp;

import data.EstadoOperacional;
import data.Mensagem;
import data.Report;
import data.TipoMensagem;
import data.Missao;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import core.Rover;

public class MissionLinkRover {
    private final String idRover;
    private final int porta;
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private final InetAddress ipNaveMae;
    private final int portaNaveMae;
    private volatile boolean running = true;
    private int ultimaMissao = -1;

    /* ====== Construtor ====== */
    public MissionLinkRover(String idRover, int porta, InetAddress ipNaveMae, int portaNaveMae) throws Exception {
        this.idRover = idRover;
        this.porta = porta;
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        this.ipNaveMae = ipNaveMae;
        this.portaNaveMae = portaNaveMae;
        System.out.println("[" + idRover + " - ML]: Conectado na porta: " + porta);
    }

    public Mensagem receiveMensagem() throws IOException {
        byte[] buffer = new byte[65507];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        Mensagem m = Mensagem.fromByteArray(buffer);
        return m;
    }

    public void startMLRover(Rover rover) {
        Thread t = new Thread(() -> handlerMLRover(rover), "Thread-ML-" + idRover);
        t.start();
    }

    private void handlerMLRover(Rover rover) {
        while (running) {
            try {
                if (rover.getEstado().getEstadoOperacional() == EstadoOperacional.PARADO) {
                    System.out.println("[" + idRover + " - ML]: Estado PARADO → solicitar missão...");
                    rover.getEstado().setEstadoOperacional(EstadoOperacional.ESPERA_MISSAO);

                    Mensagem mSYN = new Mensagem(
                            TipoMensagem.ML_SYN,
                            this.idRover,
                            rover.getIP(),
                            this.porta,
                            "NaveMae",
                            this.ipNaveMae,
                            this.portaNaveMae,
                            null
                    );

                    // Enviar SYN e esperar SYNACK
                    envioML.sendMensagem(mSYN.toByteArray(), 
                                        this.ipNaveMae, 
                                        this.portaNaveMae, 
                                        this.idRover + "_SYN");
                }

                try {
                    Mensagem m = receiveMensagem();
                    if (m == null) {
                        continue;
                    }
                    TipoMensagem tp = m.getTipo();

                    switch (tp) {
                        case ML_SYNACK -> {
                            // Confirma o SYN (parar retransmissão)
                            envioML.confirmarRececao(idRover + "_SYN");
                            System.out.println("[" + idRover + " - ML]: SYNACK de: NaveMae");

                            Mensagem mREQUEST = new Mensagem(TipoMensagem.ML_REQUEST, 
                                                            this.idRover, 
                                                            rover.getIP(), 
                                                            this.porta, 
                                                            "NaveMae", 
                                                            this.ipNaveMae, 
                                                            this.portaNaveMae, 
                                                            null);

                            // Enviar REQUEST e esperar DATA
                            envioML.sendMensagem(mREQUEST.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                this.idRover + "_REQUEST");
                        }

                        case ML_DATA -> {
                            // Confirmar o REQUEST (parar retransmissão)
                            envioML.confirmarRececao(idRover + "_REQUEST");
                            System.out.println("[" + idRover + " - ML]: DATA (Missão) de: NaveMae");

                            Mensagem mCONFIRM = new Mensagem(TipoMensagem.ML_CONFIRM, 
                                                            this.idRover, 
                                                            rover.getIP(), 
                                                            this.porta, 
                                                            "NaveMae", 
                                                            this.ipNaveMae, 
                                                            this.portaNaveMae, 
                                                            null);

                            // Envia CONFIRM e não espera por nada
                            envioML.sendMensagem(mCONFIRM.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                this.idRover + "_CONFIRM");

                            Missao missaoRecebida = new Missao();
                            missaoRecebida.fromByteArray(m.getPayload());
                            int idMisaoRecebida = missaoRecebida.getId();
                            
                            if(idMisaoRecebida != ultimaMissao){
                                this.ultimaMissao = idMisaoRecebida;
                                rover.setMissaoAtual(missaoRecebida);
                            }
                        }
                        case ML_CONFIRM_ACK -> {
                            // Confirmar o CONFIRM (parar retransmissão)
                            envioML.confirmarRececao(idRover + "_CONFIRM");
                            System.out.println("[" + idRover + " - ML]: CONFIRM_ACK de: NaveMae");
                            System.out.println("[" + idRover + " - ML]: AGORA POSSO FAZER A MISSAO");
                            rover.executaMissao(rover.getMissaoAtual());
                        }

                        default -> System.out.println("[ERRO] Tipo não existente " + tp);
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("[ERRO " + idRover + " - ML] Problema no switch: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("[ERRO " + idRover + " - ML] Handler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handlerReportMissao(Rover rover, Missao misao, int numReport){
        try{
            String path = "resources/marte_1.jpg";
            if((numReport % 2) == 0) path = "resources/marte_2.jpg";

            // Converter a imagem para um byte[]
            BufferedImage img = ImageIO.read(new File(path));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] imgBytes = baos.toByteArray();

            int tamMax = 4096;
            int numFrames = (imgBytes.length + tamMax -1) / tamMax;
            String idReport = misao.getId() + "-" + numReport;
            System.out.println("[" + idRover + " - ML]: Começar Report: " + idReport);

            // Serializar o idReport e numFrames
            byte[] texto = idReport.getBytes();
            ByteBuffer bb = ByteBuffer.allocate(4 + 4 + texto.length);
            bb.putInt(numFrames);
            bb.putInt(texto.length);
            bb.put(texto);

            byte[] payloadFRAMES = bb.array();

            Mensagem mFRAMES = new Mensagem(TipoMensagem.ML_FRAMES, 
                                                this.idRover, 
                                                rover.getIP(), 
                                                this.porta, 
                                                "NaveMae", 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                payloadFRAMES

            );

            // Envia FRAMES e espera por OK
            envioML.sendMensagem(mFRAMES.toByteArray(), 
                                this.ipNaveMae, 
                                this.portaNaveMae, 
                                idReport + "_FRAMES"
            );

            System.out.println("[" + idRover + " - ML]: Enviou FRAMES para NaveMae");

            boolean recebido = false;
            while(!recebido){
                try{
                    Mensagem m = receiveMensagem();
                    TipoMensagem tp = m.getTipo();

                    switch(tp){

                        case ML_OK -> {
                            // Confirmar o FRAMES (parar retransmissão)
                            envioML.confirmarRececao(idReport + "_FRAMES");
                            System.out.println("[" + idRover + " - ML] OK de: NaveMae");

                            // Envia todas as frames da imagem (REPORT) e nao espera por nada
                            for(int i = 0; i < numFrames; i++){
                                int inicio = i * tamMax;
                                int fim = Math.min(inicio + tamMax, imgBytes.length);
                                byte[] payloadREPORT = Arrays.copyOfRange(imgBytes, inicio, fim);

                                Mensagem mREPORT = new Report(TipoMensagem.ML_REPORT, 
                                            this.idRover, 
                                            rover.getIP(), 
                                            this.porta, 
                                            "NaveMae", 
                                            this.ipNaveMae, 
                                            this.portaNaveMae, 
                                            payloadREPORT,
                                            idReport,
                                            i
                                );

                                envioML.sendMensagem(mREPORT.toByteArray(), 
                                                    this.ipNaveMae, 
                                                    this.portaNaveMae, 
                                                    null
                                );
                                System.out.println("[" + idRover + " - ML] A enviar frame " + i + " da imagem");
                            }

                            // Serializar o idReport
                            ByteBuffer bbEND = ByteBuffer.allocate(4 + texto.length);
                            bbEND.putInt(texto.length);
                            bbEND.put(texto);

                            byte[] payloadEND = bbEND.array();

                            Mensagem mEND = new Mensagem(TipoMensagem.ML_END, 
                                                this.idRover, 
                                                rover.getIP(), 
                                                this.porta, 
                                                "NaveMae", 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                payloadEND

                            );

                            // Envia END e espera um MISS ou FIN (sinaliza fim das frames)
                            envioML.sendMensagem(mEND.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                idReport + "_END"
                            ); 

                            System.out.println("[" + idRover + " - ML]: Enviou END para NaveMae");
                        }

                        case ML_MISS -> {
                            // Confirmar o END (parar retransmissão)
                            envioML.confirmarRececao(idReport + "_END");
                            System.out.println("[" + idRover + " - ML] MISS de: NaveMae");
                            byte[] resposta = m.getPayload();

                            // Reenvia todas as frames da imagem (REPORT) e nao espera por nada
                            for(int i = 0; i < resposta.length; i++){
                                if(resposta[i] == 0){
                                    int inicio = i * tamMax;
                                    int fim = Math.min(inicio + tamMax, imgBytes.length);
                                    byte[] payload = Arrays.copyOfRange(imgBytes, inicio, fim);

                                    Mensagem mREPORT = new Report(TipoMensagem.ML_REPORT, 
                                                this.idRover, 
                                                rover.getIP(), 
                                                this.porta, 
                                                "NaveMae", 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                payload,
                                                idReport,
                                                i
                                    );

                                    envioML.sendMensagem(mREPORT.toByteArray(), 
                                                        this.ipNaveMae, 
                                                        this.portaNaveMae, 
                                                        null
                                    );
                                    System.out.println("[" + idRover + " - ML] A reenviar frame " + i + " da imagem");
                                }
                            }

                            // Serializar o idReport
                            ByteBuffer bbEND = ByteBuffer.allocate(4 + texto.length);
                            bbEND.putInt(texto.length);
                            bbEND.put(texto);

                            byte[] payloadEND = bbEND.array();

                            Mensagem mEND = new Mensagem(TipoMensagem.ML_END, 
                                                this.idRover, 
                                                rover.getIP(), 
                                                this.porta, 
                                                "NaveMae", 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                payloadEND

                            );

                            System.out.println("[" + idRover + " - ML]: Reenviou END para NaveMae");

                            // Envia END e espera um MISS ou FIN (sinaliza fim das frames)
                            envioML.sendMensagem(mEND.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                idReport + "_END"
                            );

                        }case ML_FIN -> {
                            // Confirmar o END (parar retransmissão)
                            envioML.confirmarRececao(idReport + "_END");
                            System.out.println("[" + idRover + " - ML] FIN de: NaveMae");

                            // Serializar o idReport
                            ByteBuffer bbFINACK = ByteBuffer.allocate(4 + texto.length);
                            bbFINACK.putInt(texto.length);
                            bbFINACK.put(texto);

                            byte[] payloadFINACK = bbFINACK.array();

                            Mensagem mFINACK = new Mensagem(TipoMensagem.ML_FINACK, 
                                                this.idRover, 
                                                rover.getIP(), 
                                                this.porta, 
                                                "NaveMae", 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                payloadFINACK
                            );

                            // Envia FINACK e nao espera por nada
                            envioML.sendMensagem(mFINACK.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                null
                            );

                            recebido = true;
                            
                        }default -> {
                            break;
                        }
                    }
                }catch(Exception e){
                    System.out.println("[ERRO " + idRover + " - ML] while - Handler Report Missao: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }catch(Exception e){
            System.out.println("[ERRO " + idRover + " - ML] Handler Report Missao: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopMLRover() {
        running = false;
        socket.close();
        envioML.stop();
    }
}

