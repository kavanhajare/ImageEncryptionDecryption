package main;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Server extends Thread {

  private ServerSocket serverSocket;
  private SecretKey secretKey;
  private Cipher cipher;

  public Server(int port) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeyException {

    serverSocket = new ServerSocket(port);
    secretKey = new SecretKeySpec("password".getBytes(), "Blowfish");
    cipher = Cipher.getInstance("Blowfish");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);

  }

  public void encrypt(InputStream inStream, OutputStream outStream) throws IOException,
    IllegalBlockSizeException, BadPaddingException {

    byte[] buffer = new byte[1024];
    int len;
    while ((len = inStream.read(buffer)) > 0) {
      outStream.write(cipher.update(buffer, 0, len));
      outStream.flush();
    }
    outStream.write(cipher.doFinal());

  }

  @Override
  public void run() {
    while (true) {
      try {

        // wait for client to connect
        System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
        Socket server = serverSocket.accept();
        System.out.println("Just connected to " + server.getRemoteSocketAddress());

        // once client connected, encrypt the image file
        File rawFile = new File("D:\\Workspace\\workspaces\\java\\encrypted_communication\\in.jpg");
        InputStream inStream = new FileInputStream(rawFile);
        OutputStream outStream = new DataOutputStream(server.getOutputStream());
        encrypt(inStream, outStream);

        // close all streams once done
        System.out.println("Encrypted file sent to client. Closing connection from " +
                           server.getRemoteSocketAddress());
        inStream.close();
        outStream.close();
        server.close();

        System.out.println("Connection closed");
      }
      catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
        e.printStackTrace();
        break;
      }
    }
  }

  public static void main(String[] args) {
    try {
      Thread t = new Server(8080);
      t.start();
    }
    catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
      e.printStackTrace();
    }
  }
}
