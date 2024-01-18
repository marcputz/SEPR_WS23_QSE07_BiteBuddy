package at.ac.tuwien.sepr.groupphase.backend.unittests.service;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class KeyServiceTest implements TestData {

    private static final String PRIVATE_KEY =
        "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCuFMrhD6RgQFvy"
            + "JPdX4iiD+ye0NKnVFqkfMcFT8Z64FO1vgl6LSIK9UhPpIPjKHhwm494AJ+Lj4p4H"
            + "O9xGOZFV3r0HfbH5Z7lNdyxYTLFPRffxfTgoFMr199RGfepD/x6HlcDMgwv14oaI"
            + "USqrpoSYIxDdgen2KrXsLKYsWwnV3SzKVK8GjOV4P4/sfBgu34Ke8jYPlvqbGS5t"
            + "2vI4OPcnaIyWU1r2onV6m+lb7TZzCvVKoogF11MtXTmLkPmJTVnVnAMD+buwBoG5"
            + "NDlF7nQHYttcfqFYGcr/UZZtShYARtG0kXd7f3lfKwVnpKJsmzcPQWZYDNy5vQHN"
            + "5jx3EC1vAgMBAAECggEAOyh+v5ggvMLyXCo60iddrGhsGD3zXyJpy9uNPxkz3ovi"
            + "Gox706qLH4pmkMmvMoSj/YVfer8TSG1JVdz+YPEMVlmw9Hw/QKoCm3Q94i4B7ZYy"
            + "CeOLc3eJd6hGf8Qnmf73YY0/LWD1bhSSsn2e2iFnGwqDnzsYhpsCIuYTfKxdfcsx"
            + "M04YPrxvHmmQ7bJrF7jjoM9/aLZAEF0NJ2IhpYMzwuZfhvKIYxvZVugeOhB7qHYg"
            + "V5kw+UxJvTlU7H6tR45jmMS6tqIKOReXynukHMJE84vY0GMXxsc7Oc/B0+8+ArxP"
            + "1RfUKp8cPxH16cal8JQz/3pPCNicLbp5Eknh0u61hQKBgQDq0uUL+Go05FmX5+qw"
            + "AF6gq5YRJoTSbxlrCERcVSwQGEvaTFloeBB9XTP7JWSmlc7q5AHEUg78OagKf0Kt"
            + "XlNquz5lz9OBmCTz7LEcA/aR8pAoMFD+DDsjUYM5yoO2Bio0O3recP6JOQjDUctd"
            + "75Y5R0CP5u/xzzS2JCnVtTtoawKBgQC9x5qhrv5OOJEJpzShpZewf7q90crvCvfg"
            + "uc+cAwelfMeZAMu9wA61TkrpPVMEmmQD2iivrFe109mS3k81S/+DSFzrOuNQM5LO"
            + "bQct643Nuge8t1CM8l+WwLWCVev61kkRwiDXmbjaykL10Qk+PoWu2KwrQJfMpyfU"
            + "hnFM+5ugDQKBgFPyowmdfDJ+c/0XXtIev7WGwfZNo+wq5ZZa9T1hAYibdvqKkugD"
            + "DDoVCNE+8/WN1tQoQO1zPII1BnsCNc8Oypl8aPkyy/UndAZz1nkifZJ9ecW0SDAa"
            + "JUYiDNyrJlz7ZTkZDnrZQFXEOvrAkgh572V4sH8BW0RCBCEdWotY5+0TAoGAeCjk"
            + "oFCjVbV9J6aFrX+iXq9Q9pTZr6a7Mb+UUsycT8yEuDyHw9nRtjwfNDSO1tBGWSYN"
            + "MsWlN0wua050ymMXIfF0W8AKYyJ0Zl0j+ZA2Vbbe5T8QMl3X5iSYCDEM2+JHm4XV"
            + "s4zyPR1pbijveEiv7ffkcvJP6tU/Y9HH9R0t+RUCgYEA5PCiUeLH+KodIZfFXf5B"
            + "zOVoVqXizyPtpF4Jz0QoXQZAyDRL9ZiH0L+1N/Rvcb7Q1M9kHf61cK0/SkbbhJg9"
            + "CtKx13eMZttux1Hulf0JcT5azIZ0zZMFVXsRitlfHeQhujnYlJyXVSnMpxlvsYqW"
            + "M3pum8VrPSkKJuEGt7nTtyE=";
    private static final String PUBLIC_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArhTK4Q+kYEBb8iT3V+Io"
            + "g/sntDSp1RapHzHBU/GeuBTtb4Jei0iCvVIT6SD4yh4cJuPeACfi4+KeBzvcRjmR"
            + "Vd69B32x+We5TXcsWEyxT0X38X04KBTK9ffURn3qQ/8eh5XAzIML9eKGiFEqq6aE"
            + "mCMQ3YHp9iq17CymLFsJ1d0sylSvBozleD+P7HwYLt+CnvI2D5b6mxkubdryODj3"
            + "J2iMllNa9qJ1epvpW+02cwr1SqKIBddTLV05i5D5iU1Z1ZwDA/m7sAaBuTQ5Re50"
            + "B2LbXH6hWBnK/1GWbUoWAEbRtJF3e395XysFZ6SibJs3D0FmWAzcub0BzeY8dxAt"
            + "bwIDAQAB";

    @Autowired
    private KeyService keyService;

    @Test
    public void testGetPrivateKey() throws Exception {
        // map private key to key class
        byte[] encoded = Base64.decodeBase64(PRIVATE_KEY);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        RSAPrivateKey expectedKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        Assertions.assertEquals(expectedKey, keyService.getPrivateKey());
    }

    @Test
    public void testGetPublicKey() throws Exception {
        // map public key to key class
        byte[] encoded = Base64.decodeBase64(PUBLIC_KEY);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        RSAPublicKey expectedKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

        Assertions.assertEquals(expectedKey, keyService.getPublicKey());
    }
}
