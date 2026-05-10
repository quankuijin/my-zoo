package com.zoo.admin.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private String id;
    private String name;
    private String code;
    private String type;
    private String path;
    private String parentId;
    private Integer sort;
    private String icon;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
