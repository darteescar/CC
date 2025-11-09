import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainRover {

    public static void main(String[] args) {
        if(args.length < 6){
            System.out.println("Uso: java MainRover @id_rover @ip_rover @porta_rover @id_nave_mae @ip_nave_mae @porta_nave_mae");
            return;
        }

        try{
            String id = args[0];
            InetAddress ip = InetAddress.getByName(args[1]);
            int porta = Integer.parseInt(args[2]);
            String id_nave_mae = args[3];
            InetAddress ip_nave_mae = InetAddress.getByName(args[4]);
            int porta_nave_mae = Integer.parseInt(args[5]);

            Rover rover = new Rover(id, ip, porta, id_nave_mae, ip_nave_mae, porta_nave_mae);
            rover.startComms();

        }catch(UnknownHostException e){
            System.out.println("Erro no ip: " + e);
        }

        while(true){
            // Manter o programa em execução
        }
    }
    
}