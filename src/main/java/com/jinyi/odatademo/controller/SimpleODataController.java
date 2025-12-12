package com.jinyi.odatademo.controller;

import com.jinyi.odatademo.annotation.ODataField;
import com.jinyi.odatademo.dto.EntityDefinition;
import com.jinyi.odatademo.service.AdvancedODataQueryService;
import com.jinyi.odatademo.service.DynamicEntityService;
import com.jinyi.odatademo.service.EntityRegistryService;
import com.jinyi.odatademo.service.ODataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simple-odata")
public class SimpleODataController {

    @Autowired
    private EntityRegistryService entityRegistryService;

    @Autowired
    private DynamicEntityService entityService;

    @Autowired
    private ODataQueryService queryService;

    @Autowired
    private AdvancedODataQueryService advancedQueryService;

    @GetMapping(value = "/$metadata", produces = MediaType.APPLICATION_XML_VALUE)
    public String getMetadata() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n");
        xml.append("  <edmx:DataServices>\n");
        xml.append("    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"OData.Demo\">\n");
        
        // Add static entity types
        for (String entityName : entityRegistryService.getEntityRegistry().keySet()) {
            Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
            xml.append("      <EntityType Name=\"").append(entityName).append("\">\n");
            xml.append("        <Key>\n");
            
            // Find key properties
            for (Field field : entityClass.getDeclaredFields()) {
                ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
                if (fieldAnnotation != null && fieldAnnotation.key()) {
                    xml.append("          <PropertyRef Name=\"").append(field.getName()).append("\"/>\n");
                }
            }
            xml.append("        </Key>\n");
            
            // Add all properties
            for (Field field : entityClass.getDeclaredFields()) {
                ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
                if (fieldAnnotation != null) {
                    String edmType = getEdmType(field.getType());
                    xml.append("        <Property Name=\"").append(field.getName())
                       .append("\" Type=\"").append(edmType).append("\"");
                    
                    if (!fieldAnnotation.nullable()) {
                        xml.append(" Nullable=\"false\"");
                    }
                    
                    if (field.getType() == String.class && fieldAnnotation.length() > 0) {
                        xml.append(" MaxLength=\"").append(fieldAnnotation.length()).append("\"");
                    }
                    
                    xml.append("/>\n");
                }
            }
            
            xml.append("      </EntityType>\n");
        }
        
        // Add dynamic entity types
        for (String entityName : advancedQueryService.getDynamicEntityNames()) {
            EntityDefinition entityDef = advancedQueryService.getDynamicEntityDefinition(entityName);
            if (entityDef != null) {
                xml.append("      <EntityType Name=\"").append(entityName).append("\">\n");
                xml.append("        <Key>\n");
                
                // Find key properties
                for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
                    if (field.isKey()) {
                        xml.append("          <PropertyRef Name=\"").append(field.getFieldName()).append("\"/>\n");
                    }
                }
                xml.append("        </Key>\n");
                
                // Add all properties
                for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
                    String edmType = getEdmTypeFromString(field.getDataType());
                    xml.append("        <Property Name=\"").append(field.getFieldName())
                       .append("\" Type=\"").append(edmType).append("\"");
                    
                    if (!field.isNullable()) {
                        xml.append(" Nullable=\"false\"");
                    }
                    
                    if ("STRING".equals(field.getDataType()) && field.getLength() > 0) {
                        xml.append(" MaxLength=\"").append(field.getLength()).append("\"");
                    }
                    
                    xml.append("/>\n");
                }
                
                xml.append("      </EntityType>\n");
            }
        }
        
        // Add entity container
        xml.append("      <EntityContainer Name=\"Container\">\n");
        
        // Add static entity sets
        for (String entityName : entityRegistryService.getEntityRegistry().keySet()) {
            xml.append("        <EntitySet Name=\"").append(entityName)
               .append("\" EntityType=\"OData.Demo.").append(entityName).append("\"/>\n");
        }
        
        // Add dynamic entity sets
        for (String entityName : advancedQueryService.getDynamicEntityNames()) {
            xml.append("        <EntitySet Name=\"").append(entityName)
               .append("\" EntityType=\"OData.Demo.").append(entityName).append("\"/>\n");
        }
        
        xml.append("      </EntityContainer>\n");
        
        xml.append("    </Schema>\n");
        xml.append("  </edmx:DataServices>\n");
        xml.append("</edmx:Edmx>");
        
        return xml.toString();
    }

    @GetMapping(value = "/{entitySet}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getEntitySet(@PathVariable String entitySet, HttpServletRequest request) {
        // Extract query parameters
        Map<String, String> queryParams = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                queryParams.put(key, values[0]);
            }
        });
        
        // Use advanced query service for complex queries with function support
        ODataQueryService.QueryResult queryResult = advancedQueryService.advancedQuery(entitySet, queryParams);
        
        Map<String, Object> result = new HashMap<>();
        result.put("@odata.context", "/simple-odata/$metadata#" + entitySet);
        result.put("value", queryResult.getData());
        
        // Add count if requested
        if ("true".equals(queryParams.get("$count"))) {
            result.put("@odata.count", queryResult.getTotalCount());
        }
        
        return result;
    }

    @GetMapping(value = "/{entitySet}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getEntity(@PathVariable String entitySet, @PathVariable Long id) {
        Map<String, Object> entity = entityService.findById(entitySet, id);
        
        if (entity == null) {
            throw new RuntimeException("Entity not found");
        }
        
        entity.put("@odata.context", "/simple-odata/$metadata#" + entitySet + "/$entity");
        return entity;
    }

    private String getEdmType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return "Edm.String";
        } else if (fieldType == Long.class || fieldType == long.class) {
            return "Edm.Int64";
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return "Edm.Int32";
        } else if (fieldType == BigDecimal.class) {
            return "Edm.Decimal";
        } else if (fieldType == LocalDateTime.class) {
            return "Edm.DateTimeOffset";
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "Edm.Boolean";
        }
        return "Edm.String";
    }

    private String getEdmTypeFromString(String dataType) {
        switch (dataType.toUpperCase()) {
            case "STRING":
                return "Edm.String";
            case "LONG":
                return "Edm.Int64";
            case "INTEGER":
                return "Edm.Int32";
            case "DECIMAL":
                return "Edm.Decimal";
            case "DATETIME":
                return "Edm.DateTimeOffset";
            case "BOOLEAN":
                return "Edm.Boolean";
            default:
                return "Edm.String";
        }
    }

    @GetMapping(value = "/{entitySet}/$stats/{field}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getFieldStats(@PathVariable String entitySet, @PathVariable String field) {
        Map<String, Object> stats = advancedQueryService.getAggregateStats(entitySet, field);
        
        Map<String, Object> result = new HashMap<>();
        result.put("@odata.context", "/simple-odata/$metadata#" + entitySet + "/$stats/" + field);
        result.put("field", field);
        result.put("statistics", stats);
        
        return result;
    }
}