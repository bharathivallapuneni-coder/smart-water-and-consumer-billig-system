package com.infosys.smartwater.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationValidateResponse {
    private String token;
    private String fullName;
    private String email;
    private String flatNumber;
    private String blockNumber;
    private String buildingName;
    private Boolean isValid;
    private String message;
}
