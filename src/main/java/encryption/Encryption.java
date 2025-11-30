package encryption;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class Encryption {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 70000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    public static String hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not found", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key spec", e);
        }
    }

    public static boolean validatePassword(String password, String storedHash, byte[] storedSalt) {
        String newHash = hashPassword(password, storedSalt);
        return newHash.equals(storedHash);
    }

    public static String saltToString(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    public static byte[] stringToSalt(String saltString) {
        return Base64.getDecoder().decode(saltString);
    }
}
