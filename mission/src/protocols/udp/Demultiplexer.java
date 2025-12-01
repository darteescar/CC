package protocols.udp;

import data.Mensagem;
import data.Report;
import data.TipoMensagem;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Demultiplexer implements Runnable {

    private final DatagramSocket socket;

    // Queue para mensagens de missão (SYN, DATA, CONFIRM…)
    private final BlockingQueue<Mensagem> queueMissao = new LinkedBlockingQueue<>();

    // Map de queues para cada report (idReport → queue)
    private final Map<String, BlockingQueue<Report>> queueReports = new ConcurrentHashMap<>();

    private volatile boolean running = true;

    public Demultiplexer(DatagramSocket socket) {
        this.socket = socket;
    }

    public BlockingQueue<Mensagem> getMissaoQueue() {
        return queueMissao;
    }

    public BlockingQueue<Report> getReportQueue(String idReport) {
        return queueReports.computeIfAbsent(idReport, k -> new LinkedBlockingQueue<>());
    }

    public void removeReportQueue(String idReport) {
        queueReports.remove(idReport);
    }

    @Override
    public void run() {
        while (running) {
            try {
                byte[] buf = new byte[65507];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                socket.receive(p);

                /// Descodifica a Mensagem
                Mensagem temp = Mensagem.fromByteArray(p.getData());
                TipoMensagem tipo = temp.getTipo();

                Mensagem m;
                if (tipo == TipoMensagem.ML_FRAMES ||
                    tipo == TipoMensagem.ML_REPORT ||
                    tipo == TipoMensagem.ML_END ||
                    tipo == TipoMensagem.ML_MISS ||
                    tipo == TipoMensagem.ML_OK ||
                    tipo == TipoMensagem.ML_FIN ||
                    tipo == TipoMensagem.ML_FINACK ||
                    tipo == TipoMensagem.ML_STOP_CON) {

                    m = Report.fromByteArray(p.getData());
                    Report r = (Report) m;
                    String idReport = r.getIdReport();
                    getReportQueue(idReport).offer(r);

                } else {
                    m = temp;
                    queueMissao.offer(m);
                }

            } catch (Exception e) {
                if (running) e.printStackTrace();
            }
        }
    }


    public void stop() {
        running = false;
        socket.close();
    }
}
