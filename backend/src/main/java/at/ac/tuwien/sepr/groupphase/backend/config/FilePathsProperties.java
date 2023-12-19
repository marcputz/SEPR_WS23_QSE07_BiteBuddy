package at.ac.tuwien.sepr.groupphase.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file-paths")
public class FilePathsProperties {

    private String keySecurityFolder;
    private String privateKeyFilename;
    private String publicKeyFilename;

    public String getKeySecurityFolder() {
        return keySecurityFolder;
    }

    public void setKeySecurityFolder(String keySecurityFolder) {
        this.keySecurityFolder = keySecurityFolder;
    }

    public String getPrivateKeyFilename() {
        return privateKeyFilename;
    }

    public void setPrivateKeyFilename(String privateKeyFilename) {
        this.privateKeyFilename = privateKeyFilename;
    }

    public String getPublicKeyFilename() {
        return publicKeyFilename;
    }

    public void setPublicKeyFilename(String publicKeyFilename) {
        this.publicKeyFilename = publicKeyFilename;
    }
}
