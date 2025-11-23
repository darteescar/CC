package protocols.udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ColetorReport {
    private final String idReport;
    private final int numFrames;
    private final byte[][] frames;
    private final boolean[] recebidos;
    private int countRecebidos;
    
    public ColetorReport(String idReport, int numFrames){
        this.idReport = idReport;
        this.numFrames = numFrames;
        this.frames = new byte[numFrames][];
        this.recebidos = new boolean[numFrames];
    }

    public void addFrame(int numSeq, byte[] frame){
        if(!recebidos[numSeq]){
            frames[numSeq] = frame;
            recebidos[numSeq] = true;
            countRecebidos++;
        }
    }

    public boolean estaCompleto(){
        return countRecebidos >= numFrames;
    }

    public byte[] arrayResposta(){
        byte[] resposta = new byte[numFrames];
        for(int i = 0; i < numFrames; i++){
            resposta[i] = (recebidos[i] ? (byte)1 : (byte)0); // True = 1 | False = 0
        }
        return resposta;
    }
    public synchronized byte[] reconstruirIMGBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < numFrames; i++) {
            if (frames[i] != null) baos.write(frames[i]);
            else throw new IOException("[ERRO - CR]Frame " + i + " em falta do Report " + idReport);
        }
        return baos.toByteArray();
    }


}
