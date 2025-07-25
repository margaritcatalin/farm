package ro.piata.localmarket.core.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;
import ro.piata.localmarket.core.model.AbstractAuditingEntity;

import java.io.Serializable;

@Entity
@Table(name = "b2b_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class B2BMembership extends AbstractAuditingEntity<Long> implements Serializable, Persistable<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2BUnit_id", nullable = false)
    private B2BUnit b2BUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b2bunit_role_id", nullable = false)
    private Role role;

    @org.springframework.data.annotation.Transient
    @Transient
    private boolean isPersisted;

    @org.springframework.data.annotation.Transient
    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    @Override
    public Long getId() {
        return id;
    }
}
