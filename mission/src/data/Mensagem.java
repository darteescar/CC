package data;

import java.net.InetAddress;

public class Mensagem {
    private TipoMensagem tm;
    private String id_org;
    private InetAddress ip_org;
    private int porta_org;
    private String id_dest;
    private InetAddress ip_dest;
    private int porta_dest;
    private byte[] payload;
    // numero de sequencia

    public Mensagem(TipoMensagem tm, String id_org, InetAddress ip_org, int porta_org, String id_dest, InetAddress ip_dest, int porta_dest, byte[] payload){
        this.tm = tm;
        this.id_org = id_org;
        this.ip_org = ip_org;
        this.porta_org = porta_org;
        this.id_dest = id_dest;
        this.ip_dest = ip_dest;
        this.porta_dest = porta_dest;
        this.payload = payload;
    }
}
