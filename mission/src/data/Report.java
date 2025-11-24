package data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;


public class Report extends Mensagem {
    private String idReport;
    private int numSeq;

    /* ====== Construtor ====== */

    public Report(TipoMensagem tm, String id_org, InetAddress ip_org, int porta_org,
                    String id_dest, InetAddress ip_dest, int porta_dest, byte[] payload,
                    String idReport, int numSeq) {
                        
        super(tm, id_org, ip_org, porta_org, id_dest, ip_dest, porta_dest, payload);
        this.idReport = idReport;
        this.numSeq = numSeq;
    }

    /* ====== Getters e Setters ====== */

    public String getIdReport(){
        return this.idReport;
    }

    public int getNumSeq(){
        return this.numSeq;
    }

    /* ====== Métodos ====== */

    // Serialização
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        // Tipo da mensagem (1 byte)
        dos.writeByte(this.getTipo().ordinal());

        // IDs (strings com tamanho prefixado)
        Mensagem.writeString(dos, this.getIdOrg());
        Mensagem.writeString(dos, this.getIdDest());

        // Endereços e portas
        dos.write(this.getIpOrg().getAddress());
        dos.writeInt(this.getPortaOrg());
        dos.write(this.getIpDest().getAddress());
        dos.writeInt(this.getPortaDest());

        // Payload
        dos.writeInt(this.getPayload().length);
        dos.write(this.getPayload());

        //IdReport
        Mensagem.writeString(dos, idReport);

        // NumSeq
        dos.writeInt(numSeq);

        dos.flush();
        return baos.toByteArray();
    }

    // Deserialização 
    public static Report fromByteArray(byte[] bytes) throws IOException {
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

        String idReport = readString(dis);
        int numSeq = dis.readInt();

        return new Report(tm, id_org, ip_org, porta_org, id_dest, ip_dest, porta_dest, payload, idReport, numSeq );
    }
}
