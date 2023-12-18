package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import at.ac.tuwien.sepr.groupphase.backend.utils.ResourceFileUtils;
import jakarta.annotation.PostConstruct;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

/**
 * Implementation of {@link KeyService} using files.
 *
 * @author Marc Putz
 */
@Service
@Scope("singleton")
public class FileKeyService implements KeyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${file-paths.key-security-folder}")
    private String keyFolder;

    @Value("${file-paths.private-key-filename}")
    private String privateKeyFilename;

    @Value("${file-paths.public-key-filename}")
    private String publicKeyFilename;

    private File keyFolderFile = null;

    private ResourceFileUtils resourceFileUtils;

    public FileKeyService() {}

    @Override
    public RSAPrivateKey getPrivateKey() {
        LOGGER.trace("getPrivateKey()");
        try {
            File file = resourceFileUtils.getResourceFile(this.privateKeyFilename);
            initKeyFolder();
            return readPrivateKeyFromFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RSAPublicKey getPublicKey() {
        LOGGER.trace("getPublicKey()");
        try {
            File file = resourceFileUtils.getResourceFile(this.publicKeyFilename);
            initKeyFolder();
            return readPublicKeyFromFile(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initKeyFolder() {
        LOGGER.trace("initKeyFolder()");
        try {
            resourceFileUtils = new ResourceFileUtils(keyFolder, Optional.empty());
            this.keyFolderFile = resourceFileUtils.getResourceFile(keyFolder);

            LOGGER.debug("Using key folder at {}", this.keyFolderFile.getAbsolutePath());

            if (!keyFolderFile.exists()) {
                throw new IllegalArgumentException("Key folder does not exist or is NULL value");
            }

            if (!keyFolderFile.canRead()) {
                throw new IllegalArgumentException("Application has no permission to read key folder at {}" + keyFolderFile.getAbsolutePath());
            }

            if (!keyFolderFile.isDirectory()) {
                throw new IllegalArgumentException("Specified key folder is not a directory");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private RSAPrivateKey readPrivateKeyFromFile(File file) {
        LOGGER.trace("readPrivateKeyFromFile({})", file.getAbsolutePath());
        try {
            if (!file.exists()) {
                LOGGER.error("private key file does not exist");
                return null;
            }

            if (!file.canRead()) {
                LOGGER.error("Application has no read permission on private key file");
                return null;
            }

            if (!file.isFile()) {
                LOGGER.error("Specified private key filepath is not a file");
                return null;
            }
            // read file content
            String key = Files.readString(file.toPath(), Charset.defaultCharset());

            // remove headers
            String privateKeyPem = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] encoded = Base64.decodeBase64(privateKeyPem);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (IOException ex) {
            LOGGER.error("Could not read private key file: " + ex.getMessage());
            return null;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Application environment is unable to read RSA keys");
            return null;
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("Private key is not in valid format");
            return null;
        }
    }

    private RSAPublicKey readPublicKeyFromFile(File file) {
        LOGGER.trace("readPublicKeyFromFile({})", file.getAbsolutePath());
        try {
            if (!file.exists()) {
                LOGGER.error("Public key file does not exist");
                return null;
            }

            if (!file.canRead()) {
                LOGGER.error("Application has no read permission on public key file");
                return null;
            }

            if (!file.isFile()) {
                LOGGER.error("Specified public key filepath is not a file");
                return null;
            }

            // read file content
            String key = Files.readString(file.toPath(), Charset.defaultCharset());

            // remove headers
            String publicKeyPem = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.decodeBase64(publicKeyPem);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);

            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (IOException ex) {
            LOGGER.error("Could not read public key file: " + ex.getMessage());
            return null;
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.error("Application environment is unable to read RSA keys");
            return null;
        } catch (InvalidKeySpecException ex) {
            LOGGER.error("Public key is not in valid format");
            return null;
        }
    }
}
