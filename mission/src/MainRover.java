import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainRover {

    public static void main(String[] args) {
        if(args.length < 4){
            System.out.println("Uso: java MainRover @id @ip @porta");
            return;
        }

        try{
            String id = args[1];
            InetAddress ip = InetAddress.getByName(args[2]);
            int porta = Integer.parseInt(args[3]);

            Rover rover = new Rover(id, ip, porta);
            rover.startComms();
            rover.start();

        }catch(UnknownHostException e){
            System.out.println("Erro no ip: " + e);
        }
    }
    
}