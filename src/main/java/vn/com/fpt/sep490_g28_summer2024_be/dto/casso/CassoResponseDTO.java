package vn.com.fpt.sep490_g28_summer2024_be.dto.casso;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CassoResponseDTO {
    private Integer page;
    private Integer pageSize;
    private Integer nextPage;
    private Integer prevPage;
    private Integer totalPages;
    private Integer totalRecords;
    private List<CassoRecordDTO> records;
}
