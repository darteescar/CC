package data;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Mensagem implements Serializable {
    private TipoMensagem tm;
    private String id_org;
    private InetAddress ip_org;
    private int porta_org;
    private String id_dest;
    private InetAddress ip_dest;
    private int porta_dest;
    private byte[] payload;

    /* ====== Construtor ====== */

    public Mensagem(TipoMensagem tm, String id_org, InetAddress ip_org, int porta_org,
                    String id_dest, InetAddress ip_dest, int porta_dest, byte[] payload) {
        this.tm = tm;
        this.id_org = id_org;
        this.ip_org = ip_org;
        this.porta_org = porta_org;
        this.id_dest = id_dest;
        this.ip_dest = ip_dest;
        this.porta_dest = porta_dest;
        this.payload = (payload != null) ? payload : new byte[0];
    }

    /* ====== Getters & Setters ====== */
    public TipoMensagem getTipo() { 
        return tm; 
    }

    public String getIdOrg() { 
        return id_org; 
    }

    public String getIdDest() { 
        return id_dest; 
    }

    public InetAddress getIpOrg() { 
        return ip_org; 
    }

    public InetAddress getIpDest() { 
        return ip_dest; 
    }

    public int getPortaOrg() { 
        return porta_org; 
    }

    public int getPortaDest() { 
        return porta_dest; 
    }

    public byte[] getPayload() { 
        return payload; 
    }

    /* ====== Métodos ====== */

    @Override
    public String toString() {
        return "Mensagem{" +
                "tm=" + tm +
                ", id_org='" + id_org + '\'' +
                ", ip_org=" + ip_org.getHostAddress() +
                ", porta_org=" + porta_org +
                ", id_dest='" + id_dest + '\'' +
                ", ip_dest=" + ip_dest.getHostAddress() +
                ", porta_dest=" + porta_dest +
                ", payload_length=" + payload.length +
                '}';
        
    }

    // Serialização
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Tipo da mensagem (1 byte)
        dos.writeByte(tm.ordinal());

        // IDs (strings com tamanho prefixado)
        Mensagem.writeString(dos, id_org);
        Mensagem.writeString(dos, id_dest);

        // Endereços e portas
        dos.write(ip_org.getAddress());
        dos.writeInt(porta_org);
        dos.write(ip_dest.getAddress());
        dos.writeInt(porta_dest);

        // Payload
        dos.writeInt(payload.length);
        dos.write(payload);

        dos.flush();
        return baos.toByteArray();
    }

    // Deserialização 
    public static Mensagem fromByteArray(byte[] bytes) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

        TipoMensagem tm = TipoMensagem.values()[dis.readByte()];

        String id_org = readString(dis);
        String id_dest = readString(dis);

        byte[] ipOrgBytes = new byte[4];
        dis.readFully(ipOrgBytes);
        InetAddress ip_org = InetAddress.getByAddress(ipOrgBytes);
        int porta_org = dis.readInt();

        byte[] ipDestBytes = new byte[4];
        dis.readFully(ipDestBytes);
        InetAddress ip_dest = InetAddress.getByAddress(ipDestBytes);
        int porta_dest = dis.readInt();

        int payloadLen = dis.readInt();
        byte[] payload = new byte[payloadLen];
        dis.readFully(payload);

        return new Mensagem(tm, id_org, ip_org, porta_org, id_dest, ip_dest, porta_dest, payload);
    }

    /* ======= Métodos Auxiliares ======= */

    private static void writeString(DataOutputStream dos, String s) throws IOException {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(data.length);
        dos.write(data);
    }

    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        byte[] data = new byte[len];
        dis.readFully(data);
        return new String(data, StandardCharsets.UTF_8);
    }
}