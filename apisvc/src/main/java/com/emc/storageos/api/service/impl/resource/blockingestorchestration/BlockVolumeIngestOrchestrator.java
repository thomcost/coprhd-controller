/*
 * Copyright (c) 2008-2015 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.api.service.impl.resource.blockingestorchestration;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.api.service.impl.resource.blockingestorchestration.IngestStrategyFactory.IngestStrategyEnum;
import com.emc.storageos.api.service.impl.resource.blockingestorchestration.IngestStrategyFactory.ReplicationStrategy;
import com.emc.storageos.api.service.impl.resource.blockingestorchestration.IngestStrategyFactory.VolumeType;
import com.emc.storageos.api.service.impl.resource.blockingestorchestration.context.IngestionRequestContext;
import com.emc.storageos.api.service.impl.resource.utils.PropertySetterUtil;
import com.emc.storageos.api.service.impl.resource.utils.VolumeIngestionUtil;
import com.emc.storageos.db.client.model.BlockObject;
import com.emc.storageos.db.client.model.BlockSnapshot;
import com.emc.storageos.db.client.model.StoragePool;
import com.emc.storageos.db.client.model.VirtualPool;
import com.emc.storageos.db.client.model.Volume;
import com.emc.storageos.db.client.model.UnManagedDiscoveredObjects.UnManagedVolume;
import com.emc.storageos.db.client.model.UnManagedDiscoveredObjects.UnManagedVolume.SupportedVolumeInformation;

/**
 * Responsible for ingesting block local volumes.
 */
public class BlockVolumeIngestOrchestrator extends BlockIngestOrchestrator {

    private static final Logger _logger = LoggerFactory.getLogger(BlockVolumeIngestOrchestrator.class);

    // A reference to the ingest strategy factory.
    protected IngestStrategyFactory ingestStrategyFactory;

    /**
     * Setter for the ingest strategy factory.
     * 
     * @param ingestStrategyFactory A reference to the ingest strategy factory.
     */
    public void setIngestStrategyFactory(IngestStrategyFactory ingestStrategyFactory) {
        this.ingestStrategyFactory = ingestStrategyFactory;
    }

    @Override
    protected <T extends BlockObject> T ingestBlockObjects(IngestionRequestContext requestContext, Class<T> clazz)
            throws IngestionException {

        UnManagedVolume unManagedVolume = requestContext.getCurrentUnmanagedVolume();
        boolean unManagedVolumeExported = requestContext.getVolumeContext().isVolumeExported();
        Volume volume = null;

        URI unManagedVolumeUri = unManagedVolume.getId();
        String volumeNativeGuid = unManagedVolume.getNativeGuid().replace(VolumeIngestionUtil.UNMANAGEDVOLUME,
                VolumeIngestionUtil.VOLUME);

        volume = VolumeIngestionUtil.checkIfVolumeExistsInDB(volumeNativeGuid, _dbClient);
        // Check if ingested volume has export masks pending for ingestion.
        if (isExportIngestionPending(volume, unManagedVolumeUri, unManagedVolumeExported)) {
            return clazz.cast(volume);
        }

        if (null == volume) {
            validateUnManagedVolume(unManagedVolume, requestContext.getVpool());
            // @TODO Need to revisit this. In 8.x Provider, ReplicationGroup is automatically created when a volume is associated to a
            // StorageGroup.
            // checkUnManagedVolumeAddedToCG(unManagedVolume, virtualArray, tenant, project, vPool);
            checkVolumeExportState(unManagedVolume, unManagedVolumeExported);
            checkVPoolValidForExportInitiatorProtocols(requestContext.getVpool(), unManagedVolume);
            checkHostIOLimits(requestContext.getVpool(), unManagedVolume, unManagedVolumeExported);

            StoragePool pool = validateAndReturnStoragePoolInVAarray(unManagedVolume, requestContext.getVarray());

            // validate quota is exceeded for storage systems and pools
            checkSystemResourceLimitsExceeded(requestContext.getStorageSystem(), unManagedVolume, requestContext.getExhaustedStorageSystems());
            checkPoolResourceLimitsExceeded(requestContext.getStorageSystem(), pool, unManagedVolume, requestContext.getExhaustedPools());
            String autoTierPolicyId = getAutoTierPolicy(unManagedVolume, requestContext.getStorageSystem(), requestContext.getVpool());
            validateAutoTierPolicy(autoTierPolicyId, unManagedVolume, requestContext.getVpool());

            volume = createVolume(requestContext.getStorageSystem(), volumeNativeGuid, pool, 
                    requestContext.getVarray(), requestContext.getVpool(), unManagedVolume, 
                    requestContext.getProject(), requestContext.getTenant(), autoTierPolicyId);
        }

        if (volume != null) {
            String syncActive = PropertySetterUtil.extractValueFromStringSet(
                    SupportedVolumeInformation.IS_SYNC_ACTIVE.toString(), unManagedVolume.getVolumeInformation());
            boolean isSyncActive = (null != syncActive) ? Boolean.parseBoolean(syncActive) : false;
            volume.setSyncActive(isSyncActive);

            if (VolumeIngestionUtil.isFullCopy(unManagedVolume)) {
                _logger.info("Setting clone related properties {}", unManagedVolume.getId());
                String replicaState = PropertySetterUtil.extractValueFromStringSet(
                        SupportedVolumeInformation.REPLICA_STATE.toString(), unManagedVolume.getVolumeInformation());
                volume.setReplicaState(replicaState);
            }
        }

        // Note that a VPLEX backend volume can also be a snapshot target volume.
        // When the VPLEX ingest orchestrator is executed, it gets the ingestion
        // strategy for the backend volume and executes it. If the backend volume
        // is both a snapshot and a VPLEX backend volume, this local volume ingest
        // strategy is invoked and a Volume instance will result. That is fine because
        // we need to represent that VPLEX backend volume. However, we also need a
        // BlockSnapshot instance to represent the snapshot target volume. Therefore,
        // if the unmanaged volume is also a snapshot target volume, we get and
        // execute the local snapshot ingest strategy to create this BlockSnapshot
        // instance and we add it to the created object list. Note that since the
        // BlockSnapshot is added to the created objects list and the Volume and
        // BlockSnapshot instance will have the same native GUID, we must be careful
        // about adding the Volume to the created object list in the VPLEX ingestion
        // strategy.
        BlockObject snapshot = null;
        if (VolumeIngestionUtil.isSnapshot(unManagedVolume)) {
            String strategyKey = ReplicationStrategy.LOCAL.name() + "_" + VolumeType.SNAPSHOT.name();
            IngestStrategy ingestStrategy = ingestStrategyFactory.getIngestStrategy(IngestStrategyEnum.getIngestStrategy(strategyKey));
            snapshot = ingestStrategy.ingestBlockObjects(requestContext, BlockSnapshot.class);
            requestContext.getObjectsToBeCreatedMap().put(snapshot.getNativeGuid(), snapshot);
        }

        // Run this always when volume NO_PUBLIC_ACCESS
        if (markUnManagedVolumeInactive(requestContext, volume)) {
            _logger.info("All the related replicas and parent has been ingested ",
                    unManagedVolume.getNativeGuid());
            // mark inactive if this is not to be exported. Else, mark as
            // inactive after successful export
            if (!unManagedVolumeExported) {
                unManagedVolume.setInactive(true);
                requestContext.getUnManagedVolumesToBeDeleted().add(unManagedVolume);
            }
        } else if (volume != null) {
            _logger.info(
                    "Not all the parent/replicas of unManagedVolume {} have been ingested , hence marking as internal",
                    unManagedVolume.getNativeGuid());
            volume.addInternalFlags(INTERNAL_VOLUME_FLAGS);
        }

        return clazz.cast(volume);
    }

    @Override
    protected void validateAutoTierPolicy(String autoTierPolicyId, UnManagedVolume unManagedVolume, VirtualPool vPool) {
        String associatedSourceVolume = PropertySetterUtil.extractValueFromStringSet(
                SupportedVolumeInformation.LOCAL_REPLICA_SOURCE_VOLUME.toString(),
                unManagedVolume.getVolumeInformation());
        // Skip autotierpolicy validation for clones as we use same orchestration for both volume & clone.
        if (null != associatedSourceVolume) {
            return;
        } else {
            super.validateAutoTierPolicy(autoTierPolicyId, unManagedVolume, vPool);
        }
    }
}
