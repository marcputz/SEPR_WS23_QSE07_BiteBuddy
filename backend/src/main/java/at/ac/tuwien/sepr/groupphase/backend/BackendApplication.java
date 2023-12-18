package at.ac.tuwien.sepr.groupphase.backend;

import at.ac.tuwien.sepr.groupphase.backend.config.ConfigProperties;
import at.ac.tuwien.sepr.groupphase.backend.config.FilePathsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ConfigProperties.class, FilePathsProperties.class})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
