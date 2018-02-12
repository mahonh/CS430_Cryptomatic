import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Cryptomatic - A 16bit XOR encryption, decryption, and brute force program.
 * @author Chad Mahon
 * @version 1.0
 */
public class Cryptomatic
{
    public static void main(String args[])
    {
        boolean running = true;
        Scanner scany = new Scanner(System.in);

        //Main loop which allows the user to make choices
        while (running)
        {
            System.out.println("\nPlease select an option: ");
            System.out.println("1. Encrypt File");
            System.out.println("2. Decrypt File");
            System.out.println("3. Brute Force File");
            System.out.println("4. Exit");

            String choice = scany.nextLine();
            String fileName, userKey;

            switch (choice)
            {
                case "1":
                    System.out.println("Enter a filename: ");
                    fileName = scany.nextLine();
                    System.out.println("Enter a key, two characters in length: ");
                    userKey = scany.nextLine();
                    encryptDecrypt(fileName, userKey, 1);
                    break;
                case "2":
                    System.out.println("Enter a filename: ");
                    fileName = scany.nextLine();
                    System.out.println("Enter the key: ");
                    userKey = scany.nextLine();
                    encryptDecrypt(fileName, userKey, 2);
                    break;
                case "3":
                    System.out.println("Enter a filename: ");
                    fileName = scany.nextLine();
                    bruteForce(fileName);
                    break;
                case "4":
                    System.out.println("Later Gator!");
                    running = false;
                    break;
                default:
                    System.out.println("Not a valid choice, try again");
                    break;
            }
        }
    }

    /**
     * Used to perform XOR operation to encrypt or decrypt a file.
     * @param fileName - User specified filename of encrypted or plain text file
     * @param userKey - User specified two character key
     * @param choice - Choice of 1 is used for encryption and choice of 2 for decryption
     */
    private static void encryptDecrypt(String fileName, String userKey, int choice)
    {
        byte[] fileBytes = readTextFile(fileName);
        byte[] key = new byte[fileBytes.length];

        //Used to remove any extra characters the user may have entered, only the first two are used
        byte[] keyTemp = userKey.getBytes();
        byte keyA = keyTemp[0];
        byte keyB = keyTemp[1];

        //Duplicates the two characters of the key for the length of the original file
        for (int i = 0; i < key.length; i++)
        {
            if (i % 2 == 0)
                key[i] = keyA;

            else
                key[i] = keyB;
        }

        BigInteger fileData, encryptDecryptKey, output;

        //Converts the byte[] to BigIntegers
        fileData = new BigInteger(fileBytes);
        encryptDecryptKey = new BigInteger(key);

        //XOR Operation
        output = fileData.xor(encryptDecryptKey);

        byte[] writeData = output.toByteArray();

        //Choice of 1 is used to encrypt and a choice of 2 is used to decrypt
        if (choice == 1)
            saveBinaryFile(writeData, "ENCRYPT.txt");

        else
            {
                saveBinaryFile(writeData, "DECRYPT.txt");
                System.out.println(new String(writeData));
            }
        System.out.println("\nDone!");
    }

    /**
     * Brute Force method to decrypt an encrypted file without knowing the key
     * All combinations of a 2 Character key are tested and the key with the most
     * matches in a dictionary is chosen
     * @param fileName - User specified file name
     */
    private static void bruteForce(String fileName)
    {
        System.out.println("Preparing...");

        long startTime, endTime;

        String possibleKey = null;
        int maxWordCount = 0;

        double fileSampleSize;

        byte[] keySetA, keySetB;

        byte[] tempKey = new byte[2];

        BigInteger data, key, output;


        //Reads a dictionary from file and creates a HashMap containing the words
        HashMap<String, String> dictionaryOfWords = new HashMap<String, String>();
        File dictionaryFile = new File("resources/Dictionary.txt");
        Scanner wordy = null;

        try {
            wordy = new Scanner(dictionaryFile);
            while (wordy.hasNextLine())
            {
                dictionaryOfWords.put(wordy.nextLine(), wordy.nextLine());
            }
            wordy.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] fileData = readBinaryFile(fileName); //File data read in its entirety
        startTime = System.currentTimeMillis();

        
        //Determines how much of the file to sample. If the file is larger than 10,000 bytes
        //then only the first 10,000 are used. Otherwise the entire file is used
        if (fileData.length > 10000)
            fileSampleSize = 10000;
        else
            fileSampleSize = fileData.length;

        if (fileSampleSize % 2 != 0)
            fileSampleSize++;

        byte[] keyDuplication = new byte[(int) fileSampleSize];
        byte[] fileSampleData = new byte[(int) fileSampleSize];

        //Sample data to check
        for (int x = 0; x < fileSampleData.length; x++)
            fileSampleData[x] = fileData[x];

        data = new BigInteger(fileSampleData);


        //Creates keys from a file. Each array contains the same characters, combined they
        //form all of the combinations
        keySetA = readBinaryFile("resources/Keys.txt");
        keySetB = keySetA;


        //Starting point of brute force
        System.out.println("Checking...");

        for (int x = 0; x <= keySetA.length - 1; x++)
        {
            for (int y = 0; y <= keySetB.length - 1; y++)
            {
                tempKey[0] = keySetA[x];
                tempKey[1] = keySetB[y];

                //Duplicates the two characters of the key for the length of the original file
                for (int z = 0; z < keyDuplication.length; z++)
                {
                    if (z % 2 == 0)
                        keyDuplication[z] = tempKey[0];

                    else
                        keyDuplication[z] = tempKey[1];
                }

                key = new BigInteger(keyDuplication);

                //XOR Operation
                output = data.xor(key);

                String result = new String(output.toByteArray());


                //Creates words from the newly decrypted data
                ArrayList<String> wordsToCheck = new ArrayList<>();
                Scanner createWords = new Scanner(result);

                while (createWords.hasNext())
                {
                    wordsToCheck.add(createWords.next());
                }


                //Examines each word to determine if it actually is a word by checking the dictionary
                //If the "word" is in the dictionary, the wordCount is updated
                int wordCount = 0;

                for (int q = 0; q < wordsToCheck.size(); q++)
                {
                    String check = wordsToCheck.get(q).toLowerCase();

                    if (dictionaryOfWords.containsKey(check))
                        wordCount++;
                }

                //Determines if the current key has the greatest wordCount. If it does, then
                //it is set as the possible Key.
                if (wordCount > maxWordCount)
                {
                    maxWordCount = wordCount;
                    possibleKey = "" + (char)tempKey[0] + (char)tempKey[1];
                }
            }
        }

        endTime = System.currentTimeMillis();

        encryptDecrypt(fileName, possibleKey, 2);

        System.out.println("\nComplete!\n");
        System.out.println("Total Time: " + (endTime - startTime) + " Milliseconds");
        System.out.println("Looks like your key is: " + "'" + possibleKey + "'");
    }

    /**
     * Reads a user specified text file. Converts the contents into a byte[] and adds an additional byte if the
     * overall contents are odd.
     * @param file - User specified text file
     * @return byte[] of file contents
     */
    private static byte[] readTextFile(String file)
    {
        byte[] fileData = null;

        Path location = Paths.get(file);
        try {
            fileData = Files.readAllBytes(location);

            //If the file contains an odd number of bytes, then an extra space is added to the end of the byte[]
            if (fileData.length % 2 != 0)
            {
                byte[] temp = new byte[fileData.length + 1];

                for (int i = 0; i < fileData.length; i++)
                    temp[i] = fileData[i];

                //The last space in the array is an extra space - " "
                temp[temp.length - 1] = 32;

                fileData = temp;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }

    /**
     * Reads the byte contents of a user specified file. Used for decryption.
     * @param file - User specified binary file
     * @return byte[] of file contents
     */
    private static byte[] readBinaryFile(String file)
    {
        byte[] fileBytes = null;
        try {
            Path location = Paths.get(file);
            fileBytes = Files.readAllBytes(location);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    /**
     * Saves a byte[] to a binary file. Used for encryption
     * @param data - byte[] of data to write to file
     * @param title - title of binary file
     */
    private static void saveBinaryFile(byte[] data, String title)
    {
        try (FileOutputStream outputStream = new FileOutputStream(title))
        {
            outputStream.write(data);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}