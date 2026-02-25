package com.billsplitter.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class RegistrationDTO {
    @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;
    @NotBlank @Size(max = 50) private String displayName;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotBlank private String confirmPassword;
}
