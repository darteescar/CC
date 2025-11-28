package data;

public enum TipoMensagem {
    ML_SYN((byte)1),    // Rover para NaveMae (comum UDP)

    ML_SYNACK((byte)2), // NaveMae para Rover (comum UDP)

    ML_REQUEST((byte)3),// Rover para NaveMae (comum UDP)

    ML_DATA((byte)4),   // NaveMae para Rover (enviar missao)

    ML_CONFIRM((byte)5),// Rover para NaveMae (confirmar rececao)

    ML_CONFIRM_ACK((byte)6),// Rover para NaveMae (confirmar rececao)

    TS_TCP((byte)7),    // Rover para NaveMae (comum TCP)

    ML_FRAMES((byte)8), // Rover para NaveMae (avisa o numero de frames)

    ML_OK((byte)9),     // NaveMae para Rover (confirma que recebeu num frames)

    ML_REPORT((byte)10), // Rover para NaveMae (um frame)

    ML_END((byte)11),   // Rover para NaveMae (ja enviei todos os frames)

    ML_MISS((byte)12),  // NaveMae para Rover (faltam frames)

    ML_FIN((byte)13),   // NaveMae para Rover (recebi tudo)

    ML_FINACK((byte)14);// Rover para NaveMae (ok final)

    private final byte codigo;

    TipoMensagem(byte codigo){
        this.codigo = codigo;
    }

    public byte getCodigo(){
        return this.codigo;
    }

    public static TipoMensagem descodifica (byte codigo){
        for(TipoMensagem t : values()){
            if(t.codigo == codigo)
                return t;
        }
        throw new IllegalArgumentException("[ERRO] Codigo inválido: " + codigo);
    }
}
