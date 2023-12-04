package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class JpaUserService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final UserValidator validator;

    @Autowired
    public JpaUserService(UserRepository userRepository, UserValidator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Override
    public ApplicationUser getUserByEmail(String email) throws UserNotFoundException {
        LOGGER.trace("getUserByEmail({})", email);

        ApplicationUser user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with email '" + email + "' could not be found");
        }

        return user;
    }

    @Override
    public ApplicationUser getUserByNickname(String nickname) throws UserNotFoundException {
        LOGGER.trace("getUserByUsername({})", nickname);

        ApplicationUser user = userRepository.findByNickname(nickname);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with username '" + nickname + "' could not be found");
        }

        return user;
    }

    @Override
    public ApplicationUser update(ApplicationUser applicationUser) throws UserNotFoundException, ValidationException, ConflictException {
        LOGGER.trace("update({})", applicationUser);
        validator.validateForUpdate(applicationUser);

        //TODO Add Conflict and NotFound Handling
        ApplicationUser updatedUser = userRepository.save(applicationUser);

        return updatedUser;
    }

    @Override
    public ApplicationUser create(UserRegisterDto registerDto) {
        LOGGER.trace("create({})", registerDto);
        //validator.validateForCreate(registerDto);
        ApplicationUser applicationUser = new ApplicationUser(registerDto.getEmail(), registerDto.getPasswordEncoded());
        applicationUser.setNickname(registerDto.getName());
        return userRepository.save(applicationUser);
    }


}
