package data;

public enum TipoMensagem {
    ML_SYN((byte)1),    // Tipo comum no UDP
    ML_SYNACK((byte)2), // Tipo comum no UDP
    ML_REQUEST((byte)3),// Tipo comum no UDP
    ML_DATA((byte)4),   // Tipo comum no UDP
    ML_CONFIRM((byte)5),// Tipo comum no UDP
    TS_TCP((byte)7),    // Tipo comun no TCP
    ML_REPORT((byte)8), // Tipo especial para report na missao
    ML_MISS((byte)9),   // Tipo especial para report na missao
    ML_FIN((byte)10),   // Tipo especial para report na missao
    ML_OK((byte)11);    // Tipo es+ecial para report na missao

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
