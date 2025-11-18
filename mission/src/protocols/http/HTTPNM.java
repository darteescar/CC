package protocols.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import core.NaveMae;
import data.Estado;
import data.Missao;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Map;

public class HTTPNM {

    private HttpServer server;
    private NaveMae naveMae;
    private Gson gson = new Gson();

    public HTTPNM(int porta, NaveMae naveMae) throws IOException {
        this.naveMae = naveMae;
        this.server = HttpServer.create(new InetSocketAddress(porta), 0);

        // Endpoints da API
        server.createContext("/numeroRovers", new NumeroRoversHandler());
        server.createContext("/estadoRover", new EstadoRoverHandler());
        server.createContext("/missaoRover", new MissaoRoverHandler());
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("[HTTPNaveMae] Servidor HTTP ativo na porta " + server.getAddress().getPort());
    }

    // ================== Handlers ==================

    private class NumeroRoversHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int numero = naveMae.getNumeroRovers();
            String json = gson.toJson(Map.of("numero", numero));

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        }
    }

    private class EstadoRoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery(); // ex: "nome=R-1"
            if (query == null || !query.startsWith("nome=")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            String nome = query.split("=")[1];
            Estado estado = naveMae.getEstadoRover(nome);

            if (estado == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String json = gson.toJson(estado);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        }
    }

    private class MissaoRoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || !query.startsWith("nome=")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            String nome = query.split("=")[1];
            Missao missao = naveMae.getMissaoRover(nome);

            if (missao == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String json = gson.toJson(missao);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        }
    }
}