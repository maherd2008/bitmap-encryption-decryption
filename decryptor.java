import java.util.Random;
import java.io.*;

public class decryptor {
    private int[] encBody; //define the encoded image body array
    private int[] keyBody; //define the key image body arra
    private int encSize; //define the encoded image size
    private int keySize; //define the key image size

    private byte[] byteMsg; //define the byte array that will be written to the output file 

    public decryptor(String originalImageName, String messageFileName, String encryptedFileName) { //create the constructor
        try {
            FileInputStream enc = new FileInputStream(encryptedFileName); //open the input stream for the encoded image
            FileInputStream key = new FileInputStream(originalImageName); //open the input stream for the key image

            byte[] temp = new byte[54]; //create a temp byte array of size 54 to read the header
            enc.read(temp); //read the first 54 bytes of the bitmap (the header) and add it to temp
            encSize = (int)temp[2]+(int)temp[3]*256+(int)temp[4]*256*256+(int)temp[5]*256*256*256-54; //get the encoded image size

            key.read(temp); //read the header from the key image
            keySize = (int)temp[2]+(int)temp[3]*256+(int)temp[4]*256*256+(int)temp[5]*256*256*256-54; //get the key image size

            byte[] encBytes = new byte[encSize]; //now that we have the size, define a byte array with the size found from the header
            enc.read(encBytes); //read the body
            encBody = encryptor.convertToInt(encBytes, encSize); //using the encryptor's convert to int method to convert the body to an int array

            byte[] keyBytes = new byte[keySize]; //same idea as lines 24-26
            key.read(keyBytes);
            keyBody = encryptor.convertToInt(keyBytes, keySize); //convert the body to an int array

            enc.close(); //to avoid data leaking close both input file streams
            key.close();

            encryptor.randomize(keyBody, keySize); //this is the same randomize function as in the encryptor with the same seed

            decrypt(messageFileName); //calls the decrypt function

        } catch (IOException e) { //catch the file error if it exists and send it to the stack trace
            e.printStackTrace();
        }
    }

    public void decrypt(String outFile) {
        try { //try/catch this so it doesn't explode if something goes wrong
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

            FileOutputStream out = new FileOutputStream(outFile);
            out.write(byteMsg);
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
