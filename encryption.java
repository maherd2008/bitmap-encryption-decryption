import java.util.Scanner;

class encryption {
    public static void main(String[] args) {
        Scanner question = new Scanner(System.in);

        System.out.print("What is your key bitmap file name? ");
        String originalImageName = question.nextLine();

        System.out.print("What is your plaintext file name? ");
        String messageFileName = question.nextLine();

        System.out.print("What is your encrypted bitmap name? ");
        String encryptedFileName = question.nextLine();

        System.out.print("Would you like to (e)ncrypt or (d)ecrypt? ");
        String answer = question.nextLine();

        while (!(answer.equals("e") || answer.equals("d"))) {
            System.out.print("Let's try this again... \nWould you like to (e)ncrypt or (d)ecrypt? ");
            answer = question.nextLine();
        }

        if (answer.equals("e")) {
            new encryptor(originalImageName, messageFileName, encryptedFileName);
        } else {
            new decryptor(originalImageName, messageFileName, encryptedFileName);
        }
    }
}