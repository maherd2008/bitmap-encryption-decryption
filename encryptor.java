import java.util.Random;
import java.io.*;
import java.util.Scanner;

public class encryptor {
	private FileInputStream img; //input DataStream for the original bitmap
	private FileInputStream msg; //input DataStream for the message file
	private FileOutputStream enc; //output data stream for the encrypted bitmap image

	private int[] header; //Store numerical byte values of the header from the bitmap
	private int[] body; //Bitmap pixel data in BGR format(little endian)
	private int[] message; //Message data stored byte by byte
	private int bmpsize; //Size of the body array
	private int msgsize; //Size of the message file
   
	public encryptor(String imageFileName, String messageFileName, String encryptedFileName) { //Constructor for the encryptor
		try {
			img = new FileInputStream(imageFileName);//Open an Input Stream for the bitmap file
			header = new int[54]; //create a new int array and assign it to header
           	byte[] temp = new byte[54];//create a new byte array anbd assign it to temp. This will be used to read data off of the input stream 
				
			img.read(temp,0,54);//read 54 bytes from the input stream bitmap and store in the temp byte array
			header=convertToInt(temp,54);//Call convertToInt method, have it covert all of the signed byte values into unsigned integer values and store result in header 
			bmpsize=(int)header[2]+(int)header[3]*256+(int)header[4]*256*256+(int)header[5]*256*256*256-54;//store the bitmap pixel array size from the header positions 2-5(little endian)

           	temp = new byte[bmpsize]; //reset the tmep byte array to the bitmap size
			img.read(temp,0,bmpsize);//read all pixel data from bitmap file stream
			body=convertToInt(temp,bmpsize);//Convert signed bytes to unsigned ints and store in body array
			img.close();//close bitmap image datastream

			msgsize=(int)(new File(messageFileName).length());//get message file size from the length method in the File class and store it in msgsize
           	temp = new byte[msgsize]; //reset temp byte array to the size of the message file.
			msg = new FileInputStream(messageFileName);//make a new FileInputStream for the message file.
			msg.read(temp,0,msgsize);//read all data from the message file into the temp array
			message=convertToInt(temp,msgsize);//Convert all signed bytes in the temp array to unsigned ints and store in message in array

			randomize(body,bmpsize);//Call randomize method which will edit every subpixel value in the body array by a random value betwen -20 and 19	

			encrypt();//Overlay the binarized bytes from the message file onto sequential bytes from the body.
								
			enc=new FileOutputStream(encryptedFileName);//create a new outputFileStream and assign it to enc
			
			for(int i=0;i<54;i++) {
				enc.write(header[i]);//write byte by byte the header of the image file to output				
			}

			for(int i=0;i<bmpsize;i++) {
				enc.write(body[i]);//Write byte by byte the body(modified) of the image file to output
			}
		}
		
		catch(IOException e){} 
	}

	public int[] convertToInt(byte[] in, int len) {
		//in is already populated and sized 		
		int[] out = new int[len];//Create ne int array and store it in out. This will be returned as a new int array when done
		for (int i=0;i<len;i++) {
			out[i]=in[i] & 0xFF;//converting a signed byte to a unsigned signed integer
		}
		return out;//Return out		
	}	

	public void randomize(int[] in, int len) {
        long seed = (in[34]*7-4)+(in[9]*952+12);
        Random random = new Random(seed);
		int randnum = 0;
        for(int i = 0; i < len; i++) {
			randnum = random.nextInt(40) - 20;
			if(in[i] + randnum > 255) {
                in[i] = 255; 
            } else if (in[i] + randnum < 0) {
                in[i] = 0; 
            } else {
                in[i] = in[i] - randnum;
            } 
		}
		return;
	}
		
	public void encrypt() {
    	int bpos = 0; // current pixel byte
    	int cbyte = 0; // current byte from message
    	int pow = 1; // power of 2
    	int digit = 0; // current bit

    	// Encrypt the message
    	for (int i = 0; i < msgsize; i++) {
        	cbyte = message[i];
        	pow = 1;

        	for (int j = 1; j < 9; j++) {
            	digit = (cbyte % (pow * 2)) / pow;
            	cbyte = cbyte - cbyte % (pow * 2);
            	pow = pow * 2;

            	body[bpos] = (body[bpos] & 0xFE) | digit;
            	bpos++;
        	}
    	}

    	// Add the stop flag byte (11111111)
    	cbyte = 0xFF; // flag byte
    	pow = 1;

    	for (int j = 1; j < 9; j++) {
        	digit = (cbyte % (pow * 2)) / pow;
        	cbyte = cbyte - cbyte % (pow * 2);
        	pow = pow * 2;

        	body[bpos] = (body[bpos] & 0xFE) | digit;
        	bpos++;
    	}
	}
}