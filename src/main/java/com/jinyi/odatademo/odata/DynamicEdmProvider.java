package com.jinyi.odatademo.odata;

import com.jinyi.odatademo.annotation.ODataField;
import com.jinyi.odatademo.service.EntityRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DynamicEdmProvider extends CsdlAbstractEdmProvider {

    public static final String NAMESPACE = "OData.Demo";
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    @Autowired
    private EntityRegistryService entityRegistryService;

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataException {
        if (entityRegistryService != null) {
            String entityName = entityTypeName.getName();
            Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
            
            if (entityClass != null) {
                return createEntityType(entityClass, entityName);
            }
        }
        
        return null;
    }

    private CsdlEntityType createEntityType(Class<?> entityClass, String entityName) {
        try {
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(entityName);
            
            List<CsdlProperty> properties = new ArrayList<>();
            List<CsdlPropertyRef> keyProperties = new ArrayList<>();
            
            for (Field field : entityClass.getDeclaredFields()) {
                ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
                if (fieldAnnotation != null) {
                    CsdlProperty property = createProperty(field, fieldAnnotation);
                    if (property != null) {
                        properties.add(property);
                        
                        if (fieldAnnotation.key()) {
                            CsdlPropertyRef keyProperty = new CsdlPropertyRef();
                            keyProperty.setName(property.getName());
                            keyProperties.add(keyProperty);
                        }
                    }
                }
            }
            
            entityType.setProperties(properties);
            entityType.setKey(keyProperties);
            
            return entityType;
        } catch (Exception e) {
            log.error("Error creating entity type for {}: {}", entityName, e.getMessage());
            return null;
        }
    }

    private CsdlProperty createProperty(Field field, ODataField fieldAnnotation) {
        CsdlProperty property = new CsdlProperty();
        String propertyName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
        property.setName(propertyName);
        property.setType(getEdmType(field.getType()));
        property.setNullable(fieldAnnotation.nullable());
        
        if (field.getType() == String.class) {
            property.setMaxLength(fieldAnnotation.length());
        }
        
        return property;
    }

    private FullQualifiedName getEdmType(Class<?> fieldType) {
        if (fieldType == String.class) {
            return EdmPrimitiveTypeKind.String.getFullQualifiedName();
        } else if (fieldType == Long.class || fieldType == long.class) {
            return EdmPrimitiveTypeKind.Int64.getFullQualifiedName();
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
        } else if (fieldType == BigDecimal.class) {
            return EdmPrimitiveTypeKind.Decimal.getFullQualifiedName();
        } else if (fieldType == LocalDateTime.class) {
            return EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName();
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return EdmPrimitiveTypeKind.Boolean.getFullQualifiedName();
        }
        return EdmPrimitiveTypeKind.String.getFullQualifiedName();
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) throws ODataException {
        if (entityContainer.equals(CONTAINER) && entityRegistryService != null) {
            Class<?> entityClass = entityRegistryService.getEntityClass(entitySetName);
            if (entityClass != null) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(entitySetName);
                entitySet.setType(new FullQualifiedName(NAMESPACE, entitySetName));
                return entitySet;
            }
        }
        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataException {
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        
        List<CsdlEntitySet> entitySets = new ArrayList<>();
        
        if (entityRegistryService != null && entityRegistryService.getEntityRegistry() != null) {
            for (String entityName : entityRegistryService.getEntityRegistry().keySet()) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(entityName);
                entitySet.setType(new FullQualifiedName(NAMESPACE, entityName));
                entitySets.add(entitySet);
            }
        }
        
        entityContainer.setEntitySets(entitySets);
        
        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() throws ODataException {
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);
        
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        if (entityRegistryService != null && entityRegistryService.getEntityRegistry() != null) {
            for (String entityName : entityRegistryService.getEntityRegistry().keySet()) {
                Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
                if (entityClass != null) {
                    CsdlEntityType entityType = createEntityType(entityClass, entityName);
                    if (entityType != null) {
                        entityTypes.add(entityType);
                    }
                }
            }
        }
        
        schema.setEntityTypes(entityTypes);
        
        CsdlEntityContainer container = getEntityContainer();
        if (container != null) {
            schema.setEntityContainer(container);
        }
        
        // 确保返回的 schema 有完整的信息
        List<CsdlSchema> schemas = new ArrayList<>();
        schemas.add(schema);
        return schemas;
    }
}