package encryption;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;



/**
 * Utility class for secure password handling using PBKDF2WithHmacSHA256.
 *
 * Responsibilities:
 * - Generate a random salt for each user.
 * - Hash passwords with PBKDF2 (password + salt + iterations).
 * - Validate a password against a stored hash and salt.
 * - Convert salt to/from String so it can be stored in a database.
 *
 * This avoids storing plain-text passwords and makes brute-force attacks harder.
 */

public class Encryption {

    /**
     * Algorithm used for the key derivation / hashing.
     * PBKDF2WithHmacSHA256 is a standard and secure choice.
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Number of iterations for PBKDF2.
     * More iterations = slower for attacker (and also a bit slower for us).
     */
    private static final int ITERATIONS = 70000;

    /**
     * Length of the derived key (hash) in bits.
     * 256 bits is a common size for secure hashes.
     */
    private static final int KEY_LENGTH = 256;

    /**
     * Length of the salt in bytes.
     * 16 bytes = 128 bits of randomness, enough for most use cases.
     */
    private static final int SALT_LENGTH = 16;


    /**
     * Generates a new random salt.
     *
     * @return a random byte[] of length SALT_LENGTH
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom(); // Cryptographically secure RNG
        random.nextBytes(salt);  // Fill array with random bytes
        return salt;
    }


    /**
     * Hashes a password using PBKDF2 with the provided salt.
     *
     * Steps:
     * 1. Build a PBEKeySpec with:
     *    - password chars
     *    - salt
     *    - iterations (work factor)
     *    - desired key length
     * 2. Use SecretKeyFactory to derive a key (the hash).
     * 3. Encode the hash as Base64 String to store in DB.
     *
     * @param password plain-text password entered by the user
     * @param salt     user-specific salt stored in DB
     * @return Base64 String representation of the hash
     */
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


    /**
     * Validates a password against a stored hash and salt.
     *
     * Steps:
     * 1. Re-hash the provided plain password using the stored salt.
     * 2. Compare the new hash with the stored hash.
     *
     * If they match → password is correct.
     *
     * @param password   plain-text password entered during login
     * @param storedHash Base64 hash stored in the database
     * @param storedSalt salt (byte[]) stored in the database for that user
     * @return true if password is correct, false otherwise
     */
    public static boolean validatePassword(String password, String storedHash, byte[] storedSalt) {
        String newHash = hashPassword(password, storedSalt);
        return newHash.equals(storedHash);
    }


    /**
     * Converts a salt (byte[]) to a Base64 String to store in DB.
     *
     * @param salt salt as byte array
     * @return Base64 String representation of the salt
     */
    public static String saltToString(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }


    /**
     * Converts a Base64 salt String back to byte[].
     *
     * @param saltString Base64 String stored in DB
     * @return salt as byte[]
     */
    public static byte[] stringToSalt(String saltString) {
        return Base64.getDecoder().decode(saltString);
    }

}
