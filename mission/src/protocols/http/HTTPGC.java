package protocols.http;

import data.Estado;
import data.Missao;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HTTPGC {

    private final String URL;

    public HTTPGC(String URL) {
        this.URL = URL;
    }

    private byte[] getBytes(String endpoint) throws Exception {
        URI uri = new URI(this.URL + endpoint);
        URL url = uri.toURL();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setDoInput(true);

        int responseCode = con.getResponseCode();
        if (responseCode != 200)
            throw new RuntimeException("HTTP GET falhou: " + responseCode);

        try (InputStream in = con.getInputStream();
             DataInputStream dataIn = new DataInputStream(new BufferedInputStream(in))) {
            return dataIn.readAllBytes();
        } finally {
            con.disconnect();
        }
    }

    public List<String> getListaRovers() throws Exception {
        byte[] bytes = getBytes("/getListaRovers");

        String txt = new String(bytes).trim();
        if (txt.isEmpty())
            return List.of();

        return Arrays.asList(txt.split(","));
    }

    public Estado getEstadoRover(String nome) throws Exception {
        byte[] bytes = getBytes("/getEstadoRover?nome=" + nome);
        Estado estado = new Estado();
        estado.fromByteArray(bytes);
        return estado;
    }

    public Missao getMissaoRover(String nome) throws Exception {
        byte[] bytes = getBytes("/getMissaoRover?nome=" + nome);
        Missao missao = new Missao();
        missao.fromByteArray(bytes);
        return missao;
    }
}
