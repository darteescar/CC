package data;

import interfaces.Codificavel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Estado implements Codificavel{
    private double x; // Coordenadas
    private double y; // Coordenada

    // volatile garante visibilidade das alterações entre threads sem sincronização
    private volatile EstadoOperacional estado_operacional;
    private int bateria;
    private float velocidade; // em Km/h

    /* ====== Construtores ====== */
    
    public Estado(){
        this.x = 0;
        this.y = 0;
        this.estado_operacional = EstadoOperacional.PARADO;
        this.bateria = 100;
        this.velocidade = 0;
    }

    public Estado(double x, double y, EstadoOperacional estado_operacional, int bateria, float velocidade){
        this.x = x;
        this.y = y;
        this.estado_operacional = estado_operacional;
        this.bateria = bateria;
        this.velocidade = velocidade;
    }

    public Estado(Estado e){
        this.x = e.getX();
        this.y = e.getY();
        this.estado_operacional = e.getEstadoOperacional();
        this.bateria = e.getBateria();
        this.velocidade = e.getVelocidade();
    }   

    /* ====== Getters & Setters ====== */

    public double getX(){
        return this.x;
    }
    
    public double getY(){
        return this.y;
    }

    public EstadoOperacional getEstadoOperacional(){
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

    public void setEstadoOperacional(EstadoOperacional estado_operacional){
        this.estado_operacional = estado_operacional;
    }

    public void setBateria(int bateria){
        this.bateria = bateria;
    }

    public void setVelocidade(float velocidade){
        this.velocidade = velocidade;
    }

    /* ====== Métodos ====== */

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeDouble(x);  // 8 bytes
            dos.writeDouble(y);  // 8 bytes
            dos.writeInt(estado_operacional.ordinal()); // 4 bytes
            dos.writeInt(bateria);  // 4 bytes
            dos.writeFloat(velocidade); // 4 bytes

            dos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("[ERRO] Converter Estado para ByteArray: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void fromByteArray(byte[] data){
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            DataInputStream dis = new DataInputStream(bais);

            this.x = dis.readDouble();
            this.y = dis.readDouble();
            this.estado_operacional = EstadoOperacional.values()[dis.readInt()];
            this.bateria = dis.readInt();
            this.velocidade = dis.readFloat();
        } catch (IOException e) {
            System.err.println("[ERRO] Converter ByteArray para Estado: " + e.getMessage());
        }
    }

    @Override
    public String toString(){
        return "Estado{" +
               "x=" + x +
               ", y=" + y +
               ", estado_operacional=" + estado_operacional +
               ", bateria=" + bateria +
               ", velocidade=" + velocidade +
               '}';
    }

    @Override
    public Estado clone(){
        return new Estado(this);
    }
}
