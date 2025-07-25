package ro.piata.localmarket.core.model.user;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;
import ro.piata.localmarket.core.model.AbstractAuditingEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "b2bunits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class B2BUnit extends AbstractAuditingEntity<Long> implements Serializable, Persistable<Long> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(JsonType.class)
    private Map<String, String> name = new HashMap<>();

    @OneToMany(mappedBy = "b2BUnit", cascade = CascadeType.ALL)
    private Set<B2BMembership> memberships = new HashSet<>();

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
