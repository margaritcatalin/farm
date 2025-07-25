package ro.piata.localmarket.core.service.user.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * A DTO representing a password change required data - current and new password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordChangeData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String currentPassword;
    private String newPassword;
}
