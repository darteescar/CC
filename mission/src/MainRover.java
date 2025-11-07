import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainRover {

    public static void main(String[] args) {
        if(args.length < 4){
            System.out.println("Uso: java MainRover @id_rover @ip_rover @porta_rover @id_nave_mae @ip_nave_mae @porta_nave_mae");
            return;
        }

        try{
            String id = args[1];
            InetAddress ip = InetAddress.getByName(args[2]);
            int porta = Integer.parseInt(args[3]);
            String id_nave_mae = args[4];
            InetAddress ip_nave_mae = InetAddress.getByName(args[5]);
            int porta_nave_mae = Integer.parseInt(args[6]);

            Rover rover = new Rover(id, ip, porta, id_nave_mae, ip_nave_mae, porta_nave_mae);
            rover.startComms();
            rover.move();

        }catch(UnknownHostException e){
            System.out.println("Erro no ip: " + e);
        }
    }
    
}