package com.aihuishou.bi.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOperation {
    private Integer id;
    private Integer observerId;
    private Integer accessId;
    private String accessName;
    private Integer active;
}
