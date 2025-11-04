package data;

import java.io.IOException;

import interfaces.Codificavel;

public class Missao implements Codificavel{
    private final int id;
    private double x1;
    private double y1;
    private double x2;
    private double y2;
    private String tarefa;
    private int duracao; // em minutos
    private int freq_update;

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

    public byte[] encode() throws IOException{
        return null;
    }

    public void decode(byte[] data) throws IOException{
    }

    public Missao clone(){
        return new Missao(this);
    }
}
