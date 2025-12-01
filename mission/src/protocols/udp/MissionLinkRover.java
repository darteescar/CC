package protocols.udp;

import core.Rover;
import data.EstadoOperacional;
import data.Mensagem;
import data.Missao;
import data.Report;
import data.TipoMensagem;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import javax.imageio.ImageIO;

public class MissionLinkRover {
    private final String idRover;
    private final int porta;
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private final InetAddress ipNaveMae;
    private final int portaNaveMae;
    private volatile boolean running = true;
    private int ultimaMissao = -1;

    private final Demultiplexer demultiplexer;
    private final Thread thread_demultiplexer;

    /* ====== Construtor ====== */
    public MissionLinkRover(String idRover, int porta, InetAddress ipNaveMae, int portaNaveMae) throws Exception {
        this.idRover = idRover;
        this.porta = porta;
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        this.ipNaveMae = ipNaveMae;
        this.portaNaveMae = portaNaveMae;

        System.out.println("[" + idRover + " - ML]: Conectado na porta: " + porta);

        this.demultiplexer = new Demultiplexer(socket);
        this.thread_demultiplexer = new Thread(demultiplexer, "Demultiplexer-" + idRover);
        this.thread_demultiplexer.start();
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
                    Mensagem m = demultiplexer.getMissaoQueue().take();

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
                                                            null
                            );

                            // Enviar REQUEST e nao espera por nada
                            // Assim nao ha risco de passar missoes a frente
                            envioML.sendMensagem(mREQUEST.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                null 
                            );
                        }

                        case ML_DATA -> {
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
            // Alternar qual imagem vai no report
            String path = "resources/marte_1.jpg";
            if((numReport % 2) == 0) path = "resources/marte_2.jpg";

            // Converter a imagem para um byte[]
            BufferedImage img = ImageIO.read(new File(path));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] imgBytes = baos.toByteArray();

            // Variaveis importantes para o report
            int tamMax = 4096;
            int numFrames = (imgBytes.length + tamMax -1) / tamMax;
            String idReport = misao.getId() + "-" + numReport;

            // Envio de mensagem do tipo FRAMES
            enviaFRAMES(rover.getIP(), idReport, numFrames);

            // Queue do desmultiplexer para as mensagens deste report (idReport)
            BlockingQueue<Report> q = demultiplexer.getReportQueue(idReport);
            
            boolean recebido = false; // Para verificar se o report já foi recebido por completo
            while(!recebido){
                try{
                    Report r = q.take(); // Neste ciclo while todas a msg recebidas são reports
                    String idReportRecebido = r.getIdReport();

                    if(!idReportRecebido.equals(idReport)){
                        // Verficamos se a thread deste report nao recebeu mensagem referente a outro report
                        System.out.println("[" + idRover + " - ML - ERRO] Thread do report " + idReport + " recebeu mensagem (" +r.getTipo() +") do report " + idReportRecebido );
                        System.out.println("[" + idRover + " - ML - ERRO] Ignorar mesagem do report " + idReportRecebido);
                        continue;
                    }

                    TipoMensagem tp = r.getTipo();

                    switch(tp){

                        case ML_OK -> handleOK(r, rover.getIP(), numFrames, tamMax, imgBytes);
                        case ML_MISS -> handleMISS(r, rover.getIP(), numFrames, tamMax, imgBytes);
                        case ML_FIN -> handleFIN(r, rover.getIP()); 
                        case ML_STOP_CON -> {
                            handleSTOP_CON(r, recebido);
                            // Agora sim podemos dar report como concluido e fechar o ciclo while
                            recebido = true;
                        }
                        
                        default -> {
                            break;
                        }
                    }
                }catch(Exception e){
                    System.out.println("[ERRO " + idRover + " - ML] while - Handler Report " + idReport + " " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("[" + idRover + " - ML] WHILE - SAI DO CICLO -> Thread do report " + idReport + " fechou" );

        }catch(Exception e){
            System.out.println("[ERRO " + idRover + " - ML] Handler Report Missao: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enviaFRAMES(InetAddress ip_rover, String idReport, int numFrames) throws Exception{
        System.out.println("[" + idRover + " - ML]: Começar Report: " + idReport);

        Mensagem mFRAMES = new Report(TipoMensagem.ML_FRAMES, 
                                            this.idRover, 
                                            ip_rover,
                                            this.porta, 
                                            "NaveMae", 
                                            this.ipNaveMae, 
                                            this.portaNaveMae, 
                                            null, 
                                            idReport,
                                            numFrames,
                                            -1 // No FRAMES nao interessa o numSeq
        );

        // Envia FRAMES e espera por OK
        envioML.sendMensagem(mFRAMES.toByteArray(), 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            idReport + "_FRAMES"
        );

        System.out.println("[" + idRover + " - ML]: Enviou FRAMES para NaveMae");
    }

    public void handleOK(Report report, InetAddress ip_rover, int numFrames, int tamMax, byte[] imgBytes) throws Exception{
        String idReport = report.getIdReport();

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
                        ip_rover, 
                        this.porta, 
                        "NaveMae", 
                        this.ipNaveMae, 
                        this.portaNaveMae, 
                        payloadREPORT,
                        idReport,
                        -1, // No REPORT nao interessa o numFrames
                        i
            );

            envioML.sendMensagem(mREPORT.toByteArray(), 
                                this.ipNaveMae, 
                                this.portaNaveMae, 
                                null
            );
            System.out.println("[" + idRover + " - ML] A enviar frame " + i + " do report " + idReport);
        }

        Mensagem mEND = new Report(TipoMensagem.ML_END, 
                            this.idRover, 
                            ip_rover, 
                            this.porta, 
                            "NaveMae", 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            null,
                            idReport,
                            -1, // No END nao interessa o numFrames
                            -1  // No END nao interessa o numSeq
        );

        // Envia END e espera um MISS ou FIN (sinaliza fim das frames)
        envioML.sendMensagem(mEND.toByteArray(), 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            idReport + "_END"
        ); 

        System.out.println("[" + idRover + " - ML]: Enviou END para NaveMae");
    }

    public void handleMISS(Report report, InetAddress ip_rover, int numFrames, int tamMax, byte[]imgBytes) throws Exception {
        String idReport = report.getIdReport();

        // Confirmar o END (parar retransmissão)
        envioML.confirmarRececao(idReport + "_END");
        System.out.println("[" + idRover + " - ML] MISS de: NaveMae");
        byte[] resposta = report.getPayload();

        // Reenvia todas as frames da imagem (REPORT) e nao espera por nada
        for(int i = 0; i < resposta.length; i++){
            if(resposta[i] == 0){
                int inicio = i * tamMax;
                int fim = Math.min(inicio + tamMax, imgBytes.length);
                byte[] payload = Arrays.copyOfRange(imgBytes, inicio, fim);

                Mensagem mREPORT = new Report(TipoMensagem.ML_REPORT, 
                            this.idRover, 
                            ip_rover, 
                            this.porta, 
                            "NaveMae", 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            payload,
                            idReport,
                            -1, // No REPORT nao interessa o numFrames
                            i
                );

                envioML.sendMensagem(mREPORT.toByteArray(), 
                                    this.ipNaveMae, 
                                    this.portaNaveMae, 
                                    null
                );
                System.out.println("[" + idRover + " - ML] A reenviar frame " + i + " do report " + idReport);
            }
        }

        Mensagem mEND = new Report(TipoMensagem.ML_END, 
                            this.idRover, 
                            ip_rover, 
                            this.porta, 
                            "NaveMae", 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            null,
                            idReport,
                            -1, // No END nao interessa o numFrames
                            -1  // No END nao interessa o numSeq
        );

        // Envia END e espera um MISS ou FIN (sinaliza fim das frames)
        envioML.sendMensagem(mEND.toByteArray(), 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            idReport + "_END"
        );

        System.out.println("[" + idRover + " - ML]: Reenviou END para NaveMae");
    }

    public void handleFIN(Report report, InetAddress ip_rover) throws Exception {
        String idReport = report.getIdReport();

        // Confirmar o END (parar retransmissão)
        envioML.confirmarRececao(idReport + "_END");
        System.out.println("[" + idRover + " - ML] FIN de: NaveMae");

        Mensagem mFINACK = new Report(TipoMensagem.ML_FINACK, 
                            this.idRover, 
                            ip_rover, 
                            this.porta, 
                            "NaveMae", 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            null,
                            idReport,
                            -1, // No FINACK nao interessa o numFrames
                            -1  // No FINACK nao interessa o numSeq
        );

        // Envia FINACK e nao espera por nada
        envioML.sendMensagem(mFINACK.toByteArray(), 
                            this.ipNaveMae, 
                            this.portaNaveMae, 
                            idReport + "_FINACK"
        );

        System.out.println("[" + idRover + " - ML]: Enviou FINACK para NaveMae");
}

    public void handleSTOP_CON(Report report, boolean recebido) {
        String idReport = report.getIdReport();

        // Confirmar o FINACK (parar retransmissão)
        envioML.confirmarRececao(idReport + "_FINACK");
        System.out.println("[" + idRover + " - ML] STOP_CON de: NaveMae (" + idReport + ")");
        System.out.println("[" + idRover + " - ML] A fechar WHILE para o report " + idReport);
    }


    public void stopMLRover() {
        running = false;
        socket.close();
        envioML.stop();
    }
}