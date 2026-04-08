package com.finance.likelist.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class LikeList {
    private Integer sn;
    private String userId;
    private Integer productNo;
    private Integer purchaseQuantity;
    private String account;
    private BigDecimal totalFee;
    private BigDecimal totalAmount;
    private Boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdAt;

    // 查詢用的關聯欄位(並不存在資料表)
    private String userName;
    private String productName;
    private BigDecimal price;

}
