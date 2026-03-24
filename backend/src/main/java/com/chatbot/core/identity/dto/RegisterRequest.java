// RegisterRequest.java
package com.chatbot.core.identity.dto;

import com.chatbot.core.identity.constants.IdentityConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = IdentityConstants.EMAIL_PATTERN, message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = IdentityConstants.MIN_PASSWORD_LENGTH, message = "Mật khẩu phải có ít nhất 8 ký tự")
    @Pattern(regexp = IdentityConstants.PASSWORD_PATTERN, 
             message = "Mật khẩu phải chứa ít nhất 1 số, 1 chữ thường, 1 chữ hoa và 1 ký tự đặc biệt")
    private String password;
    
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}