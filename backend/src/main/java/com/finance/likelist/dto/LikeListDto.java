package com.finance.likelist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class LikeListDto {
    
    private Integer sn;

    @NotBlank(message = "缺少使用者 ID")
    @Pattern(regexp = "^[A-Z][0-9]{10}$", message = "使用者 ID 須為大寫英文開頭加 10 位數字")
    private String userId;

    @NotBlank(message = "缺少商品名稱")
    private String productName;

    @NotNull(message = "缺少價格")
    @DecimalMin(value = "0.0", inclusive = true, message = "價格不可為負數")
    private BigDecimal price;

    @NotNull(message = "缺少手續費率")
    @DecimalMin(value = "0.0", inclusive = true, message = "費率不可為負數")
    private BigDecimal feeRate;

    @NotNull(message = "請輸入申購數量")
    @Min(value = 1, message = "申購數量不能少於 1")
    private Integer purchaseQuantity;

    @NotBlank(message = "缺少扣款帳號")
    private String account;

}
