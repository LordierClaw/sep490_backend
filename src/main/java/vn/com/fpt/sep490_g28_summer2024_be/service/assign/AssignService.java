package vn.com.fpt.sep490_g28_summer2024_be.service.assign;

import vn.com.fpt.sep490_g28_summer2024_be.dto.account.admin.AccountDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.assign.AssignResponseDTO;
import vn.com.fpt.sep490_g28_summer2024_be.dto.pageinfo.PageResponse;
import vn.com.fpt.sep490_g28_summer2024_be.entity.Assign;

import java.math.BigInteger;
import java.util.List;

public interface AssignService {
    PageResponse<AssignResponseDTO> viewMemberInProjectByFilter(Integer page, Integer size, BigInteger projectId, BigInteger roleId, String email, String name);
    List<AccountDTO> viewMembersNotAssignedToProject(BigInteger id);
    List<Assign> addMembersToProject(List<BigInteger> accountIds, BigInteger projectId);

    void removeMember(BigInteger assignId);
}
