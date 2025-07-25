package ro.piata.localmarket.core.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ro.piata.localmarket.core.model.user.User;
import ro.piata.localmarket.core.service.user.data.AdminUserData;
import ro.piata.localmarket.core.service.user.data.UserData;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> activateRegistration(String key);

    Optional<User> completePasswordReset(String newPassword, String key);

    Optional<User> requestPasswordReset(String mail);

    User registerUser(AdminUserData userDTO, String password);

    User createUser(AdminUserData userDTO);

    Optional<AdminUserData> updateUser(AdminUserData userDTO);

    void deleteUser(String login);

    void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl);

    void changePassword(String currentClearTextPassword, String newPassword);

    Page<AdminUserData> getAllManagedUsers(Pageable pageable);

    Page<UserData> getAllPublicUsers(Pageable pageable);

    Optional<User> getUserWithRolesByLogin(String login);

    Optional<User> getUserWithRoles();

    void removeNotActivatedUsers();

    List<String> getRoles();
}
