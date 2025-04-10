package vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponseDTO<T> {
    T content;
    Integer limit;
    Integer offset;
    Integer total;
}
