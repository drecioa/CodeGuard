package es.tfg.codeguard.service.imp;

import java.util.Optional;

import es.tfg.codeguard.model.dto.UserPrivilegesDTO;
import es.tfg.codeguard.util.PasswordNotValidException;
import es.tfg.codeguard.util.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import es.tfg.codeguard.model.dto.UserDTO;
import es.tfg.codeguard.model.dto.UserPassDTO;
import es.tfg.codeguard.model.entity.deleteduser.DeletedUser;
import es.tfg.codeguard.model.entity.user.User;
import es.tfg.codeguard.model.entity.userpass.UserPass;
import es.tfg.codeguard.model.repository.deleteduser.DeletedUserRepository;
import es.tfg.codeguard.model.repository.user.UserRepository;
import es.tfg.codeguard.model.repository.userpass.UserPassRepository;
import es.tfg.codeguard.service.AdminService;

@Service
public class AdminServiceImp implements AdminService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPassRepository userPassRepository;
    @Autowired
    private DeletedUserRepository deletedUserRepository;

    @Override
    public UserDTO deleteUser(String username) {

        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found [" + username + "]");
        }

        User user = userOptional.get();

        DeletedUser deletedUser = new DeletedUser(user);

        deletedUserRepository.save(deletedUser);

        userRepository.delete(user);

        return new UserDTO(deletedUser);
    }

    @Override
    public UserPassDTO updatePassword(String username, String newUserPass) {

        Optional<User> userOptional = userRepository.findById(username);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found [" + username + "]");
        }

        UserPass userPass = userPassRepository.findById(username).get();

        try {
            checkPassword(newUserPass);
            userPass.setHashedPass(passwordEncoder.encode(newUserPass));
        } catch (PasswordNotValidException e) {
            throw new PasswordNotValidException("Password not valid [" + newUserPass + "]");
        }

        userPassRepository.save(userPass);

        return new UserPassDTO(userPass);

    }

    //TODO: COMRPOBAR QUE EL ADMIN ES ADMIN MEDIANTE EL TOKEN
    @Override
    public UserDTO updateUserPrivileges(UserPrivilegesDTO userPrivilegesDTO) {

        Optional<User> userOptional = userRepository.findById(userPrivilegesDTO.username());

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found [ " + userPrivilegesDTO.username() + " ]");
        }

        User user = userOptional.get();
        user.setTester(userPrivilegesDTO.tester());
        user.setCreator(userPrivilegesDTO.creator());

        userRepository.save(user);

        return new UserDTO(user);

    }

    private void checkPassword(String password) {
        if (password == null || password.equals(""))
            throw new PasswordNotValidException("Password not valid [ " + password + " ]");
    }

}
