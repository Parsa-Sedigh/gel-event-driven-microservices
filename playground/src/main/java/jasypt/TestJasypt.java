package jasypt;


import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class TestJasypt {
    public static void main(String[] args) {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();

        /* jasypt will use this password as a key when encrypting our secrets. That means we need to pass this password to our
        app at runtime to be able to DECRYPT the secrets. */
        standardPBEStringEncryptor.setPassword("Demo");
        standardPBEStringEncryptor.setAlgorithm("PBEWithHmacSHA512AndAES_256");
        standardPBEStringEncryptor.setIvGenerator(new RandomIvGenerator());

        var result = standardPBEStringEncryptor.encrypt("123");

        System.out.println(result);
        System.out.println(standardPBEStringEncryptor.decrypt(result));
    }
}
