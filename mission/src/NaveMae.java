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

    //private final String id = "0000";
    private InetAddress endIP;
    private int porta;

    private Queue<Missao> queue;

    private MissionLink ml;
    private TelemetryStream ts;

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
        //Acabar
    }

    public void startNaveMae(){
        new Thread(() -> startServerTS() ).start();
        new Thread(() -> startMissionManager() ).start();
        //new Thread(() -> startAPI() ).start();
    }

    public void startAPI(){
        //Acabar
    }

    public void startServerTS(){
        this.ts.startServerNaveMae();
        //Acabar
    }

    public void startMissionManager(){

        while(true){
            Mensagem msg = this.receiveMessageML();// Fazer de forma concorrente
            new Thread(() -> processRequestML(msg) ).start();
        }
        
    }

    public void processRequestML(Mensagem msg){
        if (msg == null || msg.getTipo() != TipoMensagem.ML_SYN) {
            System.out.println("[NaveMae] Mensagem inválida recebida via MissionLink.");
            return;
        } else if (msg.getTipo() == TipoMensagem.ML_SYN && msg != null) {
            System.out.println("[NaveMae] SYN recebido via MissionLink.");
            sendSynAck(msg.getIdOrg(), msg.getIpOrg(), msg.getPortaOrg());

            msg = this.receiveMessageML();//Request da Missao
        }

        if(msg != null && msg.getTipo() == TipoMensagem.ML_REQUEST){
            String id_rover = msg.getIdOrg();
            InetAddress ip_rover = msg.getIpOrg();
            int porta_rover = msg.getPortaOrg();

            if (numeroMissaoNaQueue() == 0) {
                this.reporMissoesQueue();
            }

            Missao m = this.queue.remove();

            this.sendMissionToRover(m, id_rover, ip_rover, porta_rover);
        }

        //Receber confirmação?

    }

    public Mensagem receiveMessageML(){
        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();
            System.out.println("[NaveMae] Request de missão recebido via MissionLink.");
            return msg_recebida;

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao receber request via MissionLink: " + e.getMessage());
            return null;
        }
        
    }

    public void sendMissionToRover(Missao m, String id_rover, InetAddress ip_rover, int porta_rover){
        try {
            byte[] payload = m.toByteArray();
            Mensagem msg_missao = new Mensagem(
                TipoMensagem.ML_DATA,
                "NaveMae",
                this.endIP,
                this.porta,
                id_rover,
                ip_rover,
                porta_rover,
                payload
            );
            this.ml.sendMensagem(msg_missao.toByteArray(), ip_rover, porta_rover);
            System.out.println("[NaveMae] Missão enviada ao rover " + id_rover + " via MissionLink.");

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao enviar missão via MissionLink: " + e.getMessage());
        }
        
    }

    public void sendSynAck(String id_rover, InetAddress ip_rover, int porta_rover){
        try {
            Mensagem payload = new Mensagem(TipoMensagem.ML_SYNACK,
                                                    "NaveMae",
                                                    this.endIP,
                                                    this.porta,
                                                    id_rover,
                                                    ip_rover,
                                                    porta_rover,
                                                    new byte[0]);
            this.ml.sendMensagem(payload.toByteArray(), ip_rover, porta_rover);
            System.out.println("[NaveMae] SYN ACK enviada ao rover " + id_rover + " via MissionLink.");

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao enviar confirmação SYN via MissionLink: " + e.getMessage());
        }
    }

}
