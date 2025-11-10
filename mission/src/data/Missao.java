package data;

import interfaces.Codificavel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Missao implements Codificavel, Comparable<Missao>{
    private int id;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private String tarefa;
    private int duracao; // em minutos
    private int freq_update; // em segundos

    /* ====== Construtores ====== */

    public Missao(){
        this.id = 0;
        this.x1 = 0;
        this.y1 = 0;
        this.x2 = 0;
        this.y2 = 0;
        this.tarefa = "";
        this.duracao = 0;
        this.freq_update = 0;
    }

    public Missao(int id, double x1, double y1, double x2, double y2, String tarefa, int duracao, int freq_update){
        this.id = id;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.tarefa = tarefa;
        this.duracao = duracao;
        this.freq_update = freq_update;
    }

    public Missao(Missao m){
        this.id = m.getId();
        this.x1 = m.getX1();
        this.y1 = m.getY1();
        this.x2 = m.getX2();
        this.y2 = m.getY2();
        this.tarefa = m.getTarefa();
        this.duracao = m.getDuracao();
        this.freq_update = m.getFreqUpdate();
    }

    /* ====== Getters & Setters ====== */

    public int getId(){
        return this.id;
    }

    public double getX1(){
        return this.x1;
    }

    public double getY1(){
        return this.y1;
    }

    public double getX2(){
        return this.x2;
    }

    public double getY2(){
        return this.y2;
    }

    public String getTarefa(){
        return this.tarefa;
    }

    public int getDuracao(){
        return this.duracao;
    }

    public int getFreqUpdate(){
        return this.freq_update;
    }

    /* Métodos */

    @Override
    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(id); // 4 bytes
            dos.writeDouble(x1); // 8 bytes
            dos.writeDouble(y1); // 8 bytes
            dos.writeDouble(x2); // 8 bytes
            dos.writeDouble(y2); // 8 bytes
            byte[] tarefaBytes = tarefa.getBytes("UTF-8");
            dos.writeInt(tarefaBytes.length); // 4 bytes para o tamanho da string
            dos.write(tarefaBytes); // bytes da string
            dos.writeInt(duracao); // 4 bytes
            dos.writeInt(freq_update); // 4 
            
            dos.flush();
            return baos.toByteArray();
        }
        catch (IOException e) {
            System.err.println("[ERRO] Converter Missão para ByteArray: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void fromByteArray(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);

            this.id = dis.readInt();

            this.x1 = dis.readDouble();
            this.y1 = dis.readDouble();
            this.x2 = dis.readDouble();
            this.y2 = dis.readDouble();
            int tarefaLength = dis.readInt();
            byte[] tarefaBytes = new byte[tarefaLength];
            dis.readFully(tarefaBytes);
            this.tarefa = new String(tarefaBytes, "UTF-8");
            this.duracao = dis.readInt();
            this.freq_update = dis.readInt();
            
        } catch (IOException e) {
            System.err.println("[ERRO] Converter ByteArray para Missão: " + e.getMessage());
        }
    }

    @Override
    public Missao clone(){
        return new Missao(this);
    }

    @Override
    public String toString(){
        return "Missao{" +
                "id=" + id +
                ", x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", tarefa='" + tarefa + '\'' +
                ", duracao=" + duracao +
                ", freq_update=" + freq_update +
                '}';
    }

    @Override
    public int compareTo(Missao other) {
        // Ordenação por id ascendente (IDs mais baixos têm maior prioridade).
        // Ajusta aqui se preferires outra política (e.g., duração, área, etc.).
        return Integer.compare(this.id, other.id);
    }
}
