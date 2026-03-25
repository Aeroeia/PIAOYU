---
name: "damai-scaffold"
description: "Generates Spring Boot components (Entity, Controller, Service, Mapper, DTO, VO) following Damai project conventions. Invoke when creating new APIs, entities, or CRUD logic."
---

# Damai Project Scaffold

This skill generates code following the specific architecture and conventions of the `damai-opt` project.

## Architecture & Conventions

- **Framework**: Spring Boot 3 + Spring Cloud
- **ORM**: MyBatis-Plus
  - Entities must extend `BaseTableData`
  - Use `@TableName("d_table_name")`
  - Implement `Serializable`
- **JSON**: FastJSON (`com.alibaba.fastjson`)
- **Documentation**: Swagger V3 (`io.swagger.v3.oas.annotations`)
  - `@Tag(name = "xxx", description = "xxx")` on Controller
  - `@Operation(summary = "xxx")` on methods
- **Response**: `ApiResponse<T>` (`com.damai.common.ApiResponse`)
- **Injection**: Field injection with `@Autowired` (do not use constructor injection for consistency)
- **Lombok**: `@Data`, `@Slf4j`
- **DTO/VO**: Strict separation.
  - Input: `XxxDto`
  - Output: `XxxVo`

## Template Examples

### 1. Entity
```java
package com.damai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.damai.data.BaseTableData;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
@TableName("d_example")
public class Example extends BaseTableData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    // other fields...
}
```

### 2. Controller
```java
package com.damai.controller;

import com.damai.common.ApiResponse;
import com.damai.dto.ExampleDto;
import com.damai.service.ExampleService;
import com.damai.vo.ExampleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/example")
@Tag(name = "example", description = "示例模块")
public class ExampleController {

    @Autowired
    private ExampleService exampleService;

    @Operation(summary = "创建示例")
    @PostMapping("/create")
    public ApiResponse<Boolean> create(@Valid @RequestBody ExampleDto dto) {
        return ApiResponse.ok(exampleService.create(dto));
    }
}
```

### 3. Service
```java
package com.damai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.dto.ExampleDto;
import com.damai.entity.Example;
import com.damai.mapper.ExampleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class ExampleService extends ServiceImpl<ExampleMapper, Example> {
    
    // Business logic...
    public Boolean create(ExampleDto dto) {
        // implementation
        return true;
    }
}
```

### 4. Mapper
```java
package com.damai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.entity.Example;

public interface ExampleMapper extends BaseMapper<Example> {
}
```

## Usage
When the user asks to "create an API for X" or "add X feature", generate all necessary layers (Controller, Service, Mapper, Entity, DTO, VO) following the patterns above.
