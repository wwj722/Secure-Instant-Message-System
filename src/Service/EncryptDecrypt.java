package Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {
	public static final String KEYS = "/Users/binbinlu/Documents/NetworkSecurity/ps4/";
//	
//	public static final String serverPrivate = "G:/NetworkSecurity/ps4/server_private.key";
//	
//	public static final String clientPublic = "G:/NetworkSecurity/ps4/client_public.key";
//	
//	public static final String clientPrivate = "G:/NetworkSecurity/ps4/client_private.key";
	
	public Cipher rsaCipher;
	
	
	public EncryptDecrypt() {
		try {
			rsaCipher = Cipher.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PublicKey readPublicKey(byte[] encodedKey) {
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public PrivateKey readPrivateKey(byte[] encodedKey) {
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] encryptPublic(byte[] content, String position) {
		PublicKey publicKey = readPublicKey(readFile(position+"Public"));
		try {
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] cipherText = rsaCipher.doFinal(content);
			return cipherText;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decryptPrivate(byte[] content, String position) {
		PrivateKey privateKey = readPrivateKey(readFile(position+"Private"));
		try {
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] cipherText = rsaCipher.doFinal(content);
			return cipherText;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] encrypt (byte[] aesKey, byte[] content, byte[] iv) {
		SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
		try {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
			
			byte[] cipherText = aesCipher.doFinal(content);
			return cipherText;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
//	public static byte[] aesEncrypt(byte[] message, SecretKey secretKey, byte[] iv) {
//		try {
//			byte[] key = secretKey.getEncoded();
//	        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
//
//	        // build the initialization vector spec
//	        IvParameterSpec ivspec = new IvParameterSpec(iv);
//
//	        // initialize the cipher for encrypt mode
//	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//	        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);
//	        
//		    // Encrypt the cleartext
//		    byte[] ciphertext = cipher.doFinal(message);
//		    
//		    return ciphertext;
//		}
//		catch (Exception e) {
//			//System.out.println("Error in AES");
//		}
//		
//		return null;
//	}

	public byte[] encrypt (byte[] aesKey, int num, byte[] iv) {
		SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
		try {
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, ivSpec);
			
			byte[] content = ByteBuffer.allocate(4).putInt(num).array();
			byte[] cipherText = aesCipher.doFinal(content);
			return cipherText;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public byte[] decrypt(byte[] aesKey, byte[] cipheredText, byte[] iv) {
		SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
		try {
			// build the initialization vector spec
	        IvParameterSpec ivspec = new IvParameterSpec(iv);

	        // initialize the cipher for encrypt mode
	        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivspec);
		    
			byte[] originText =  aesCipher.doFinal(cipheredText);
			return originText;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	
	/*
	 * aesDecrypt: Decode the message using shared secret key
	 */
	public static byte[] aesDecrypt(byte[] cipherText, SecretKey secretKey, byte[] iv) {
		try {
			byte[] key = secretKey.getEncoded();
	        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

	        // build the initialization vector spec
	        IvParameterSpec ivspec = new IvParameterSpec(iv);

	        // initialize the cipher for encrypt mode
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
	        
		    // Decrypt the cipherText
		    byte[] cleartext = cipher.doFinal(cipherText);
		    		    
		    return cleartext;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	/* Derive the key, given password and salt. */
//	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//	KeySpec spec = new PBEKeySpec(password, salt, 65536, 256);
//	SecretKey tmp = factory.generateSecret(spec);
//	SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
//	/* Encrypt the message. */
//	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//	cipher.init(Cipher.ENCRYPT_MODE, secret);
//	AlgorithmParameters params = cipher.getParameters();
//	byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
//	byte[] ciphertext = cipher.doFinal("Hello, World!".getBytes("UTF-8"));
//	Now send the ciphertext and the iv to the recipient. The recipient generates a SecretKey in exactly the same way, using the same salt and password. Then initialize the cipher with the key and the initialization vector.
//
//	/* Decrypt the message, given derived key and initialization vector. */
//	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//	cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
//	String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
//	System.out.println(plaintext);

	
	public byte[] readFile(String path) {
		File file = new File(KEYS+path+".der");
		byte[] content = new byte[(int) file.length()];
		try {
			new FileInputStream(file).read(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
}
