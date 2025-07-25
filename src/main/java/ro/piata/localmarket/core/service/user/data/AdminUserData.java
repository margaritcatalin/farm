package ro.piata.localmarket.core.service.user.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ro.piata.localmarket.core.model.user.Role;
import ro.piata.localmarket.core.model.user.User;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class AdminUserData extends UserData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> roles;

    public AdminUserData() {
        // Empty constructor needed for Jackson.
    }

    public AdminUserData(User user) {
        setId(user.getId());
        setLogin(user.getLogin());
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.imageUrl = user.getImageUrl();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.roles = user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
    }

}
