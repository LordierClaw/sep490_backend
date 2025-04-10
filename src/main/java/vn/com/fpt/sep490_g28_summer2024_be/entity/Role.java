package vn.com.fpt.sep490_g28_summer2024_be.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", columnDefinition = "BIGINT")
    BigInteger roleId;

    @Column(name = "role_name", columnDefinition = "NVARCHAR(50)")
    String roleName;

    @Column(name = "role_description", columnDefinition = "NVARCHAR(255)")
    String roleDescription;

    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Column(name = "is_active", columnDefinition = "BOOLEAN")
    Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "role")
    List<Account> accounts;

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleDescription='" + roleDescription + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isActive=" + isActive +
                '}';
    }
}
