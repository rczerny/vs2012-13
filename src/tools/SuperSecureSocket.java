package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;

public class SuperSecureSocket
{
	private Socket s;
	private Key serverPubPrivKey;
	private String clientsKeyDir = "";
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private SecretKey secretKey = null;
	private IvParameterSpec iv = null;

	public SuperSecureSocket(Socket s, Key serverPubPrivKey, String clientsKeyDir) {
		this.s = s;
		try {
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		} catch (IOException e) {
			System.err.println("Error: Couldn't open I/O streams!");
		}
		this.serverPubPrivKey = serverPubPrivKey;
		this.clientsKeyDir = clientsKeyDir;
	}

	public String login(String message1) throws IOException, Exception {
		String result = "";
		message1 = message1.trim();
		String[] commandParts = message1.split("\\s+");
		String username = commandParts[1];
		byte[] clientChallenge = generateSecureRandomNumber(32);
		byte[] base64Challenge = Base64.encode(clientChallenge);
		System.out.println(base64Challenge.length);
		System.out.println(new String(base64Challenge));
		System.out.println(clientChallenge);
		message1 += " " + new String(Base64.encode(encrypt(base64Challenge, "RSA/NONE/OAEPWithSHA256AndMGF1Padding", serverPubPrivKey)));
		String message2S = sendAndReceive(message1);
		byte[] message2 = message2S.getBytes();
		if (message2S == null || !message2S.trim().startsWith("!ok"))
			throw new Exception("Error while sending 1st message!");
		message2S = message2S.trim();
		String[] commandParts2 = message2S.split("\\s+");
		System.out.println("message2 received: " + message2S);
		PrivateKey pK = getPEMPrivateKey(clientsKeyDir + username + ".pem");
		if (pK == null) {
			sendLine("ERROR");
			throw new Exception("Error getting the private key");
		}
		message2 = decrypt(Base64.decode(commandParts2[1]), "RSA/NONE/OAEPWithSHA256AndMGF1Padding", pK);
		final String B64 = "a-zA-Z0-9/+";
		assert ("!ok " + new String(message2)).matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==") : "2nd message";
		commandParts2 = new String("!ok " + new String(message2)).trim().split("\\s+");
		byte[] serverChallenge = Base64.decode(commandParts2[2]);
		SecretKey secretKey = new SecretKeySpec(Base64.decode(commandParts2[3]), "AES");
		this.secretKey = secretKey;
		IvParameterSpec iv = new IvParameterSpec(Base64.decode(commandParts2[4]));
		System.out.println("iv-length: " + Base64.decode(commandParts2[4]).length);
		System.out.println("aes-key-length: " + Base64.decode(commandParts2[3]).length);
		System.out.println("server-challenge-length: " + Base64.decode(commandParts2[2]).length);
		System.out.println("client-challenge-length: " + Base64.decode(commandParts2[1]).length);
		this.iv = iv;
		if (commandParts2[0].equals("!ok")) {
			System.out.println("Got !ok message!");
			if (new String(Base64.decode(commandParts2[1].getBytes())).equals(new String(clientChallenge))) {
				System.out.println("Client Challenges match!");
				result = new String(sendAndReceive(new String(serverChallenge)));
				if (result == null || result.equals(""))
					throw new Exception("Error while sending 3rd message!");
			}
		}
		return result;
	}

	public String sendAndReceive(String message) throws IOException {
		String answer = "";
		String result = "";
		byte[] messageB = message.getBytes();
		try {
			if (secretKey != null && iv != null) {
				messageB = encrypt(Base64.encode(messageB), "AES/CTR/NoPadding", secretKey, iv);
			}
			byte[] base64Message = Base64.encode(messageB);
			bw.write(new String(base64Message));
			bw.newLine();
			bw.flush();
			//while (!(answer = br.readLine()).equals("ready")) {
			answer = br.readLine();
			if (answer == null)
				throw new IOException();
			byte[] answerB = Base64.decode(answer);
			if (secretKey != null && iv != null) {
				answerB = Base64.decode(decrypt(answerB, "AES/CTR/NoPadding", secretKey, iv));
			}
			result += new String(answerB);
			System.out.println("Result added!");
			//}
		} catch (IOException e) {
			System.err.println("Error while communicating with the server!");
			e.printStackTrace();
		}
		System.out.println("Result returned: " + result);
		return result;
	}

	public String readLine() throws IOException{
		String answer = br.readLine();
		if (answer == null)
			throw new IOException();
		byte[] answerB = Base64.decode(answer.getBytes());
		if (new String(answerB).startsWith("ERROR")) {
			return "";
		}
		if (secretKey != null && iv != null) {
			answerB = Base64.decode(decrypt(answerB, "AES/CTR/NoPadding", secretKey, iv));
		}
		System.out.println("readLine(): " + new String(answerB));
		return new String(answerB);
	}

	public void sendLine(String message) throws IOException {
		System.out.println("sendLine(): " + new String(message));
		byte[] messageB = Base64.encode(message.getBytes());
		if (secretKey != null && iv != null) {
			messageB = Base64.encode(encrypt(messageB, "AES/CTR/NoPadding", secretKey, iv));
		}
		bw.write(new String(messageB));
		bw.newLine();
		bw.flush();
	}

	public byte[] encrypt(byte[] input, String algorithm, Key key) {
		byte[] output = null;
		Cipher crypt;
		try {
			crypt = Cipher.getInstance(algorithm);
			crypt.init(Cipher.ENCRYPT_MODE, key);
			output = crypt.doFinal(input);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ERROR: Algorithm unknown!");
		} catch (NoSuchPaddingException e) {
			System.err.println("ERROR: No padding!");
		} catch (IllegalBlockSizeException e) {
			System.err.println("ERROR: Illegal block size!");
		} catch (BadPaddingException e) {
			System.err.println("ERROR: Bad padding!");
		} catch (InvalidKeyException e) {
			System.err.println("ERROR: Invalid key!");
		}
		return output;
	}

	public byte[] decrypt(byte[] input, String algorithm, Key key) {
		byte[] output = null;
		Cipher crypt;
		try {
			crypt = Cipher.getInstance(algorithm);
			crypt.init(Cipher.DECRYPT_MODE, key);
			output = crypt.doFinal(input);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ERROR: Algorithm unknown!");
		} catch (NoSuchPaddingException e) {
			System.err.println("ERROR: No padding!");
		} catch (IllegalBlockSizeException e) {
			System.err.println("ERROR: Illegal block size!");
		} catch (BadPaddingException e) {
			System.err.println("ERROR: Bad padding!");
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			System.err.println("ERROR: Invalid key!");
		}
		return output;
	}

	public byte[] encrypt(byte[] input, String algorithm, Key key, IvParameterSpec iv) {
		byte[] output = null;
		Cipher crypt;
		try {
			crypt = Cipher.getInstance(algorithm);
			crypt.init(Cipher.ENCRYPT_MODE, key, iv);
			output = crypt.doFinal(input);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ERROR: Algorithm unknown!");
		} catch (NoSuchPaddingException e) {
			System.err.println("ERROR: No padding!");
		} catch (IllegalBlockSizeException e) {
			System.err.println("ERROR: Illegal block size!");
		} catch (BadPaddingException e) {
			System.err.println("ERROR: Bad padding!");
		} catch (InvalidKeyException e) {
			System.err.println("ERROR: Invalid key!");
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("ERROR: Invalid initialization vector!");
			e.printStackTrace();
		}
		return output;
	}

	public byte[] decrypt(byte[] input, String algorithm, Key key, IvParameterSpec iv) {
		byte[] output = null;
		Cipher crypt;
		try {
			crypt = Cipher.getInstance(algorithm);
			crypt.init(Cipher.DECRYPT_MODE, key, iv);
			output = crypt.doFinal(input);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("ERROR: Algorithm unknown!");
		} catch (NoSuchPaddingException e) {
			System.err.println("ERROR: No padding!");
		} catch (IllegalBlockSizeException e) {
			System.err.println("ERROR: Illegal block size!");
		} catch (BadPaddingException e) {
			System.err.println("ERROR: Bad padding!");
		} catch (InvalidKeyException e) {
			System.err.println("ERROR: Invalid key!");
		} catch (InvalidAlgorithmParameterException e) {
			System.err.println("ERROR: Invalid initialization vector!");
			e.printStackTrace();
		}
		return output;
	}

	public byte[] generateSecureRandomNumber(int size) {
		SecureRandom secureRandom = new SecureRandom(); 
		final byte[] number = new byte[size];
		secureRandom.nextBytes(number);
		return number;
	}

	public PrivateKey getPEMPrivateKey(String pathToPrivateKey) {
		PEMReader in;
		PrivateKey privateKey = null;
		try {
			in = new PEMReader(new FileReader(pathToPrivateKey), new PasswordFinder() {
				@Override
				public char[] getPassword() {
					char[] privK = null;
					System.out.println("Enter pass phrase:");
					try {
						privK = new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray() ;
					} catch (IOException e) {
						System.err.println("Couldn't read password!");
					}
					return privK;
				}
			});
			KeyPair keyPair = (KeyPair) in.readObject(); 
			privateKey = keyPair.getPrivate();
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find private key!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Couldn't read Private Key!");
			e.printStackTrace();
		}
		return privateKey;
	}

	public PublicKey getPEMPublicKey(String pathToPublicKey) {
		PEMReader in;
		PublicKey publicKey = null;
		try {
			in = new PEMReader(new FileReader(pathToPublicKey));
			publicKey = (PublicKey) in.readObject();
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find public key!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Couldn't read public key!");
			e.printStackTrace();
		}
		return publicKey;
	}

	public SecretKey generateAESKey(int keySize) {
		KeyGenerator generator = null;
		try {
			generator = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Algorithm is unknown!");
			e.printStackTrace();
		} 
		generator.init(keySize); 
		SecretKey key = generator.generateKey();
		return key;
	}

	public InputStream getInputStream() {
		InputStream is = null;
		try {
			is = s.getInputStream();
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get input stream!");
		}
		return is;
	}

	public OutputStream getOutputStream() {
		OutputStream os = null;
		try {
			os = s.getOutputStream();
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't get input stream!");
		}
		return os;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

	public IvParameterSpec getIv() {
		return iv;
	}

	public void setIv(IvParameterSpec iv) {
		this.iv = iv;
	}
}
