package interfaces;

import java.io.IOException;

public interface Codificavel {
    byte[] toByteArray() throws IOException;
    void fromByteArray(byte[] data) throws IOException;
}