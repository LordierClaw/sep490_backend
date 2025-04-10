package vn.com.fpt.sep490_g28_summer2024_be.dto.donation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.challenge.ChallengeRequestDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.project.ProjectUpdateRequestDTO;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationRequestDTO {

    @JsonProperty("donation_id")
    private BigInteger donationId;

    private String id;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    private String tid;

    @JsonProperty("project")
    private ProjectUpdateRequestDTO project;

    @JsonProperty("refer")
    private AccountDTO refer;

    @JsonProperty("challenge")
    private ChallengeRequestDTO challenge;

    @JsonProperty("created_by")
    private AccountDTO createdBy;

    @JsonProperty("transferred_project")
    private ProjectUpdateRequestDTO transferredProject;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("description")
    private String description;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("bank_sub_acc_id")
    private String bankSubAccId;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("bank_name")
    private String bankName;

    @Length(max = 100, message = "Không được vượt quá 100 ký tự")
    @JsonProperty("corresponsive_name")
    private String corresponsiveName;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("correspensive_account")
    private String corresponsiveAccount;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("correspensive_bank_id")
    private String corresponsiveBankId;

    @Length(max = 20, message = "Không được vượt quá 20 ký tự")
    @JsonProperty("correspensive_bank_name")
    private String corresponsiveBankName;

    @Length(max = 500, message = "Không được vượt quá 500 ký tự")
    private String note;
}
