package ground_control;

import data.Estado;
import java.awt.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class RoverListPanel extends JPanel {

    private final Color[] cores;
    private final Map<String, Estado> estados;
    private final Map<String, File> reports;

    // Maps para atualizar componentes individuais
    private final Map<String, JPanel> paineisRovers = new HashMap<>();
    private final Map<String, JButton> botoesReports = new HashMap<>();

    public RoverListPanel(Map<String, Estado> estados, Color[] cores, Map<String, File> reports) {
        this.estados = estados;
        this.cores = cores;
        this.reports = reports;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.GRAY);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(),
            "Rovers",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 24),
            Color.WHITE
        ));
    }

    // Atualiza painéis ou botões conforme necessidade
    public void atualizar() {
        for (String nome : estados.keySet()) {
            Estado estado = estados.get(nome);
            File report = reports.get(nome);

            if (paineisRovers.containsKey(nome)) {
                // Atualiza botão do report apenas
                JButton btn = botoesReports.get(nome);
                atualizarBotaoReport(btn, report);
            } else {
                // Cria novo painel e botão
                JPanel painel = criarPainel(nome, estado, cores[paineisRovers.size() % cores.length], report);
                paineisRovers.put(nome, painel);
                add(painel);
            }
        }

        revalidate();
        repaint();
    }

    private void atualizarBotaoReport(JButton btn, File report) {
        btn.setEnabled(report != null && report.exists());
        btn.setToolTipText(report != null ? report.getName() : "Sem report");

        // Remove listeners antigos
        for (var al : btn.getActionListeners()) {
            btn.removeActionListener(al);
        }

        if (report != null && report.exists()) {
            btn.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(report);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(btn,
                            "Não foi possível abrir o report:\n" + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private JPanel criarPainel(String nome, Estado estado, Color cor, File report) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        JLabel titulo = new JLabel(nome);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(cor);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        String estadoRover = switch (estado.getEstadoOperacional()) {
            case EM_MISSAO -> "Em Missão";
            case ESPERA_MISSAO -> "Espera Missão";
            case A_CAMINHO -> "A Caminho";
            case PARADO -> "Parado";
            default -> "Inoperacional";
        };

        int bateria = estado.getBateria();
        String corBateria = bateria >= 75 ? "green" : (bateria >= 40 ? "orange" : "red");

        JLabel info = new JLabel(String.format(
                "<html>"
                + "<b>Posição:</b> (%.1f, %.1f)<br>"
                + "<b>Bateria:</b> <span style='color:%s'>%d%%</span><br>"
                + "<b>Velocidade:</b> %.1f Km/h<br>"
                + "<b>Estado:</b> %s"
                + "</html>",
                estado.getX(), estado.getY(),
                corBateria,
                bateria,
                estado.getVelocidade(),
                estadoRover
        ));
        info.setFont(new Font("Arial", Font.PLAIN, 18));
        info.setAlignmentX(CENTER_ALIGNMENT);

        p.add(titulo);
        p.add(info);

        JButton btnReport = new JButton("Ver Report");
        btnReport.setAlignmentX(CENTER_ALIGNMENT);
        atualizarBotaoReport(btnReport, report);
        botoesReports.put(nome, btnReport);

        p.add(Box.createVerticalStrut(10));
        p.add(btnReport);

        return p;
    }
}
