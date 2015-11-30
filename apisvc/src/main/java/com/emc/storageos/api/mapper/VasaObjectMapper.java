package com.emc.storageos.api.mapper;

import static com.emc.storageos.api.mapper.DbObjectMapper.mapDataObjectFieldsNoLink;
import static com.emc.storageos.api.mapper.DbObjectMapper.toRelatedResource;

import java.net.URI;

import com.emc.storageos.api.service.impl.response.RestLinkFactory;
import com.emc.storageos.db.client.model.StorageContainer;
import com.emc.storageos.model.ResourceTypeEnum;
import com.emc.storageos.model.RestLinkRep;
import com.emc.storageos.model.vasa.StorageContainerCreateResponse;

public class VasaObjectMapper {

    public static StorageContainerCreateResponse toStorageContainer(StorageContainer from) {
        if (from == null) {
            return null;
        }
        StorageContainerCreateResponse to = new StorageContainerCreateResponse();
        return mapStorageContainerFields(from, to);
    }

    private static StorageContainerCreateResponse mapStorageContainerFields(StorageContainer from, StorageContainerCreateResponse to) {
        mapDataObjectFieldsNoLink(from, to);
        ResourceTypeEnum type = ResourceTypeEnum.STORAGE_CONTAINER;
        to.setLink(new RestLinkRep("self", RestLinkFactory.newLink(type, from.getId())));
        to.setType(from.getType());
        to.setDescription(from.getDescription());
        to.setProtocols(from.getProtocols());
        to.setProvisioningType(from.getProvisioningType());
        to.setProtocolEndPointType(from.getProtocolEndPointType());
        to.setMaxVvolSizeMB(from.getMaxVvolSizeMB());
        
        if (from.getVirtualArrays() != null) {
            for (String neighborhood : from.getVirtualArrays()) {
                to.getVirtualArrays().add(toRelatedResource(ResourceTypeEnum.VARRAY, URI.create(neighborhood)));
            }
        }
        
        to.setStorageSystem(toRelatedResource(ResourceTypeEnum.STORAGE_SYSTEM, from.getStorageSystem()));
        
        return to;
    }
}
