import data.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class MainNaveMae {

    public static void main(String[] args) {
        if(args.length < 4 + 3*Integer.parseInt(args[3])){
            System.out.println("Uso: java MainNaveMae @ip @porta @NumeroDeRovers @id1 @ip1 @porta1 @idX @ipX @portaX ");
            return;
        }

        NaveMae naveMae = null;

        try{
            InetAddress ip = InetAddress.getByName(args[1]);
            int porta = Integer.parseInt(args[2]);
            int nRovers = Integer.parseInt(args[3]);

            Map<String, Estado> m1 = new HashMap<>();
            Map<String, InetAddress> m2 = new HashMap<>();
            Map<String, Integer> m3 = new HashMap<>();
            
            for(int i = 0; i < nRovers; i++){
                String id = args[4 + i*3];
                InetAddress roverIP = InetAddress.getByName(args[5 + i*3]);
                int roverPorta = Integer.parseInt(args[6 + i*3]);
                m1.put(id, new Estado());
                m2.put(id, roverIP);
                m3.put(id, roverPorta);
            }
            naveMae = new NaveMae(ip, porta, m1, m2, m3);
        }catch(UnknownHostException e){
            System.out.println("Erro no ip: " + e);
        }

        naveMae.adicionarMissaoQueue(new Missao(2367, 1, 2, 3, 4, "Anda por aí", 5, 5));
        naveMae.adicionarMissaoQueue(new Missao(2368, 1, 2, 3, 4, "Anda por aí", 5, 5));
        naveMae.adicionarMissaoQueue(new Missao(2369, 1, 2, 3, 4, "Anda por aí", 5, 5));

        if (naveMae != null) {
            naveMae.startNaveMae();
        }
    }
    
}
