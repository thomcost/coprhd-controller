/*
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.db.gc;

import java.net.URI;
import com.emc.storageos.security.geo.GeoDependencyChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.coordinator.client.service.CoordinatorClient;

import com.emc.storageos.db.client.DbClient;
import com.emc.storageos.db.client.model.DataObject;
import com.emc.storageos.db.common.DependencyTracker;

/**
 * Runnable implementation for the GC threads
 */
public class GlobalGCRunnable extends GarbageCollectionRunnable {
    private static final Logger log = LoggerFactory.getLogger(GlobalGCRunnable.class);

    private GeoDependencyChecker geoDependencyChecker;

    GlobalGCRunnable(DbClient dbClient, Class<? extends DataObject> type,
            DependencyTracker tracker, int gcDelayMins,
            CoordinatorClient coordinator) {
        super(dbClient, type, tracker, gcDelayMins, coordinator);

        geoDependencyChecker = new GeoDependencyChecker(dbClient, coordinator, dependencyChecker);
    }

    @Override
    protected boolean canBeGC(URI id) {
        String dependency = geoDependencyChecker.checkDependencies(id, type, true);

        if (dependency != null) {
            log.debug("Geo object {} has dependencies on {}", dependency);
        }

        return dependency == null;
    }
}
