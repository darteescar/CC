package ground_control;

import data.Estado;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class RoverListPanel extends JPanel {

    private final Color[] cores;
    private final Map<String, Estado> estados;
    private final Map<String, File> reports;

    public RoverListPanel(Map<String, Estado> estados, Color[] cores, Map<String,File> reports) {
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

    public void atualizar() {
        removeAll();

        List<String> lista = new ArrayList<>(this.estados.keySet());
        Collections.sort(lista);

        int i = 0;
        for (String nome : lista) {
            add(criarPainel(nome, this.estados.get(nome),cores[i % cores.length], reports.get(nome)));
            i++;
        }

        revalidate();
        repaint();
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
            """
            <html>
                <b>Posição:</b> (%.1f, %.1f)<br>
                <b>Bateria:</b> <span style='color:%s'>%d%%</span><br>
                <b>Velocidade:</b> %.1f Km/h<br>
                <b>Estado:</b> %s
            </html>
            """,
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

        // ---- Botão para abrir imagem do relatório ----
        JButton botao = new JButton("Abrir Report");

        botao.setAlignmentX(CENTER_ALIGNMENT);

        botao.addActionListener(e -> {
            if (report == null || !report.exists()) {
                JOptionPane.showMessageDialog(p,
                        "A imagem do report não foi encontrada.",
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                BufferedImage img = ImageIO.read(report);
                ImageIcon icon = new ImageIcon(img);
                if (icon.getIconWidth() <= 0) {
                    JOptionPane.showMessageDialog(p,
                            "Não foi possível carregar a imagem.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JFrame janelaImagem = new JFrame("Report - " + nome);
                janelaImagem.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                janelaImagem.setSize(1000, 600);
                janelaImagem.setLayout(new BoxLayout(janelaImagem.getContentPane(), BoxLayout.Y_AXIS));

                JLabel labelNome = new JLabel("Último report do " + nome);
                labelNome.setFont(new Font("Arial", Font.PLAIN, 25));
                labelNome.setBorder(null);
                labelNome.setAlignmentX(CENTER_ALIGNMENT);
                janelaImagem.add(labelNome);

                JLabel labelImagem = new JLabel(icon);
                labelImagem.setBorder(null);

                // Criar scroll sem borda
                JScrollPane scroll = new JScrollPane(labelImagem);
                scroll.setBorder(BorderFactory.createEmptyBorder());

                janelaImagem.add(scroll);

                janelaImagem.setVisible(true);
            } catch (IOException e2) {
                    System.out.println("Imagem buffered deu erro:" + e2.getMessage());
            }        
        });

        p.add(botao);

        return p;
    }

}