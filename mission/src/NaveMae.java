import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import data.*;
import protocols.MissionLink;
import protocols.TelemetryStream;

import java.net.InetAddress;
//import java.net.UnknownHostException;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;

    private final String id = "NaveMae";
    private InetAddress endIP;
    private int porta;

    private Queue<Missao> queue;

    private MissionLink ml;
    private TelemetryStream ts;

    /* ========== Construtor ========== */

    public NaveMae(InetAddress ip, int porta, Map<String, Estado> m1, Map<String, InetAddress> m2, Map<String, Integer> m3 ){
        this.setRoversEstado(m1);
        this.setRoversIP(m2);
        this.setRoversPorta(m3);
        this.endIP = ip;
        this.porta = porta;

        this.queue = new PriorityQueue<Missao>();

        try {
            //
            this.ml = new MissionLink(this.porta);

            // Nave Mãe escuta telemetria via TCP
            this.ts = new TelemetryStream(this.porta);

            System.out.println("[Nave Mãe] inicializada.");
            System.out.println(" - Escutando requests UDP na porta " + porta);
            System.out.println(" - Escutando telemetria TCP na porta " + porta);

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar Nave Mãe: " + e.getMessage());
        }

        
    }


    public Map<String, Estado> getRoversEstado() {
        Map<String, Estado> novo = new HashMap<>();
        for(Map.Entry<String,Estado> m: this.roversEstado.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversEstado(Map<String, Estado> roversEstado2) {
        Map<String, Estado> novo = new HashMap<>();
        for(Map.Entry<String,Estado> m: roversEstado2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversEstado = novo;
    }

    public Map<String, InetAddress> getRoversIP() {
        Map<String, InetAddress> novo = new HashMap<>();
        for(Map.Entry<String,InetAddress> m: this.roversIP.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversIP(Map<String, InetAddress> roversIP2) {
        Map<String, InetAddress> novo = new HashMap<>();
        for(Map.Entry<String,InetAddress> m: roversIP2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversIP = novo;
    }

    public Map<String, Integer> getRoversPorta() {
        Map<String, Integer> novo = new HashMap<>();
        for(Map.Entry<String,Integer> m: this.roversPorta.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        return novo;
    }

    public void setRoversPorta(Map<String, Integer> roversPorta2) {
        Map<String, Integer> novo = new HashMap<>();
        for(Map.Entry<String,Integer> m: roversPorta2.entrySet()){
            novo.put(m.getKey(),m.getValue());
        }
        this.roversPorta = novo;
    }

    public InetAddress getEndIP() {
        return this.endIP;
    }

    public int getPorta() {
        return this.porta;
    }

    /* ========== MÉTODOS PARA MANIPULAÇÃO DA QUEUE DE MISSÕES ========== */

    public void adicionarMissaoQueue(Missao m){
        this.queue.add(m);
    }

    public Missao retirarMissaoQueue(){
        return this.queue.remove();
    }

    public int numeroMissaoNaQueue(){
        return this.queue.size();
    }

    public Missao verProximaMissao(){
        return this.queue.element();
    }

    public void atualizaEstadoRover(String id, Estado e){
        this.roversEstado.put(id, e);
    }

    public void reporMissoesQueue(){
        adicionarMissaoQueue(new Missao(2367, 1, 2, 3, 4, "Anda por aí", 1, 3000));
        adicionarMissaoQueue(new Missao(2368, 1, 2, 3, 4, "Anda por aí", 1, 3000));
        adicionarMissaoQueue(new Missao(2369, 1, 2, 3, 4, "Anda por aí", 1, 3000));
        //Acabar
    }

    /* ========== Método para iniciar a Nave Mãe ==========*/

    public void startNaveMae(){
        new Thread(() -> startServerTS()).start();
        new Thread(() -> startMissionManager() ).start();
        //new Thread(() -> startAPI() ).start();
    }

    /* ========== Métodos para iniciar os diferentes serviços da Nave Mãe ========== */

    public void startAPI(){
        //Acabar
    }

    public void startServerTS(){
        this.ts.startServerNaveMae();
        /*
        while(true){
            Mensagem msg = this.receiveMessageTS();
            new Thread(() -> {
                if (msg != null && msg.getTipo() == TipoMensagem.TS_REPORT){
                    String id_rover = msg.getIdOrg();
                    byte[] payload = msg.getPayload();
                    Estado novo = new Estado();
                    novo.fromByteArray(payload); 
                    this.atualizaEstadoRover(id_rover, novo);
                    System.out.println("[NaveMae] Telemetria recebida do rover " + id_rover + " via TelemetryStream.");
                } else {
                    System.out.println("[NaveMae] Mensagem de tipo desconhecido recebida via TelemetryStream.");
                }
            }).start();
        }
        */
        
    }

    public void startMissionManager(){

        while(true){
            Mensagem msg = this.receiveMessageML();
            new Thread(() -> {
                if (msg.getTipo() == TipoMensagem.ML_SYN && msg != null){
                    sendMessageML(TipoMensagem.ML_SYNACK, new byte[0], msg.getIdOrg(), msg.getIpOrg(), msg.getPortaOrg());
                } else if (msg.getTipo() == TipoMensagem.ML_REQUEST && msg != null){
                    processRequestML(msg);
                } else if (msg.getTipo() == TipoMensagem.ML_CONFIRM && msg != null){
                    System.out.println("[NaveMae] Confirmação de receção de missão recebida do rover " + msg.getIdOrg() + " via MissionLink.");
                } else {
                    System.out.println("[NaveMae] Mensagem de tipo desconhecido recebida via MissionLink.");
                }
            }).start();       
        }
        
    }

    /* ========== Métodos relacionados ao MissionLink ========== */

    public void processRequestML(Mensagem msg){
            if (numeroMissaoNaQueue() == 0) {
                this.reporMissoesQueue();
            }

            Missao m = this.queue.remove();

            sendMessageML(TipoMensagem.ML_DATA,
                                m.toByteArray(), 
                                msg.getIdOrg(), 
                                msg.getIpOrg(), 
                                msg.getPortaOrg());
    }

    public Mensagem receiveMessageML(){
        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();
            return msg_recebida;

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao receber request via MissionLink: " + e.getMessage());
            return null;
        }
        
    }

    public void sendMessageML(TipoMensagem tipo, byte[] data, String id_rover, InetAddress ip_rover, int porta_rover){
        try {
            Mensagem payload = new Mensagem(tipo,
                                    this.id, 
                                    this.endIP, 
                                    this.porta, 
                                    id_rover, 
                                    ip_rover, 
                                    porta_rover, 
                                    data);
            this.ml.sendMensagem(payload.toByteArray(), ip_rover, porta_rover);

            if (tipo == TipoMensagem.ML_SYNACK) {
                System.out.println("[NaveMae] SYN ACK enviada ao rover " + id_rover + " via MissionLink.");
            } else if (tipo == TipoMensagem.ML_DATA) {
                System.out.println("[NaveMae] Missao enviada ao rover " + id_rover + " via MissionLink.");
            }
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao enviar mensagem via MissionLink: " + e.getMessage());
        }
    }

    /* ========== Métodos relacionados ao TelemetryStream ========== */

    /*
    public Mensagem receiveMessageTS(){
        return this.ts.obterMensagemServidor();
    }
        */
    

}
