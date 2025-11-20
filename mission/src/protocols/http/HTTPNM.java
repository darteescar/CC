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
import java.util.Set;

public class HTTPNM {

    private HttpServer server;
    private NaveMae naveMae;

    public HTTPNM(int porta, NaveMae naveMae) throws IOException {
        this.naveMae = naveMae;
        this.server = HttpServer.create(new InetSocketAddress(porta), 0);

        server.createContext("/getNumeroRovers", new NumeroRoversHandler());
        server.createContext("/getEstadoRover", new EstadoRoverHandler());
        server.createContext("/getMissaoRover", new MissaoRoverHandler());
        server.createContext("/getListaRovers", new ListaRoversHandler());
    }

    public void start() {
        server.setExecutor(null);
        server.start();
        System.out.println("[HTTPNaveMae] Servidor HTTP ativo na porta " + server.getAddress().getPort());
    }

    // ==========================================================

    private class NumeroRoversHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int numeroRovers = naveMae.getNumeroRovers();

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, Integer.BYTES);

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()));
            out.writeInt(numeroRovers);
            out.flush();
            out.close();
        }
    }

    private class ListaRoversHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Set<String> rovers = naveMae.getRoversID();

            String result = String.join(",", rovers);
            byte[] bytes = result.getBytes();

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, bytes.length);

            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        }
    }

    private class EstadoRoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String[] partes = query.split("=");
            if (partes.length != 2 || !partes[0].equals("nome")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            Estado estado = naveMae.getEstadoRover(partes[1]);
            if (estado == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] bytes = estado.toByteArray();

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, bytes.length);

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()));
            out.write(bytes);
            out.flush();
            out.close();
        }
    }

    private class MissaoRoverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String[] partes = query.split("=");
            if (partes.length != 2 || !partes[0].equals("nome")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            Missao missao = naveMae.getMissaoRover(partes[1]);
            if (missao == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] bytes = missao.toByteArray();

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, bytes.length);

            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(exchange.getResponseBody()));
            out.write(bytes);
            out.flush();
            out.close();
        }
    }
}
