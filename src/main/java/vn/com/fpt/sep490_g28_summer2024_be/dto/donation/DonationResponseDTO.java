package vn.com.fpt.sep490_g28_summer2024_be.dto.donation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectResponseDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationResponseDTO {

    @JsonProperty("donation_id")
    private BigInteger donationId;

    private String id;

    private String tid;

    @JsonProperty("project")
    private ProjectResponseDTO project;

    @JsonProperty("refer")
    private AccountDTO refer;

    @JsonProperty("challenge")
    private ChallengeResponseDTO challenge;

    @JsonProperty("transferred_project")
    private ProjectResponseDTO transferredProject;

    @JsonProperty("created_by")
    private AccountDTO createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("description")
    private String description;

    @JsonProperty("bank_sub_acc_id")
    private String bankSubAccId;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("corresponsive_name")
    private String corresponsiveName;

    @JsonProperty("correspensive_account")
    private String corresponsiveAccount;

    @JsonProperty("correspensive_bank_id")
    private String corresponsiveBankId;

    @JsonProperty("correspensive_bank_name")
    private String corresponsiveBankName;

    private String note;

    private BigDecimal target;

    @JsonProperty("total_donation")
    private BigDecimal totalDonation;
    @JsonProperty("status")
    private String status;

}
