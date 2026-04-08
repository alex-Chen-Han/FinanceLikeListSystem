package com.finance.likelist.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class Product {
    private Integer no;
    private String productName;
    private BigDecimal price;
    private BigDecimal feeRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdAt;

}
