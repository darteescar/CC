package nave_mae;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainNaveMae {

    public static void main(String[] args) {
        if(args.length < 3 + 3*Integer.parseInt(args[2])){
            System.out.println("Uso: java MainNaveMae @ip");
            return;
        }
        try{
            InetAddress ip = InetAddress.getByName(args[1]);
            NaveMae naveMae = new NaveMae(ip);
            // Iniciar a NaveMae

        }catch(UnknownHostException e){
            System.out.println("[NaveMae - ERRO]: problema com IP: " + e.getMessage());
        }
    }
}
