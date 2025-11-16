package core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import data.Estado;
import data.EstadoOperacional;
import data.Missao;
import protocols.udp.MissionLinkRover;
import protocols.tcp.TelemetryStreamRover;

public class Rover {
    // Identificação do Rover
    private final String id; // R-x
    private final InetAddress ip;
    private final int porta;
    private Estado estado;
    private Missao missaoAtual;

    // Identificação da Nave Mãe
    private static final String id_NaveMae = "NaveMae";
    private final InetAddress ip_NaveMae;
    private static final int portaUDPNaveMae = 5000;
    private static final int portaTCPNaveMae = 6000;

    // Protocolos
    private MissionLinkRover ml;
    private TelemetryStreamRover ts;

    /* ========== Construtor ========== */

    public Rover(String id, 
                InetAddress ip, 
                int porta,
                InetAddress ip_NaveMae){

        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.estado = new Estado();
        this.missaoAtual = null;            

        this.ip_NaveMae = ip_NaveMae;

        try {
            this.ml = new MissionLinkRover(id, porta, ip_NaveMae, portaUDPNaveMae);
            this.ts = new TelemetryStreamRover(this, ip_NaveMae, portaTCPNaveMae); 

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ====== Getters e Setters ====== */

    public String getId(){
        return this.id;
    }

    public InetAddress getIP(){
        return this.ip;
    }

    public int getPorta(){
        return this.porta;
    }

    public Estado getEstado(){
        return this.estado;
    }

    public Estado tiraFotoEstado(){
        return this.estado.clone();        
    }

    public Missao getMissaoAtual(){
        return this.missaoAtual;
    }

    public String getIdNaveMae(){
        return id_NaveMae;
    }

    public InetAddress getIpNaveMae(){
        return this.ip_NaveMae;
    }

    public int getPortaUDPNaveMae(){
        return portaUDPNaveMae;
    }

    public int getPortaTCPNaveMae(){
        return portaTCPNaveMae;
    }

    /* ====== Métodos ====== */

    public void startRover(){
        this.ml.startMLRover(this);
        this.ts.startTSRover();
    }
    
    /* ====== Executar a Missao ====== */

    public void executaMissao(Missao missao){
        this.missaoAtual = missao;
        int idMissao = missao.getId();

        System.out.println("[" + this.id + "] A executar missao: " + idMissao);

        this.estado.setEstadoOperacional(EstadoOperacional.A_CAMINHO);
        handleACaminho(missao);

        if (this.estado.getBateria() == 0) {
            this.estado.setVelocidade(0);
            this.estado.setEstadoOperacional(EstadoOperacional.INOPERACIONAL);
            return;
        }

        this.estado.setEstadoOperacional(EstadoOperacional.EM_MISSAO);
        handleMisao(missao);

        if (this.estado.getBateria() == 0) {
            this.estado.setVelocidade(0);
            this.estado.setEstadoOperacional(EstadoOperacional.INOPERACIONAL);
            return;
        }

        this.estado.setVelocidade(0);
        this.estado.setEstadoOperacional(EstadoOperacional.PARADO);
        this.missaoAtual = null;

        System.out.println("[" + this.id + "] Fim da missao: " + idMissao);
    }

    public void handleACaminho(Missao missao){
        // Variaveis da Missao
        double x1 = missao.getX1();
        double y1 = missao.getY1();
        double x2 = missao.getX2();
        double y2 = missao.getY2();

        // Variaveis do Estado do Rover
        double posX = this.estado.getX();
        double posY = this.estado.getY();
        int bateria = this.estado.getBateria();
        float velocidade = this.estado.getVelocidade();

        // Constantes
        final float VEL_MAX = 10.0f;    // Km/h
        final float ACELERACAO = 0.5f;  // Velocidade incrementada por ciclo
        final int STEP_MS = 200;
        final double RAIO_MIN = 0.3;    // distancia minima do centro para acabar

        int ciclosBateria = 0;

        // ---------------
        double centroZonaX = x1 + ((x2-x1)/2); 
        double centroZonaY = y1 + ((y2-y1)/2);

        while(true){

            if(bateria == 0){
                this.estado.setEstadoOperacional(EstadoOperacional.INOPERACIONAL);
                return;
            }

            double distancia = Math.sqrt(Math.pow(centroZonaX - posX, 2) + Math.pow(centroZonaY - posY, 2));
            if(distancia < RAIO_MIN) break;
            
            if(velocidade < VEL_MAX){
                velocidade += ACELERACAO;
                this.estado.setVelocidade(velocidade);
            }

            double vel_ms = (velocidade * 1000.0) / 3600.0;

            double dirX = (centroZonaX - posX) / distancia;
            double dirY = (centroZonaY - posY) / distancia;

            double dt = STEP_MS / 1000.0;
            posX += dirX *vel_ms * dt;
            posY += dirY *vel_ms * dt;

            this.estado.setX(posX);
            this.estado.setY(posY);

            ciclosBateria++;
            if(ciclosBateria >= 300){
                bateria = Math.max(0, bateria - 1);
                this.estado.setBateria(bateria);
                ciclosBateria = 0;
            }

            try{Thread.sleep(STEP_MS);}
            catch(Exception e){
                System.out.println("[" + this.id + " ERRO] handleACaminho: " + e.getMessage());
                e.printStackTrace();
            }
        }

        this.estado.setX(centroZonaX);
        this.estado.setY(centroZonaY);
    }

    public void handleMisao(Missao missao){
        // Variaveis da Missao
        double x1 = missao.getX1();
        double y1 = missao.getY1();
        double x2 = missao.getX2();
        double y2 = missao.getY2();
        double duracao = missao.getDuracao() * 60.0; // minutos para segundos
        final int STEP_MS = 200;

        // Variaveis do Estado do Rover
        double posX = this.estado.getX();
        double posY = this.estado.getY();
        int bateria = this.estado.getBateria();
        float velocidade = this.estado.getVelocidade();

        double vel_ms = (velocidade * 1000.0) / 3600.0;

        Random rand = new Random();
        int ciclosBateria = 0;

        while(duracao > 0){

            if(bateria == 0){
                this.estado.setEstadoOperacional(EstadoOperacional.INOPERACIONAL);
                return;
            }

            double ang = rand.nextDouble() * 2 * Math.PI;
            double dt = STEP_MS /1000.0;

            double dx = Math.cos(ang) * vel_ms * (STEP_MS/1000.0);
            double dy = Math.sin(ang) * vel_ms * (STEP_MS/1000.0);

            double novoX = posX + dx;
            double novoY = posY + dy;

            if(novoX >= x1 && novoX <= x2) posX = novoX;
            if(novoY >= y1 && novoY <= y2) posY = novoY;

            this.estado.setX(posX);
            this.estado.setY(posY);

            ciclosBateria++;
            if(ciclosBateria >= 300){
                bateria = Math.max(0, bateria - 1);
                this.estado.setBateria(bateria);
                ciclosBateria = 0;
            }
            
            duracao -= dt;

            try{Thread.sleep(STEP_MS);}
            catch(Exception e){
                System.out.println("[" + this.id + " ERRO] handleMissao: " + e.getMessage());
                e.printStackTrace();
            }
        }
        this.estado.setVelocidade(0);
    }

    public static void main(String[] args) {
    if(args.length < 4){
        System.out.println("Uso: java Rover <id_rover> <ip_rover> <porta_rover> <ip_nave_mae>");
        return;
    }

    try{
        String id = args[0];
        InetAddress ip = InetAddress.getByName(args[1]);
        int porta = Integer.parseInt(args[2]);
        InetAddress ip_NaveMae = InetAddress.getByName(args[3]);

        Rover rover = new Rover(id, ip, porta, ip_NaveMae);
        rover.startRover();

    }catch(UnknownHostException e){
        System.out.println("[Rover - ERRO] problema com IP: " + e.getMessage());
        e.printStackTrace();
    }
    }
}

/* 
 * javac -d bin $(find src -name "*.java")
 * java -cp bin core.Rover
 * */
