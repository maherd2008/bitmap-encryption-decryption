import java.util.Random;
import java.io.*;

public class decryptor {
    private int[] encBody;
    private int[] keyBody;
    private int encSize;
    private int keySize;

    private byte[] byteMsg;

    public decryptor(String originalImageName, String messageFileName, String encryptedFileName) {
        try {
            FileInputStream enc = new FileInputStream(encryptedFileName);
            FileInputStream key = new FileInputStream(originalImageName);

            byte[] temp = new byte[54];
            enc.read(temp);
            encSize = readSize(temp);

            key.read(temp);
            keySize = readSize(temp);

            byte[] encBytes = new byte[encSize];
            enc.read(encBytes);
            encBody = convertToInt(encBytes, encSize);

            byte[] keyBytes = new byte[keySize];
            key.read(keyBytes);
            keyBody = convertToInt(keyBytes, keySize);

            enc.close();
            key.close();

            // Reverse the randomization
            derandomize(keyBody, keySize);

            decrypt(messageFileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int readSize(byte[] header) {
        return (header[2] & 0xFF)
                + ((header[3] & 0xFF) << 8)
                + ((header[4] & 0xFF) << 16)
                + ((header[5] & 0xFF) << 24)
                - 54;
    }

    public int[] convertToInt(byte[] in, int len) {
        int[] out = new int[len];
        for (int i = 0; i < len; i++)
            out[i] = in[i] & 0xFF;
        return out;
    }

    // Reverse of encryptor.randomize()
    public void derandomize(int[] in, int len) {
        long seed = (in[34] * 7 - 4) + (in[9] * 952 + 12);
        Random random = new Random(seed);

        for (int i = 0; i < len; i++) {
            int randnum = random.nextInt(40) - 20;

            int val = in[i] + randnum;
            if (val < 0) val = 0;
            if (val > 255) val = 255;

            in[i] = val;
        }
    }

    public void decrypt(String outputFile) throws IOException {
        ByteArrayOutputStream msgStream = new ByteArrayOutputStream();

        int bpos = 0;
        while (bpos + 7 < encBody.length) {
            int cbyte = 0;
            int pow = 1;

            for (int j = 0; j < 8; j++) {
                int bit = encBody[bpos] & 1;
                cbyte += bit * pow;
                pow <<= 1;
                bpos++;
            }

            if (cbyte == 0xFF) {
                break;
            }

            msgStream.write(cbyte);
        }

        byteMsg = msgStream.toByteArray();

        FileOutputStream out = new FileOutputStream(outputFile);
        out.write(byteMsg);
        out.close();
    }
}