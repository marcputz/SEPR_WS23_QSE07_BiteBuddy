package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class JpaUserService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;

    @Autowired
    public JpaUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ApplicationUser getUserByEmail(String email) throws UserNotFoundException {
        LOGGER.trace("getUserByEmail(" + email + ")");

        ApplicationUser user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with email '" + email + "' could not be found");
        }

        return user;
    }

    @Override
    public ApplicationUser getUserByNickname(String nickname) throws UserNotFoundException {
        LOGGER.trace("getUserByUsername(" + nickname + ")");

        ApplicationUser user = userRepository.findByNickname(nickname);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with username '" + nickname + "' could not be found");
        }

        return user;
    }
}
