package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

  private SecretKey secretKey;
  private Cipher cipher;

  public Client(byte[] decryptionKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException {

    secretKey = new SecretKeySpec(decryptionKey, "Blowfish");
    cipher = Cipher.getInstance("Blowfish");
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
  }

  public void decrypt(InputStream inStream, OutputStream outStream) throws IOException,
    IllegalBlockSizeException, BadPaddingException {
    byte[] buffer = new byte[1024];
    int len;
    while ((len = inStream.read(buffer)) > 0) {
      outStream.write(cipher.update(buffer, 0, len));
      outStream.flush();
    }
    outStream.write(cipher.doFinal());
  }

  public static void main(String[] args) {
    try {
      // read the decryption key from user
      Scanner scanner = new Scanner(System.in);
      System.out.println("Enter the decryption key");
      System.out.println();
      String password = scanner.nextLine();
      scanner.close();
      Client client = new Client(password.getBytes());

      // connect to the server and receive data
      String serverName = "localhost";
      int port = 8080;
      System.out.println("Connecting to " + serverName + " on port " + port);
      Socket clientSocket = new Socket(serverName, port);
      System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());
      InputStream inFromServer = clientSocket.getInputStream();

      // save the data from server input stream into a file output stream after
      // decryption
      File decryptedFile =
        new File("D:\\Workspace\\workspaces\\java\\encrypted_communication\\out.jpg");
      OutputStream outStream = new FileOutputStream(decryptedFile);
      client.decrypt(inFromServer, outStream);
      inFromServer.close();
      outStream.close();
      clientSocket.close();

      System.out.println("File written at: " + decryptedFile.getAbsolutePath());
      System.out.println("Exiting client");
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (IllegalBlockSizeException | BadPaddingException e) {
      System.err.println("Could not decrypt. Please check your password");
      e.printStackTrace();
    }
    catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
      e.printStackTrace();
    }
  }

}
