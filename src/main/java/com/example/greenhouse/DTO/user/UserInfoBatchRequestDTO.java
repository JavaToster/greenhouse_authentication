package com.example.greenhouse.DTO.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UserInfoBatchRequestDTO {
    @NotEmpty(message = "Ids set should be not empty")
    @Size(max = 500, message = "You can't request more 500 users inside one batch")
    private Set<Long> userIds;
}
