package interfaces;

import java.io.IOException;

public interface Codificavel {
    byte[] encode() throws IOException;
    void decode(byte[] data) throws IOException;
}
