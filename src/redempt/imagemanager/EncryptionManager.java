package redempt.imagemanager;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionManager {
	
	public static byte[] encrypt(byte[] input, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			SecretKey k = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.ENCRYPT_MODE, k);
			byte[] output = cipher.doFinal(input);
			return output;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		}
		return null;
	}
	
	public static byte[] decrypt(byte[] input, byte[] key) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			SecretKey k = new SecretKeySpec(key, "AES");
			cipher.init(Cipher.DECRYPT_MODE, k);
			byte[] output = cipher.doFinal(input);
			return output;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
		}
		return null;
	}
	
	public static byte[] hash(String input) {
		byte[] bytes = input.getBytes();
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			return Arrays.copyOf(sha.digest(bytes), 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
