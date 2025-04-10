package vn.com.fpt.sep490_g28_summer2024_be.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "assign")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Assign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assign_id", columnDefinition = "BIGINT")
    BigInteger assignId;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "project_id")
    Project project;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "account_id")
    Account account;
    @ManyToOne
    @JoinColumn(name = "created_by")
    Account createdBy;
    @Column(name = "created_at", columnDefinition = "DATETIME")
    LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "updated_by")
    Account updatedBy;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Assign{" +
                "assignId=" + assignId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
