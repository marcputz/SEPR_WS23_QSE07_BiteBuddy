package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Profile("generateData")
@Component
public class DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;

    public DataGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void generateData() {
        // Generate data here
        generateUserData();
    }

    private void generateUserData() {
        if (userRepository.findAll().size() > 0) {
           userRepository.deleteAll();
        }

        LOGGER.debug("generating default test users");

        ApplicationUser user1 = new ApplicationUser().setEmail("max.mustermann@test.at").setPasswordEncoded("password").setNickname("maxmuster");
        LOGGER.debug("saving user '" + user1.getNickname() + "'");
        userRepository.save(user1);

        ApplicationUser user2 = new ApplicationUser().setEmail("john.doe@nasa.org").setPasswordEncoded("password").setNickname("johndoe");
        LOGGER.debug("saving user '" + user2.getNickname() + "'");
        userRepository.save(user2);
    }
}
