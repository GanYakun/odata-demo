# Java Entity File Generation

This document describes the Java entity file generation feature that automatically creates Java entity classes when dynamic entities are registered.

## Overview

When you register a dynamic entity through the REST API, the system can automatically generate a corresponding Java entity class file in the `src/main/java/com/jinyi/odatademo/entity/` directory. This provides several benefits:

1. **Code Visibility**: Generated entities are visible in your IDE and can be used for development
2. **Type Safety**: Generated classes provide compile-time type checking
3. **Documentation**: Generated files include proper JavaDoc comments
4. **Integration**: Generated entities can be used with other Spring Boot features

## Features

### Automatic Generation
- Java files are generated automatically when registering entities (default behavior)
- Can be disabled by setting `generateJavaFile=false` in the registration request
- Files include proper package declarations, imports, and annotations

### Generated Content
Each generated Java file includes:
- Proper package declaration (`com.jinyi.odatademo.entity`)
- Required imports (`@ODataEntity`, `@ODataField`, `@Data`, data type imports)
- Class-level annotations (`@Data`, `@ODataEntity`)
- JavaDoc comments for class and fields
- Properly typed fields with appropriate annotations

### Supported Data Types
The generator supports all dynamic entity data types:
- `STRING` → `String`
- `LONG` → `Long` 
- `INTEGER` → `Integer`
- `DECIMAL` → `BigDecimal`
- `DATETIME` → `LocalDateTime`
- `BOOLEAN` → `Boolean`

## API Endpoints

### Register Entity with File Generation
```http
POST /api/entities/register?generateJavaFile=true
Content-Type: application/json

{
  "entityName": "Customer",
  "tableName": "customers",
  "description": "Customer entity",
  "autoCreate": true,
  "fields": [...]
}
```

### Preview Generated File (without creating)
```http
POST /api/entities/preview
Content-Type: application/json

{
  "entityName": "Customer",
  "fields": [...]
}
```

### Generate File for Existing Entity
```http
POST /api/entities/{entityName}/generate-file
```

### Check File Status
```http
GET /api/entities/{entityName}/file-status
```

### Delete Entity File
```http
DELETE /api/entities/{entityName}/file
```

### Delete Entity with File
```http
DELETE /api/entities/{entityName}?dropTable=true&deleteJavaFile=true
```

## Example Generated File

```java
package com.jinyi.odatademo.entity;

import com.jinyi.odatademo.annotation.ODataEntity;
import com.jinyi.odatademo.annotation.ODataField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ODataEntity(name = "Customer", table = "customers")
/**
 * Customer entity for testing
 * Generated automatically by Dynamic Entity Registration Service
 */
public class Customer {

    /**
     * Customer ID
     */
    @ODataField(key = true, nullable = false)
    private Long id;

    /**
     * Customer name
     */
    @ODataField(nullable = false, length = 100)
    private String name;

    /**
     * Customer email
     */
    @ODataField()
    private String email;

    /**
     * Creation timestamp
     */
    @ODataField(nullable = false)
    private LocalDateTime createdAt;
}
```

## Configuration

### Field Annotations
The generator automatically adds appropriate `@ODataField` annotations based on field properties:
- `key = true` for primary key fields
- `nullable = false` for required fields
- `length = N` for STRING fields with custom length (if not 255)
- `name = "column_name"` for custom column names

### File Management
- Files are created in UTF-8 encoding
- Existing files are overwritten when regenerating
- File deletion is optional when unregistering entities
- Directory structure is created automatically if needed

## Integration with Dynamic Entities

The Java file generation is fully integrated with the dynamic entity system:
1. Files are generated during entity registration
2. Files are deleted during entity unregistration (optional)
3. File status can be checked independently
4. Files can be regenerated for existing entities

## Best Practices

1. **Use meaningful entity names** - they become Java class names
2. **Provide descriptions** - they become JavaDoc comments
3. **Preview before registering** - use the preview endpoint to check generated code
4. **Keep files in sync** - regenerate files if entity definitions change
5. **Version control** - consider adding generated files to version control for team development

## Troubleshooting

### File Not Generated
- Check application logs for errors
- Verify write permissions on the entity directory
- Ensure entity registration was successful

### Compilation Errors
- Verify all required imports are present
- Check for naming conflicts with existing classes
- Ensure field names are valid Java identifiers

### File Deletion Issues
- Check file system permissions
- Verify file is not locked by IDE or other processes
- Use manual deletion endpoint if automatic deletion fails