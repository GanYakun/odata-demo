package com.jinyi.odata.dynamic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体文件生成服务
 * 负责根据动态实体定义生成对应的Java实体类文件
 */
@Service
@Slf4j
public class EntityFileGeneratorService {

    private static final String ENTITY_PACKAGE = "com.jinyi.business.entity";
    private static final String ENTITY_PATH = "src/main/java/com/jinyi/business/entity/";

    /**
     * 根据实体定义生成 Java 实体类文件
     */
    public String generateEntityFile(EntityDefinition entityDef) {
        try {
            String className = entityDef.getEntityName();
            String fileName = className + ".java";
            String filePath = ENTITY_PATH + fileName;
            
            // 确保目录存在
            File directory = new File(ENTITY_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // 生成 Java 代码
            String javaCode = generateJavaCode(entityDef);
            
            // 写入文件
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(javaCode);
            }
            
            log.info("Generated entity file: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("Failed to generate entity file for {}: {}", entityDef.getEntityName(), e.getMessage());
            throw new RuntimeException("Failed to generate entity file: " + e.getMessage());
        }
    }

    /**
     * 生成 Java 代码
     */
    private String generateJavaCode(EntityDefinition entityDef) {
        StringBuilder code = new StringBuilder();
        
        // Package declaration
        code.append("package ").append(ENTITY_PACKAGE).append(";\n\n");
        
        // Imports
        Set<String> imports = generateImports(entityDef);
        for (String importStatement : imports) {
            code.append("import ").append(importStatement).append(";\n");
        }
        code.append("\n");
        
        // Class declaration with annotations
        code.append("@Data\n");
        code.append("@ODataEntity(name = \"").append(entityDef.getEntityName()).append("\"");
        if (entityDef.getTableName() != null && !entityDef.getTableName().isEmpty()) {
            code.append(", table = \"").append(entityDef.getTableName()).append("\"");
        }
        code.append(")\n");
        
        // Add class comment if description exists
        if (entityDef.getDescription() != null && !entityDef.getDescription().isEmpty()) {
            code.append("/**\n");
            code.append(" * ").append(entityDef.getDescription()).append("\n");
            code.append(" * Generated automatically by Dynamic Entity Registration Service\n");
            code.append(" */\n");
        }
        
        code.append("public class ").append(entityDef.getEntityName()).append(" {\n\n");
        
        // Fields
        for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
            generateField(code, field);
        }
        
        code.append("}\n");
        
        return code.toString();
    }

    /**
     * 生成导入语句
     */
    private Set<String> generateImports(EntityDefinition entityDef) {
        Set<String> imports = new HashSet<>();
        
        // 基础导入
        imports.add("com.jinyi.odata.annotation.ODataEntity");
        imports.add("com.jinyi.odata.annotation.ODataField");
        imports.add("lombok.Data");
        
        // 根据字段类型添加导入
        for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
            String dataType = field.getDataType().toUpperCase();
            switch (dataType) {
                case "DECIMAL":
                    imports.add("java.math.BigDecimal");
                    break;
                case "DATETIME":
                    imports.add("java.time.LocalDateTime");
                    break;
            }
        }
        
        return imports;
    }

    /**
     * 生成字段代码
     */
    private void generateField(StringBuilder code, EntityDefinition.FieldDefinition field) {
        // 添加字段注释
        if (field.getDescription() != null && !field.getDescription().isEmpty()) {
            code.append("    /**\n");
            code.append("     * ").append(field.getDescription()).append("\n");
            code.append("     */\n");
        }
        
        // 添加 @ODataField 注解
        code.append("    @ODataField(");
        
        // 添加注解参数
        boolean hasParams = false;
        
        if (field.isKey()) {
            code.append("key = true");
            hasParams = true;
        }
        
        if (!field.isNullable()) {
            if (hasParams) code.append(", ");
            code.append("nullable = false");
            hasParams = true;
        }
        
        if ("STRING".equals(field.getDataType()) && field.getLength() > 0 && field.getLength() != 255) {
            if (hasParams) code.append(", ");
            code.append("length = ").append(field.getLength());
            hasParams = true;
        }
        
        if (field.getColumnName() != null && !field.getColumnName().isEmpty() && 
            !field.getColumnName().equals(camelToSnake(field.getFieldName()))) {
            if (hasParams) code.append(", ");
            code.append("name = \"").append(field.getColumnName()).append("\"");
        }
        
        code.append(")\n");
        
        // 字段声明
        String javaType = getJavaType(field.getDataType());
        code.append("    private ").append(javaType).append(" ").append(field.getFieldName()).append(";\n\n");
    }

    /**
     * 获取 Java 类型
     */
    private String getJavaType(String dataType) {
        switch (dataType.toUpperCase()) {
            case "STRING":
                return "String";
            case "LONG":
                return "Long";
            case "INTEGER":
                return "Integer";
            case "DECIMAL":
                return "BigDecimal";
            case "DATETIME":
                return "LocalDateTime";
            case "BOOLEAN":
                return "Boolean";
            default:
                return "String";
        }
    }

    /**
     * 驼峰转下划线
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 检查实体文件是否存在
     */
    public boolean entityFileExists(String entityName) {
        String filePath = ENTITY_PATH + entityName + ".java";
        return new File(filePath).exists();
    }

    /**
     * 删除实体文件
     */
    public boolean deleteEntityFile(String entityName) {
        try {
            String filePath = ENTITY_PATH + entityName + ".java";
            File file = new File(filePath);
            
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("Deleted entity file: {}", filePath);
                } else {
                    log.warn("Failed to delete entity file: {}", filePath);
                }
                return deleted;
            } else {
                log.info("Entity file does not exist: {}", filePath);
                return true; // 文件不存在也算删除成功
            }
        } catch (Exception e) {
            log.error("Error deleting entity file for {}: {}", entityName, e.getMessage());
            return false;
        }
    }

    /**
     * 获取实体文件路径
     */
    public String getEntityFilePath(String entityName) {
        return ENTITY_PATH + entityName + ".java";
    }

    /**
     * 生成实体文件预览（不写入文件）
     */
    public String previewEntityFile(EntityDefinition entityDef) {
        return generateJavaCode(entityDef);
    }
}