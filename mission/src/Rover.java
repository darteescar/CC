import data.*;
import java.net.InetAddress;
import protocols.MissionLink;
import protocols.TelemetryStream;

public class Rover {

    // Identificação do Rover
    private final String id; // R-x
    private final InetAddress ip;
    private final int porta;
    private Estado estado;
    private Missao missao_atual;

    // Identificação da Nave Mãe
    private final String id_NaveMae;
    private final InetAddress ip_NaveMae;
    private final int porta_NaveMae;

    // Protocolos
    private MissionLink ml;
    private TelemetryStream ts;

    /* ========== Construtor ========== */

    public Rover(String id, InetAddress ip, int porta,
                 String id_NaveMae, InetAddress ip_NaveMae, int porta_NaveMae) {
        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.estado = new Estado();
        this.missao_atual = new Missao();

        this.id_NaveMae = id_NaveMae;
        this.ip_NaveMae = ip_NaveMae;
        this.porta_NaveMae = porta_NaveMae;;

        try {
            // Rover escuta missões vindas da nave-mãe via UDP
            this.ml = new MissionLink(porta);

            // Rover conecta-se à nave-mãe para enviar telemetria via TCP
            this.ts = new TelemetryStream(ip_NaveMae, porta_NaveMae);

            System.out.println("[Rover] " + id + " inicializado.");
            System.out.println(" - Escutando missões UDP na porta " + porta);
            System.out.println(" - Telemetria TCP conectada à nave-mãe em " + ip_NaveMae + ":" + porta_NaveMae + "\n");

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar Rover: " + e.getMessage() + "\n");
        }
    }

    public Estado getEstado(){
        return this.estado;
    }

    public InetAddress getIP(){
        return this.ip;
    }

    public int getPorta(){
        return this.porta;
    }

    public String getId(){
        return this.id;
    }

    /* ========== Método para inciar serviços do rover ========== */

    public void startComms(){
        new Thread(() -> startTS() ).start();
        new Thread(() -> startML()).start();
    }

    public void startTS(){
        while (true) { 
            EstadoOperacional estado_op = this.getEstado().getEstadoOperacional();

            if (estado_op == EstadoOperacional.EM_MISSAO ) {
                byte[] payload = this.estado.toByteArray();
                sendMessageNaveMaeTS(TipoMensagem.TS_REPORT, payload);
            }

            try {
                int freq_update = this.missao_atual.getFreqUpdate();
                
                Thread.sleep(freq_update); // espera 1 segundo (por padrão)
            } catch (InterruptedException e) {
                e.printStackTrace();                
            }
        }
    }

    public void startML(){
        while (true) { 
            EstadoOperacional estado = this.getEstado().getEstadoOperacional();

            switch (estado) {
                case EM_MISSAO:
                    move();
                    break;
                case INOPERACIONAL:
                    // envia falha
                    break;
                case PARADO:
                    boolean handshakeSuccess = startThreewayHandshake();

                    if (handshakeSuccess) {
                        requestMission();
                        receiveMission(this.missao_atual);
                        System.out.println();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    /* ========== Métodos relacionados ao MissionLink ========== */

    public boolean startThreewayHandshake(){
        sendMessageNaveMaeML(TipoMensagem.ML_SYN, new byte[0]);

        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();
            if (msg_recebida.getTipo() != TipoMensagem.ML_SYNACK) {
                System.out.println("[ERRO] Three-way handshake falhou: tipo de mensagem inesperado.");
                return false;
            }
            System.out.println("[Rover] SYN ACK recebida da nave-mãe via MissionLink.");

            sendMessageNaveMaeML(TipoMensagem.ML_ACK, new byte[0]);
            System.out.println("[Rover] Three-way handshake via MissionLink concluído com sucesso.");

        } catch (Exception e) {
            System.out.println("[ERRO]" + e.getMessage());
        }

        return true;
    }

    void requestMission(){
        sendMessageNaveMaeML(TipoMensagem.ML_REQUEST, new byte[0]);
    }

    void receiveMission(Missao missao){
        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();
            byte[] payload = msg_recebida.getPayload();
            missao.fromByteArray(payload);
            System.out.println("[Rover] Missão recebida via MissionLink.");

            sendMessageNaveMaeML(TipoMensagem.ML_CONFIRM, new byte[0]);

            this.estado.setEstadoOperacional(EstadoOperacional.EM_MISSAO); 

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao receber mensagem via MissionLink: " + e.getMessage());
        }
    }

    public void sendMessageNaveMaeML(TipoMensagem tipo, byte[] data){
        try {
            Mensagem payload = new Mensagem(tipo,
                                    this.id, 
                                    this.ip, 
                                    this.porta, 
                                    this.id_NaveMae, 
                                    this.ip_NaveMae, 
                                    this.porta_NaveMae, 
                                    data);
            this.ml.sendMensagem(payload.toByteArray(), this.ip_NaveMae, this.porta_NaveMae);

            if (tipo == TipoMensagem.ML_SYN) {
                System.out.println("[Rover] SYN enviada à nave-mãe via MissionLink.");
            } else if (tipo == TipoMensagem.ML_ACK) {
                System.out.println("[Rover] ACK enviada à nave-mãe via MissionLink.");
            } else if (tipo == TipoMensagem.ML_CONFIRM) {
                System.out.println("[Rover] Confirmação de receção de missão enviada à nave-mãe via MissionLink.");
            } else if (tipo == TipoMensagem.ML_REQUEST) {
                System.out.println("[Rover] Pedido de missão enviado à nave-mãe via MissionLink.");
            }

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao enviar mensagem via MissionLink: " + e.getMessage());
        }
    }

    public void move(){
        
        EstadoOperacional estado_op = this.getEstado().getEstadoOperacional();
            
        if (estado_op == EstadoOperacional.EM_MISSAO) {
            // Executa a missão atual: espera pela duração e atualiza o estado para PARADO.
            Missao m = this.missao_atual;
            if (m == null) return;

            long durationMs = (long) m.getDuracao() * 60 * 1000L; // interpreta duracao como minutos
            long endTime = System.currentTimeMillis() + Math.max(0, durationMs);

            System.out.println("\n[Rover] Iniciando missão id=" + m.getId() + ", duração=" + m.getDuracao() + " (minutos)");

            while (System.currentTimeMillis() < endTime) {
                //Relaizar missao
            }

            // Missão concluída
            this.estado.setEstadoOperacional(EstadoOperacional.PARADO);
            System.out.println("[Rover] Missão concluída. Estado atualizado para PARADO.\n");
        }
    }

    /* ========== Métodos relacionados ao TelemetryStream ========== */

    public void sendMessageNaveMaeTS(TipoMensagem tipo, byte[] data){
        try {
            Mensagem payload = new Mensagem(tipo,
                                    this.id, 
                                    this.ip, 
                                    this.porta, 
                                    this.id_NaveMae, 
                                    this.ip_NaveMae, 
                                    this.porta_NaveMae, 
                                    data);
            this.ts.enviarMensagem(payload);

            if (tipo == TipoMensagem.TS_REPORT) {
                System.out.println("[Rover] Relatório de telemetria enviado à nave-mãe via TelemetryStream.");
            }

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao enviar relatório via TelemetryStream: " + e.getMessage());
        }
    }
}