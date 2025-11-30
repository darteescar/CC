package protocols.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import core.NaveMae;
import data.Estado;
import data.Missao;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HTTPNM {

    private HttpServer server;
    private NaveMae naveMae;

    public HTTPNM(int porta, NaveMae naveMae) throws IOException {
        this.naveMae = naveMae;
        this.server = HttpServer.create(new InetSocketAddress(porta), 0);

        server.createContext("/rovers", new RoverRouter());
        server.createContext("/missoes/concluidas", new MissoesConcluidas());
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("[HTTPNaveMae] Servidor HTTP ativo na porta " + server.getAddress().getPort());
    }

    // ==========================================================
    // Contextos HTTP
    // ==========================================================

    private class MissoesConcluidas implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            enviarListaMissoesConcluidas(exchange);
        }
    }

    private class RoverRouter implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String[] partes = path.split("/");

            // partes[0] = "" (vazio)
            // partes[1] = "rovers"
            // partes[2] = "{id}" (opcional)
            // partes[3] = "estado" ou "missao" (opcional)

            try {
                if (partes.length == 2) { // /rovers
                    enviarListaRovers(exchange);
                } else if (partes.length == 4) { // /rovers/{id}/estado ou /missao
                    String id = partes[2];
                    String acao = partes[3];

                    switch (acao) {
                        case "estado":
                            enviarEstadoRover(exchange, id);
                            break;
                        case "missao":
                            enviarMissaoRover(exchange, id);
                            break;
                        default:
                            exchange.sendResponseHeaders(404, -1);
                            break;
                    }
                } else { // URL inválida
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        }
    }

    // ==========================================================
    // Métodos de envio de dados
    // ==========================================================

    public void enviarListaRovers(HttpExchange exchange) throws IOException {
        List<String> lista_ids = this.naveMae.getRoversID();

        if (lista_ids == null) {
            lista_ids = List.of(); // lista vazia
        }

        exchange.sendResponseHeaders(200, 0);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()))) {
            out.writeInt(lista_ids.size());
            for (String id : lista_ids) {
                byte[] bytes = id.getBytes(StandardCharsets.UTF_8);
                out.writeInt(bytes.length);
                out.write(bytes);
            }
            out.flush();
        }
    }

    public void enviarEstadoRover(HttpExchange exchange, String id) throws IOException {
        Estado estado = this.naveMae.getEstadoRover(id);

        if (estado == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        byte[] bytes = estado.toByteArray();
        exchange.sendResponseHeaders(200, bytes.length);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()))) {
            out.write(bytes);
            out.flush();
        }
    }

    public void enviarMissaoRover(HttpExchange exchange, String id) throws IOException {
        Missao missao = this.naveMae.getMissaoRover(id);

        if (missao == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        byte[] bytes = missao.toByteArray();
        exchange.sendResponseHeaders(200, bytes.length);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()))) {
            out.write(bytes);
            out.flush();
        }
    }

    public void enviarListaMissoesConcluidas(HttpExchange exchange) throws IOException {
        List<Missao> missoes = this.naveMae.getMissoesConcluidas();

        if (missoes == null) {
            missoes = List.of(); // lista vazia
        }

        exchange.sendResponseHeaders(200, 0);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()))) {
            out.writeInt(missoes.size());
            for (Missao missao : missoes) {
                byte[] bytes = missao.toByteArray();
                out.writeInt(bytes.length);
                out.write(bytes);
            }
            out.flush();
        }
    }
}
