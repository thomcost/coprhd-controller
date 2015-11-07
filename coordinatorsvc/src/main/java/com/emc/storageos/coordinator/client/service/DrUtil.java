/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.coordinator.client.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.coordinator.client.model.Constants;
import com.emc.storageos.coordinator.client.model.Site;
import com.emc.storageos.coordinator.client.model.SiteInfo;
import com.emc.storageos.coordinator.client.service.impl.CoordinatorClientImpl;
import com.emc.storageos.coordinator.common.Configuration;
import com.emc.storageos.coordinator.common.Service;
import com.emc.storageos.coordinator.exceptions.CoordinatorException;
import com.emc.storageos.coordinator.exceptions.RetryableCoordinatorException;
import com.emc.storageos.svcs.errorhandling.resources.ServiceCode;

/**
 * Common utility functions for Disaster Recovery
 */
public class DrUtil {
    private static final Logger log = LoggerFactory.getLogger(DrUtil.class);
    
    private CoordinatorClient coordinator;

    public DrUtil(CoordinatorClient coordinator) {
        this.coordinator = coordinator;
    }
    
    public CoordinatorClient getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(CoordinatorClient coordinator) {
        this.coordinator = coordinator;
    }
    
    /**
     * Check if current site is primary
     * 
     * @return true for primary. otherwise false
     */
    public boolean isPrimary() {
        return getPrimarySiteId().equals(coordinator.getSiteId());
    }
    
    /**
     * Check if current site is a standby site
     * 
     * @return true for standby site. otherwise false
     */
    public boolean isStandby() {
        return !isPrimary();
    }
    
    /**
     * Get primary site in current vdc
     * 
     * @return
     */
    public String getPrimarySiteId() {
        Configuration config = coordinator.queryConfiguration(Constants.CONFIG_DR_PRIMARY_KIND, Constants.CONFIG_DR_PRIMARY_ID);
        return config.getConfig(Constants.CONFIG_DR_PRIMARY_SITEID);
    }
    
    /**
     * Load site information from coordinator 
     * 
     * @param siteId
     * @return
     */
    public Site getSite(String siteId) {
        Configuration config = coordinator.queryConfiguration(Site.CONFIG_KIND, siteId);
        if (config != null) {
            return new Site(config);
        }
        throw CoordinatorException.retryables.cannotFindSite(siteId);
    }
    
    
    /**
     * List all standby sites in current vdc
     * 
     * @return list of standby sites
     */
    public List<Site> listStandbySites() {
        String primaryId = this.getPrimarySiteId();
        List<Site> result = new ArrayList<Site>();
        for(Site site : listSites()) {
            if (!site.getUuid().equals(primaryId)) {
                result.add(site);
            }
        }
        return result;
    }

    /**
     * List all sites in current vdc
     * 
     * @return list of all sites
     */
    public List<Site> listSites() {
        Site primarySite = getSite(getPrimarySiteId());
        String vdcId = primarySite.getVdcShortId();
        List<Site> result = new ArrayList<>();
        for(Configuration config : coordinator.queryAllConfiguration(Site.CONFIG_KIND)) {
            Site site = new Site(config);
            if (site.getVdcShortId().equals(vdcId)) {
                result.add(site);
            }
        }
        return result;
    }
    
    /**
     * Get number of running services in given site
     * 
     * @return number to indicate servers 
     */
    public int getNumberOfLiveServices(String siteUuid, String svcName, String svcVersion) {
        try {
            List<Service> svcs = coordinator.locateAllServices(siteUuid, svcName, svcVersion, null, null);
            return svcs.size();
        } catch (RetryableCoordinatorException ex) {
            if (ex.getServiceCode() == ServiceCode.COORDINATOR_SVC_NOT_FOUND) {
                return 0;
            }
            throw ex;
        }
    }
    
    /**
     * Check if site is up and running
     * 
     * @param siteId
     * @return true if any syssvc is running on this site
     */
    public boolean isSiteUp(String siteId) {
        // Get service beacons for given site - - assume syssvc on all sites share same service name in beacon
        try {
            String syssvcName = ((CoordinatorClientImpl)coordinator).getSysSvcName();
            String syssvcVersion = ((CoordinatorClientImpl)coordinator).getSysSvcVersion();
            List<Service> svcs = coordinator.locateAllServices(siteId, syssvcName, syssvcVersion,
                    (String) null, null);

            List<String> nodeList = new ArrayList<String>();
            for(Service svc : svcs) {
                nodeList.add(svc.getNodeId());
            }
            log.info("Site {} is up. active nodes {}", siteId, StringUtils.join(nodeList, ","));
            return true;
        } catch (CoordinatorException ex) {
            if (ex.getServiceCode() == ServiceCode.COORDINATOR_SVC_NOT_FOUND) {
                return false; // no service beacon found for given site
            }
            log.error("Unexpected error when checking site service becons", ex);
            return true;
        }
    }
    
    /**
     * Update SiteInfo's action and version for specified site id 
     * @param siteId site UUID
     * @param action action to take
     */
    public void updateVdcTargetVersion(String siteId, String action) throws Exception {
        SiteInfo siteInfo;
        SiteInfo currentSiteInfo = coordinator.getTargetInfo(siteId, SiteInfo.class);
        if (currentSiteInfo != null) {
            siteInfo = new SiteInfo(System.currentTimeMillis(), action, currentSiteInfo.getTargetDataRevision());
        } else {
            siteInfo = new SiteInfo(System.currentTimeMillis(), action);
        }
        coordinator.setTargetInfo(siteId, siteInfo);
        log.info("VDC target version updated to {} for site {}", siteInfo.getVdcConfigVersion(), siteId);
    }

    /**
     * Check if a specific site is the local site
     * @param site
     * @return true if the specified site is the local site
     */
    public boolean isLocalSite(Site site) {
        return site.getUuid().equals(coordinator.getSiteId());
    }
    
    /**
     * Generate Cassandra data center name for given site.
     * 
     * @param site
     * @return
     */
    public String getCassandraDcId(Site site) {
        String dcId = null;
        if (site.getVdcShortId().equals(site.getStandbyShortId())) {
            dcId = site.getVdcShortId();
        } else {
            dcId = site.getUuid();
        }

        log.info("Cassandra DC Name is {}", dcId);
        return dcId;
    }
    
    /**
     * Get current site information
     * @return Site current Site
     */
    public Site getCurrentSite() {
        return this.getSite(coordinator.getSiteId());
    }
}
