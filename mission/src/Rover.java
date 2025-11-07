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
            System.out.println(" - Telemetria TCP conectada à nave-mãe em " + ip_NaveMae + ":" + porta_NaveMae);

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar Rover: " + e.getMessage());
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

    public void startComms(){
        new Thread(() -> startTS() ).start();
        new Thread(() -> startML()).start();
    }

    public void startTS(){
        while (true) { 
            EstadoOperacional estado_op = this.getEstado().getEstadoOperacional();

            if (estado_op == EstadoOperacional.EM_MISSAO ) {
                byte[] payload = this.estado.toByteArray();
                Mensagem msg = new Mensagem(TipoMensagem.TS_REPORT,
                                                            this.id,
                                                            this.ip,
                                                            this.porta,
                                                            this.id_NaveMae,
                                                            this.ip_NaveMae,
                                                            this.porta_NaveMae,
                                                            payload);      
                this.ts.enviarMensagem(msg);
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
                case EstadoOperacional.EM_MISSAO:
                    // NADA, ESTÁ A ENVIAR TELEMETRIA POR TCP
                    break;
                case EstadoOperacional.INOPERACIONAL:
                    // envia falha
                    break;
                case EstadoOperacional.PARADO:
                    boolean handshakeSuccess = startThreewayHandshake();

                    if (handshakeSuccess) {
                        requestMission();
                        receiveMission(this.missao_atual);
                    }

                    break;
                default:
                    break;
            }
        }
    }

    public boolean startThreewayHandshake(){
        try {
            Mensagem payload = new Mensagem(TipoMensagem.ML_SYN,
                                                    this.id,
                                                    this.ip,
                                                    this.porta,
                                                    this.id_NaveMae,
                                                    this.ip_NaveMae,
                                                    this.porta_NaveMae,
                                                    new byte[0]);
            byte[] msg = payload.toByteArray();
            
            this.ml.sendMensagem(msg, this.ip_NaveMae, this.porta_NaveMae);
            System.out.println("[Rover] Started three-way handshake via MissionLink.");
        } catch (Exception e) {
            System.out.println("[ERRO]  Three-way handshake falhou: " + e.getMessage());
        }

        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();

            if (msg_recebida.getTipo() != TipoMensagem.ML_SYNACK) {
                System.out.println("[ERRO] Three-way handshake falhou: tipo de mensagem inesperado.");
                return false;
            }

            System.out.println("[Rover] Three-way handshake via MissionLink concluído com sucesso.");

        } catch (Exception e) {
            System.out.println("[ERRO]" + e.getMessage());
        }

        return true;
    }

    void requestMission(){
        try {
            Mensagem payload = new Mensagem(TipoMensagem.ML_REQUEST,
                                                    this.id,
                                                    this.ip,
                                                    this.porta,
                                                    this.id_NaveMae,
                                                    this.ip_NaveMae,
                                                    this.porta_NaveMae,
                                                    new byte[0]);
            byte[] msg = payload.toByteArray();
            
            this.ml.sendMensagem(msg, this.ip_NaveMae, this.porta_NaveMae);
            System.out.println("[Rover] Solicitação de missão enviada via MissionLink.");
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao solicitar missão via MissionLink: " + e.getMessage());
        }
    }

    void receiveMission(Missao missao){
        try {
            Mensagem msg_recebida = this.ml.receiveMensagem();
            byte[] payload = msg_recebida.getPayload();
            missao.fromByteArray(payload);
            String s = missao.toString();
            System.out.println("[Rover] Missão recebida via MissionLink: " + s);

            this.estado.setEstadoOperacional(EstadoOperacional.EM_MISSAO);
            sendConfirmation();

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao receber mensagem via MissionLink: " + e.getMessage());
        }
    }

    void sendConfirmation(){
        try {
            Mensagem payload = new Mensagem(TipoMensagem.ML_CONFIRM,
                                                    this.id,
                                                    this.ip,
                                                    this.porta,
                                                    this.id_NaveMae,
                                                    this.ip_NaveMae,
                                                    this.porta_NaveMae,
                                                    new byte[0]);
            byte[] msg = payload.toByteArray();
            
            this.ml.sendMensagem(msg, this.ip_NaveMae, this.porta_NaveMae);
            System.out.println("[Rover] Confirmação da recepção da missão enviada via MissionLink.");
        } catch (Exception e) {
            System.out.println("[ERRO] Falha na recepção da missão via MissionLink: " + e.getMessage());
        }
    }

    void move(){
        /* 
        while (true) {
            EstadoOperacional estado = this.getEstado().getEstadoOperacional();

            if (estado == EstadoOperacional.EM_MISSAO) {
                if (nao esta na area da missao) {
                    // mover-se para a area da missao
                } else {
                    // executar a missao durante o tempo estipulado
                    // diminui bateria de acordo com a velocidade ?
                    // se ficar sem bateria, mudar estado para INOPERACIONAL e break
                }
                // atualizar estado para PARADO apos completar a missao
            }  
        }
        */
    }
}