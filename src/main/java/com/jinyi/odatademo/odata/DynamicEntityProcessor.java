package com.jinyi.odatademo.odata;

import com.jinyi.odatademo.service.DynamicEntityService;
import org.apache.olingo.commons.api.data.*;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DynamicEntityProcessor implements EntityCollectionProcessor, EntityProcessor {

    @Autowired
    private DynamicEntityService entityService;

    private OData odata;
    private ServiceMetadata serviceMetadata;

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        String entitySetName = edmEntitySet.getName();
        List<Map<String, Object>> entities = entityService.findAll(entitySetName);
        
        EntityCollection entityCollection = new EntityCollection();
        
        for (Map<String, Object> entityData : entities) {
            Entity entity = createEntity(entityData, edmEntitySet.getEntityType());
            entityCollection.getEntities().add(entity);
        }
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
                .contextURL(createContextUrl(edmEntitySet, false, null, null, null))
                .build();
        
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntitySet.getEntityType(), entityCollection, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
        
        String entitySetName = edmEntitySet.getName();
        
        // Simplified: assume ID is the first key value
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        if (keyPredicates.isEmpty()) {
            throw new ODataApplicationException("Key is required", HttpStatusCode.BAD_REQUEST.getStatusCode(), null);
        }
        
        String keyValue = keyPredicates.get(0).getText();
        // Remove single quotes if present
        if (keyValue.startsWith("'") && keyValue.endsWith("'")) {
            keyValue = keyValue.substring(1, keyValue.length() - 1);
        }
        
        Map<String, Object> entityData = entityService.findById(entitySetName, Long.parseLong(keyValue));
        
        if (entityData == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), null);
        }
        
        Entity entity = createEntity(entityData, edmEntitySet.getEntityType());
        
        ODataSerializer serializer = odata.createSerializer(responseFormat);
        EntitySerializerOptions options = EntitySerializerOptions.with()
                .contextURL(createContextUrl(edmEntitySet, true, null, null, null))
                .build();
        
        SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntitySet.getEntityType(), entity, options);
        
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        // Implement entity creation logic
        response.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode());
    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
        // Implement entity update logic
        response.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode());
    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
        // Implement entity deletion logic
        response.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED.getStatusCode());
    }

    private Entity createEntity(Map<String, Object> entityData, EdmEntityType edmEntityType) {
        Entity entity = new Entity();
        
        for (Map.Entry<String, Object> entry : entityData.entrySet()) {
            Property property = new Property();
            property.setName(entry.getKey());
            property.setValue(ValueType.PRIMITIVE, entry.getValue());
            entity.addProperty(property);
        }
        
        return entity;
    }

    private ContextURL createContextUrl(EdmEntitySet edmEntitySet, boolean isSingleEntity, 
                                     String expand, String select, String navOrPropertyPath) {
        return ContextURL.with()
                .entitySet(edmEntitySet)
                .suffix(isSingleEntity ? ContextURL.Suffix.ENTITY : null)
                .build();
    }
}