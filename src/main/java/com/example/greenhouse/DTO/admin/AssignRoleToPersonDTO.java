package com.example.greenhouse.DTO.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssignRoleToPersonDTO {
    @NotBlank(message = "Роль не может быть пустой")
    @Pattern(
            regexp = "^ROLE_(ADMIN|OWNER|WORKER|INSTALLER|UNKNOWN)$",
            message = "Недопустимая роль. Доступны: ROLE_ADMIN, ROLE_OWNER, ROLE_WORKER, ROLE_INSTALLER"
    )
    private String role;
}
