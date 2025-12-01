package protocols.http;

import data.Estado;
import data.Missao;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HTTPGC {

    private final String baseUrl;

    public HTTPGC(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private DataInputStream getStream(String endpoint) throws Exception {
        URI uri = new URI(baseUrl + endpoint);
        URL url = uri.toURL();

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();

        InputStream in;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            in = con.getInputStream();
        } else {
            System.out.println("HTTP GET falhou: " + responseCode);
            in = con.getErrorStream();
            if (in == null) {
                // O servidor não enviou corpo de erro
                throw new RuntimeException("HTTP GET falhou sem corpo de erro: " + responseCode);
            }
        }

        return new DataInputStream(new BufferedInputStream(in));
    }

    // ======================================================
    // 1) LISTA DE ROVERS
    // ======================================================

    public List<String> getListaRovers() throws Exception {
        DataInputStream in = getStream("/rovers");

        int total = in.readInt();
        List<String> lista = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            int size = in.readInt();
            byte[] bytes = in.readNBytes(size);
            lista.add(new String(bytes, StandardCharsets.UTF_8));
        }

        return lista;
    }

    // ======================================================
    // 2) ESTADO DO ROVER
    // ======================================================

    public Estado getEstadoRover(String id) throws Exception {
        DataInputStream in = getStream("/rovers/" + id + "/estado");

        byte[] all = in.readAllBytes();
        Estado e = new Estado();
        e.fromByteArray(all);
        return e;
    }

    // ======================================================
    // 3) MISSÃO DO ROVER
    // ======================================================

    public Missao getMissaoRover(String id) throws Exception {
        DataInputStream in = getStream("/rovers/" + id + "/missao");

        byte[] all = in.readAllBytes();
        Missao m = new Missao();
        m.fromByteArray(all);
        return m;
    }

    // ======================================================
    // 4) MISSÕES CONCLUÍDAS
    // ======================================================

    public List<Missao> getMissoesConcluidas() throws Exception {
        DataInputStream in = getStream("/missoes/concluidas");

        int total = in.readInt();
        List<Missao> lista = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            int size = in.readInt();
            byte[] bytes = in.readNBytes(size);

            Missao m = new Missao();
            m.fromByteArray(bytes);
            lista.add(m);
        }

        return lista;
    }

    // ======================================================
    // 4) MAP DE REPORTS
    // ======================================================


    public Map<String, File> getMapReports() throws Exception {
        DataInputStream in = getStream("/reports");

        Map<String, File> reports = new ConcurrentHashMap<>();

        int total = in.readInt();

        for (int i = 0; i < total; i++) {

            // ---- ID ----
            int numBytes_id = in.readInt();
            byte[] idArray = in.readNBytes(numBytes_id);
            String id = new String(idArray, StandardCharsets.UTF_8);

            // ---- IMAGEM ----
            int numBytes_file = in.readInt();
            byte[] fileBytes = in.readNBytes(numBytes_file);

            // ---- CRIAR FICHEIRO TEMPORÁRIO ----
            File f = new File("img/groundControl/" + id + ".png");  
            //f.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(fileBytes);
            }

            reports.put(id, f);
        }

        return reports;
    }

}
