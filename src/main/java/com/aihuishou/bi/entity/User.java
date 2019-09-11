package com.aihuishou.bi.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class User implements Serializable {

    private Integer obId;

    private String name;

    private String mobile;

    private String email;

    private String employeeNo;

    private Boolean admin = false;

    public User(Integer obId) {
        this.obId = obId;
    }

    public User() {
    }

    public User(Integer obId, String name, String mobile, String email, String employeeNo, Boolean admin) {
        this.obId = obId;
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.employeeNo = employeeNo;
        this.admin = admin;
    }
}
