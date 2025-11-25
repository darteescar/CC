package ground_control;

public class GroundControl {

    public static void main(String[] args) {
        new GroundControl().run();
    }

    private void run() {

        RoversInfo infos = new RoversInfo("http://10.0.6.21:7000");

        GroundControlGUI gui = new GroundControlGUI(
                infos.getEstados(),
                infos.getMissoes()
        );

        while (true) {
            try {
                infos.atualizarDados(); 
                gui.atualizar();         
                Thread.sleep(1000);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}

