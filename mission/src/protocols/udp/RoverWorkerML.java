package protocols.udp;

import core.NaveMae;
import data.Mensagem;
import data.Missao;
import data.Report;
import data.TipoMensagem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RoverWorkerML implements Runnable{
    private final String idRover;
    private final InetAddress ipRover;
    private final int portaRover;
    private final EnvioML envioML;
    private final NaveMae nm;
    private final BlockingQueue<Mensagem> queue = new LinkedBlockingQueue<>();
    private final Map<String, ColetorReport> coletores = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    /* ====== Construtor ====== */

    public RoverWorkerML(String id, InetAddress ip, int porta, EnvioML envioML, NaveMae nm){
        this.idRover = id;
        this.ipRover = ip;
        this.portaRover = porta;
        this.envioML = envioML;
        this.nm = nm;
    }

    /* ====== Métodos ====== */

    // Logica principal da Thread de cada RoverWorer
    @Override
    public void run(){
        System.out.println("[WorkerML - " + idRover + "] Iniciado");
        try{
            while(running){
                Mensagem m = queue.take();
                this.handleMensagem(m);
            }
        }catch(Exception e){
            System.out.println("[WorkerML - " + idRover + " - ERRO] while da Thread " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Handler das mensagens
    public void handleMensagem(Mensagem m){
        TipoMensagem tipo = m.getTipo();
        System.out.println("[WorkerML - " + idRover + "] Recebida mensagem do tipo: " + tipo);

        try{
            switch(tipo) {
                case ML_SYN -> handleSYN();
                case ML_REQUEST -> handleREQUEST();
                case ML_CONFIRM -> handleCONFIRM();
                case ML_FRAMES -> handleFRAMES(m);
                case ML_REPORT -> handleREPORT(m);
                case ML_END -> handleEND(m);
                case ML_FINACK -> handleFINACK(m);
                default -> System.out.println("[ERRO] Tipo não existente: " + tipo);
            }
        }catch(Exception e){
            System.out.println("[WorkerML - " + idRover + " - ERRO] Handler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleSYN() throws Exception{
        Missao atual = this.nm.getMissaoRover(idRover);
        this.nm.addMissaoConcluida(atual);
        this.nm.removeMissaoMap(idRover);

        Mensagem mSYNACK = new Mensagem(TipoMensagem.ML_SYNACK, 
                                        "NaveMae", 
                                        nm.getIP(), 
                                        nm.getPortaUDP(),
                                        idRover, 
                                        ipRover, 
                                        portaRover, 
                                        null
        );

        // Enviar SYNACK e esperar REQUEST
        envioML.sendMensagem(mSYNACK.toByteArray(), 
                            ipRover, 
                            portaRover,
                            idRover + "_SYNACK"
        );
        System.out.println("[WorkerML - " + idRover + "] SYNACK enviado ao rover: " + idRover);
    }

    public void handleREQUEST() throws Exception{
        // Confirma o SYNACK (parar retransmissão)
        envioML.confirmarRececao(idRover + "_SYNACK");

        // Enviar missão
        Missao missao = nm.getMissaoQueue();
        this.nm.putMissaoMap(idRover, missao);

        Mensagem mDATA = new Mensagem(TipoMensagem.ML_DATA, 
                                "NaveMae", 
                                nm.getIP(), 
                                nm.getPortaUDP(),
                                idRover, 
                                ipRover, 
                                portaRover, 
                                missao.toByteArray()
        );

        // Enviar DATA e esperar CONFIRM
        envioML.sendMensagem(mDATA.toByteArray(), 
                            ipRover, 
                            portaRover,
                            idRover + "_DATA"
        );

        System.out.println("[WorkerML - " + idRover + "] Missao enviada ao rover: " + idRover);
    }

    public void handleCONFIRM(){
        // Confirma o DATA(parar retransmissão)
        envioML.confirmarRececao(idRover + "_DATA");
        System.out.println("[WorkerML - " + idRover + "] CONFIRM de: " + idRover);
    }

    public void handleFRAMES(Mensagem m) throws Exception {
        byte[] payload = m.getPayload();

        // Deserializar o idReport e numFrames
        ByteBuffer bb = ByteBuffer.wrap(payload);

        int numFrames = bb.getInt();
        int size = bb.getInt();

        byte[] texto = new byte[size];
        bb.get(texto);
        String idReport = new String(texto);

        //Criar um coletor de reports
        ColetorReport col = new ColetorReport(idReport, numFrames);
        this.coletores.put(idReport, col);

        Mensagem mOK = new Mensagem(TipoMensagem.ML_OK,
                                    "NaveMae", 
                                    nm.getIP(), 
                                    nm.getPortaUDP(),
                                    idRover, 
                                    ipRover,
                                    portaRover,
                                    null
        );

        // Envia OK e espera REPORT ou END
        envioML.sendMensagem(mOK.toByteArray(), 
                            ipRover, 
                            portaRover, 
                            idReport + "_OK"
        );

        System.out.println("[WorkerML - " + idRover + "] OK enviado para report " + idReport);

    }

    public void handleREPORT(Mensagem m) {
        Report r  = (Report)m;
        String idReport = r.getIdReport();
        int numSeq = r.getNumSeq();
        byte[] frame = r.getPayload();

        // Confirmar o OK (parar retransmissão)
        envioML.confirmarRececao(idReport + "_OK");
        // Confirmar o MISS (parar retransmissão)
        envioML.confirmarRececao(idReport + "_MISS");

        // Adiciona a frame que recebeu ao coletor da report correspondente
        ColetorReport col = coletores.get(idReport);
        col.addFrame(numSeq, frame);
    }

    public void handleEND(Mensagem m) throws Exception {
        byte[] payload = m.getPayload();

        // Deserializar o idReport
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int size = bb.getInt();
        byte[] texto = new byte[size];
        bb.get(texto);
        String idReport = new String(texto);

        // Confirmar o OK (parar retransmissão)
        envioML.confirmarRececao(idReport + "_OK");

        ColetorReport col = coletores.get(idReport);


        if (!col.estaCompleto()) {
            byte[] resposta = col.arrayResposta();
            Mensagem mMISS = new Mensagem(TipoMensagem.ML_MISS,
                                        "NaveMae", 
                                        nm.getIP(), 
                                        nm.getPortaUDP(),
                                        idRover, 
                                        ipRover,
                                        portaRover,
                                        resposta
            );

            // Envia MISS e espera por REPORT
            envioML.sendMensagem(mMISS.toByteArray(), 
                                ipRover, 
                                portaRover, 
                                idReport + "_MISS"
            );

            System.out.println("[WorkerML - " + idRover + "] MISS enviado para report " + idReport);

        } else {
            Mensagem mFIN = new Mensagem(TipoMensagem.ML_FIN,
                                        "NaveMae", 
                                        nm.getIP(), 
                                        nm.getPortaUDP(),
                                        idRover, 
                                        ipRover, 
                                        portaRover,
                                        null
            );
            
            // Envia FIN e espera FINACK
            envioML.sendMensagem(mFIN.toByteArray(), 
                                ipRover, 
                                portaRover,
                                idReport+ "_FIN"
            );

            System.out.println("[WorkerML - " + idRover + "] FIN enviado para report " + idReport);

            try {
                byte[] imgBytes = col.reconstruirIMGBytes();
                File out = new File("img/report_" + idRover + "_" + idReport + ".jpg");
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(imgBytes);
                }
                System.out.println("[WorkerML - " + idRover + "] Report " + idReport + " reconstruído em " + out.getAbsolutePath());

                coletores.remove(idReport);
            } catch (IOException e) {
                System.err.println("[WorkerML - " + idRover + "] Falha ao montar imagem: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handleFINACK(Mensagem m){
        byte[] payload = m.getPayload();

        // Deserializar o idReport
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int size = bb.getInt();
        byte[] texto = new byte[size];
        bb.get(texto);
        String idReport = new String(texto);

        // Confirma o FIN (parar retransmissão)
        envioML.confirmarRececao(idReport + "_FIN");
        System.out.println("[WorkerML - " + idRover + "] FINACK de: " + idRover);
    }


    public void addMensagemQueue(Mensagem m){
        queue.offer(m);
    }

    public void paraCiclo(){
        this.running = false;
    }
}
