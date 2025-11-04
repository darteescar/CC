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

    public byte[] encode() throws IOException{
        return null;
    }

    public void decode(byte[] data) throws IOException{
    }
}
