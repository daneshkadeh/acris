package sk.seges.corpis.pay;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author ladislav.gazo
 */
public class SignatureHelper {

	public static byte[] sha1(String in) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			sha.update(in.getBytes());
			return sha.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder builder = new StringBuilder(30);
		String str;
		for (int i = 0; i < bytes.length; i++) {
			str = Integer.toHexString(bytes[i] & 0xFF);
			if (str.length() == 1) {
				builder.append("0" + str);
			} else {
				builder.append(str);
			}
		}
		return builder.toString().toUpperCase();
	}

	public static byte[] des(String key, byte[] hash) {
		try {
			DESKeySpec desKeySpec;
			desKeySpec = new DESKeySpec(key.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
			Cipher ecipher = Cipher.getInstance("DES");
			ecipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return ecipher.doFinal(hash);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}