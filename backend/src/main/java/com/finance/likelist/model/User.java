package com.finance.likelist.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class User {
    private String userId;
    private String userName;
    private String email;
    private String account;
    private Boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updatedAt;

}
