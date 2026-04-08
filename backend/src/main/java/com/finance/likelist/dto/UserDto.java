package com.finance.likelist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Data;

@Data
public class UserDto {
    
    @NotBlank(message = "缺少使用者 ID")
    @Pattern(regexp = "^[A-Z][0-9]{10}$", message = "使用者 ID 須為大寫英文開頭加 10 位數字")
    private String userId;

    @NotBlank(message = "缺少姓名")
    private String userName;

    @NotBlank(message = "缺少電子郵件")
    @Email(message = "電子郵件格式不正確")
    private String email;

    @NotBlank(message = "缺少扣款帳號")
    @Pattern(regexp = "^\\d{10}$", message = "扣款帳號須為剛好 10 位數字")
    private String account;

}
