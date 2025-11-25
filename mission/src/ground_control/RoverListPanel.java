package ground_control;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import data.Estado;

public class RoverListPanel extends JPanel {

    private final Color[] cores;
    private final Map<String, Estado> estados;

    public RoverListPanel(Map<String, Estado> estados, Color[] cores) {
        this.estados = estados;
        this.cores = cores;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.LIGHT_GRAY);
    }

    public void atualizar() {
        removeAll();

        int i = 0;
        for (var entry : estados.entrySet()) {
            add(criarPainel(entry.getKey(), entry.getValue(),
                    cores[i % cores.length]));
            i++;
        }

        revalidate();
        repaint();
    }

    private JPanel criarPainel(String nome, Estado estado, Color cor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        JLabel titulo = new JLabel(nome);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(cor);
        titulo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel info = new JLabel(String.format(
            "<html>"
                + "Posição: (%.1f, %.1f)<br>"
                + "Bateria: %d%%<br>"
                + "Velocidade: %.1f m/s</html>",
            estado.getX(), estado.getY(),
            estado.getBateria(),
            estado.getVelocidade()
        ));
        info.setFont(new Font("Arial", Font.PLAIN, 18));
        info.setAlignmentX(CENTER_ALIGNMENT);

        p.add(titulo);
        p.add(info);
        return p;
    }
}

