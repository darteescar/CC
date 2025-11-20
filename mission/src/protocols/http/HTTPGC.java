package protocols.http;

import data.Estado;
import data.Missao;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;

public class HTTPGC {
     private final String URL;

     public HTTPGC(String URL) {
          this.URL = URL;
     }

     private byte[] getBytes(String endpoint) throws Exception {
          URI uri = new URI(this.URL + endpoint); // garante que a URL está bem formada, i.e., sem espaços ou caracteres especiais inválidos
          URL url = uri.toURL(); // converte URI para URL 

          HttpURLConnection con = (HttpURLConnection) url.openConnection();

          con.setRequestMethod("GET");
          con.setDoInput(true);

          int responseCode = con.getResponseCode();
          if (responseCode != 200) {
               throw new RuntimeException("HTTP GET falhou: " + responseCode);
          }

          try (InputStream in = con.getInputStream();
               DataInputStream dataIn = new DataInputStream(new BufferedInputStream(in))) {
               return dataIn.readAllBytes();
          } catch (Exception e) {
               throw new RuntimeException("Erro ao ler resposta HTTP: " + e.getMessage(), e);
          } finally {
               con.disconnect();
          }
     }

     public int getNumeroRovers() throws Exception {
          byte[] bytes = getBytes("/getnumeroRovers");
          return ByteBuffer.wrap(bytes).getInt(); // converte o array de bytes em um buffer e lê 4 bytes seguintes como um inteiro
     }

     public Estado getEstadoRover(String nome) throws Exception {
          byte[] bytes = getBytes("/getestadoRover?nome=" + nome);
          Estado estado = new Estado();
          estado.fromByteArray(bytes);
          return estado;
     }

      public Missao getMissaoRover(String nome) throws Exception {
          byte[] bytes = getBytes("/getmissaoRover?nome=" + nome);
          Missao missao = new Missao();
          missao.fromByteArray(bytes);
          return missao;
     }
}