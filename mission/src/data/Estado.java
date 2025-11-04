package data;

import java.io.IOException;

import interfaces.Codificavel;

public class Estado implements Codificavel{
    private double x;
    private double y;
    private String estado_operacional;
    private int bateria;
    private float velocidade; // em Km/h

    public Estado(){
        this.x = 0;
        this.y = 0;
        this.estado_operacional = "Parado";
        this.bateria = 100;
        this.velocidade = 0;
    }

    public Estado(double x, double y, String estado_operacional, int bateria, float velocidade){
        this.x = x;
        this.y = y;
        this.estado_operacional = estado_operacional;
        this.bateria = bateria;
        this.velocidade = velocidade;
    }

    public double getX(){
        return this.x;
    }
    
    public double getY(){
        return this.y;
    }

    public String getEstadoOperacional(){
        return this.estado_operacional;
    }

    public int getBateria(){
        return this.bateria;
    }

    public float getVelocidade(){
        return this.velocidade;
    }

    public void setX(double x){
        this.x = x;
    }

    public void setY(double y){
        this.y = y;
    }

    public void setEstadoOperacional(String estadp_operacional){
        this.estado_operacional = estadp_operacional;
    }

    public void setBateria(int bateria){
        this.bateria = bateria;
    }

    public void setVelocidade(float velocidade){
        this.velocidade = velocidade;
    }

    public byte[] encode() throws IOException{
        return null;
    }

    public void decode(byte[] data) throws IOException{

    }
}
