package data;

public enum TipoMensagem {
    ML_SYN((byte)1),
    ML_SYNACK((byte)2),
    ML_REQUEST((byte)3),
    ML_DATA((byte)4),
    ML_CONFIRM((byte)5),
    ML_ACK((byte)6),
    TS_TCP((byte)7),
    TS_REPORT((byte)8),
    TS_CONFIRM((byte)9);

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
        throw new IllegalArgumentException("Codigo inválido: " + codigo);
    }
}
