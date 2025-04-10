package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "wrong_donation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wrong_donation_id", columnDefinition = "BIGINT")
    private BigInteger wrongDonationId;

    @OneToOne
    @JoinColumn(name = "donation_id", referencedColumnName = "donation_id")
    private Donation donation;

    @Override
    public String toString() {
        return "WrongDonation{" +
                "wrongDonationId=" + wrongDonationId +
                '}';
    }
}
