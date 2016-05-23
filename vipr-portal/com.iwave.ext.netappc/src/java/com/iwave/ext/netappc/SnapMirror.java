/*
 * Copyright (c) 2012-2016 iWave Software LLC
 * All Rights Reserved
 */
package com.iwave.ext.netappc;

import java.util.Iterator;
import java.util.List;

import netapp.manage.NaElement;
import netapp.manage.NaServer;

import org.apache.log4j.Logger;

import com.iwave.ext.netapp.NetAppException;
import com.iwave.ext.netappc.model.SnapMirrorVolumeStatus;
import com.iwave.ext.netappc.model.SnapmirrorCreateParam;
import com.iwave.ext.netappc.model.SnapmirrorInfo;
import com.iwave.ext.netappc.model.SnapmirrorInfoResp;
import com.iwave.ext.netappc.model.SnapmirrorRelationshipStatus;
import com.iwave.ext.netappc.model.SnapmirrorResp;
import com.iwave.ext.netappc.model.SnapmirrorState;
import com.iwave.ext.netappc.model.SnapmirrorTransferType;

/**
 * Snapmirror management operations
 */

public class SnapMirror {
    private Logger log = Logger.getLogger(getClass());

    private String name = "";
    private NaServer server = null;

    public SnapMirror(NaServer server, String volumeName) {
        this.server = server;
        this.name = volumeName;
    }

    /**
     * creates a SnapMirror relationship between a source and destination volumes
     * 
     * @param snapMirrorCreateParam
     * @return
     */
    public SnapmirrorInfoResp createSnapMirror(SnapmirrorCreateParam snapMirrorCreateParam) {

        SnapmirrorInfoResp snapMirrorResp = null;

        NaElement elem = new NaElement("snapmirror-create");

        // source attributes
        elem.addNewChild("source-location", snapMirrorCreateParam.getSourceLocation());

        // destination attributes
        elem.addNewChild("destination-location", snapMirrorCreateParam.getDestinationLocation());

        // the name of the cron schedule
        elem.addNewChild("schedule", snapMirrorCreateParam.getCronScheduleName());

        if (snapMirrorCreateParam.isReturnRecord() == true) {
            elem.addNewChild("return-record", "true");
        }

        // type of the SnapMirror relationship
        elem.addNewChild("relationship-type", snapMirrorCreateParam.getRelationshipType());

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to create SnapMirror: " + snapMirrorCreateParam.getDestinationVolume();
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return snapMirrorResp;
    }

    public boolean initialiseSnapMirror(String destLocation) {
        NaElement elem = new NaElement("snapmirror-initialize");

        // destination attributes
        elem.addNewChild("destination-location", destLocation);

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Initialize SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    public SnapmirrorResp resyncSnapMirror(SnapmirrorInfo snapMirrorInfo) {
        SnapmirrorResp snapMirrorResp = new SnapmirrorResp();

        return snapMirrorResp;
    }

    public SnapmirrorResp restoreSnapMirror(SnapmirrorInfo snapMirrorInfo) {
        SnapmirrorResp snapMirrorResp = new SnapmirrorResp();

        return snapMirrorResp;
    }

    public boolean breakSnapMirror(String destLocation, String relationShipId) {

        NaElement elem = new NaElement("snapmirror-break");

        // destination attributes
        elem.addNewChild("destination-location", destLocation);

        if (relationShipId != null && relationShipId.isEmpty()) {
            elem.addNewChild("relationship-id", relationShipId);
        }

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Break SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    public boolean quiesceSnapMirror(String destLocation) {
        NaElement elem = new NaElement("snapmirror-quiesce");

        // destination attributes
        elem.addNewChild("destination-location", destLocation);

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Quiesce SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;

    }

    public boolean resumesnapMirror(String destLocation) {
        NaElement elem = new NaElement("snapmirror-resume");
        // destination attributes
        elem.addNewChild("destination-location", destLocation);

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Resume SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    public SnapmirrorResp destroyAsyncSnapMirror(SnapmirrorInfo snapMirrorInfo) {
        SnapmirrorResp snapMirrorResp = null;

        NaElement elem = new NaElement("snapmirror-destroy-async");

        // destination attributes
        prepSourceReq(elem, snapMirrorInfo);

        // source attributes
        prepDestReq(elem, snapMirrorInfo);

        try {
            NaElement results = server.invokeElem(elem);
            snapMirrorResp = parseRespJobResult(results);

        } catch (Exception e) {
            String msg = "Failed to Destory Async SnapMirror: " + snapMirrorInfo.getDestinationVolume();
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return snapMirrorResp;
    }

    public boolean destroySnapMirror(String destLocation) {
        NaElement elem = new NaElement("snapmirror-destroy");

        // destination attributes
        elem.addNewChild("destination-location", destLocation);

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Destroy SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    public boolean releaseSnapMirror(String destLocation) {
        NaElement elem = new NaElement("snapmirror-destroy");

        // destination attributes
        elem.addNewChild("destination-location", destLocation);
        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Release SnapMirror: " + destLocation;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    /**
     * get the snapmirror relation ship details info
     * 
     * @param snapMirrorInfo
     * @return
     */
    public SnapmirrorInfoResp getSnapMirrorInfo(String destPath) {
        SnapmirrorInfoResp snapMirrorResp = null;

        NaElement elem = new NaElement("snapmirror-get");

        // destination path
        elem.addNewChild("destination-location", destPath);

        try {
            NaElement results = server.invokeElem(elem);

            snapMirrorResp = parseSnapMirrorRelationShipInfo(results);

        } catch (Exception e) {
            String msg = "Failed to get snapMirror info: " + destPath;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return snapMirrorResp;

    }

    /**
     * get the snapmirror relation ship details info
     * 
     * @param snapMirrorInfo
     * @return
     */
    public SnapmirrorInfoResp getSnapMirrorDestInfo(SnapmirrorInfo snapMirrorInfo) {
        SnapmirrorInfoResp snapMirrorResp = null;

        NaElement elem = new NaElement("snapmirror-get-destination");

        // destination attributes
        prepSourceReq(elem, snapMirrorInfo);

        // source attributes
        prepDestReq(elem, snapMirrorInfo);

        try {
            NaElement results = server.invokeElem(elem);

            snapMirrorResp = parseSnapMirrorRelationShipInfo(results);

        } catch (Exception e) {
            String msg = "Failed to get SnapMirror Destination Info: " + snapMirrorInfo.getDestinationVolume();
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return snapMirrorResp;

    }

    public SnapMirrorVolumeStatus getSnapMirrorVolumeStatus(String volume) {

        NaElement elem = new NaElement("snapmirror-get-volume-status");

        elem.addNewChild("volume", volume);

        SnapMirrorVolumeStatus snapMirrorVolumeStatus = null;

        try {
            NaElement results = server.invokeElem(elem);
            if (results != null) {
                snapMirrorVolumeStatus = new SnapMirrorVolumeStatus();
                // is-destination
                String isDest = results.getChildContent("is-destination");
                if (isDest != null && isDest.isEmpty()) {
                    if (isDest.equalsIgnoreCase("true")) {
                        snapMirrorVolumeStatus.setDestination(true);
                    } else {
                        snapMirrorVolumeStatus.setDestination(false);
                    }
                }

                // is-source
                String isSource = results.getChildContent("is-source");
                if (isSource != null && !isSource.isEmpty()) {
                    if (isSource.equalsIgnoreCase("true")) {
                        snapMirrorVolumeStatus.setSource(true);
                    } else {
                        snapMirrorVolumeStatus.setSource(false);
                    }
                }

                // is-transfer-broken
                String isBroken = results.getChildContent("is-transfer-broken");
                if (isBroken != null && !isBroken.isEmpty()) {
                    if (isBroken.equalsIgnoreCase("true")) {
                        snapMirrorVolumeStatus.setTransferBroken(true);
                    } else {
                        snapMirrorVolumeStatus.setTransferBroken(false);
                    }
                }

                // is-transfer-in-progress
                String transferProgress = results.getChildContent("is-transfer-in-progress");
                if (transferProgress != null && !transferProgress.isEmpty()) {
                    if (transferProgress.equalsIgnoreCase("true")) {
                        snapMirrorVolumeStatus.setTransferInProgress(true);
                    } else {
                        snapMirrorVolumeStatus.setTransferInProgress(false);
                    }
                }
            }

        } catch (Exception e) {
            String msg = "Failed to SnapMirror Volume Status: " + volume;
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return snapMirrorVolumeStatus;
    }

    public boolean abortSnapMirror(SnapmirrorInfo snapMirrorInfo) {
        NaElement elem = new NaElement("snapmirror-abort");

        // destination attributes
        prepSourceReq(elem, snapMirrorInfo);

        // source attributes
        prepDestReq(elem, snapMirrorInfo);

        try {
            server.invokeElem(elem);
        } catch (Exception e) {
            String msg = "Failed to Abort SnapMirror: " + snapMirrorInfo.getDestinationVolume();
            log.error(msg, e);
            throw new NetAppCException(msg, e);
        }
        return true;
    }

    /**
     * Checks if license exists for SnapMirror
     * 
     * @return true if license exists for SnapMirror; false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean checkLicense() {

        boolean licenseValid = false;
        NaElement elem = new NaElement("license-v2-status-list-info");
        try {
            List<NaElement> licences = server.invokeElem(elem).getChildByName("license-v2-status").getChildren();
            if (licences != null) {
                for (Iterator<NaElement> iterator = licences.iterator(); iterator.hasNext();) {
                    NaElement licenceElement = iterator.next();
                    String packageStr = licenceElement.getChildByName("package").getContent();
                    String methodStr = licenceElement.getChildByName("method").getContent();
                    if ("snapmirror".equals(packageStr) && "license".equals(methodStr)) {
                        licenseValid = true;
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Failed to check snapmirror license-v2 status.";
            log.error(msg, e);
            throw new NetAppException(msg, e);
        }
        return licenseValid;
    }

    // helper function

    private void prepSourceReq(NaElement elem, SnapmirrorInfo snapMirrorInfo) {
        if (elem != null && snapMirrorInfo.getSourceCluster() != null) {
            elem.addNewChild("source-cluster", snapMirrorInfo.getSourceCluster());
        }

        if (elem != null && snapMirrorInfo.getSourceLocation() != null) {
            elem.addNewChild("source-location", snapMirrorInfo.getSourceLocation());
        }

        if (elem != null && snapMirrorInfo.getSourceVolume() != null) {
            elem.addNewChild("source-volume", snapMirrorInfo.getSourceVolume());
        }

        if (elem != null && snapMirrorInfo.getSourceVserver() != null) {
            elem.addNewChild("source-vserver", snapMirrorInfo.getSourceVserver());
        }
    }

    private void prepDestReq(NaElement elem, SnapmirrorInfo snapMirrorInfo) {
        if (elem != null && snapMirrorInfo.getDestinationCluster() != null) {
            elem.addNewChild("destination-cluster", snapMirrorInfo.getDestinationCluster());
        }

        if (elem != null && snapMirrorInfo.getDestinationVolume() != null) {
            elem.addNewChild("destination-volume", snapMirrorInfo.getDestinationVolume());
        }

        if (elem != null && snapMirrorInfo.getDestinationLocation() != null) {
            elem.addNewChild("destination-location", snapMirrorInfo.getDestinationLocation());
        }

        if (elem != null && snapMirrorInfo.getDestinationVserver() != null) {
            elem.addNewChild("destination-vserver", snapMirrorInfo.getDestinationVserver());
        }
    }

    private SnapmirrorResp parseRespJobResult(NaElement results) {
        SnapmirrorResp snapMirrorResp = new SnapmirrorResp();
        // set result status
        String status = results.getChildContent("result-status");
        snapMirrorResp.setResultStatus(status);
        // set job
        Integer jobId = results.getChildIntValue("result-jobid", -1);
        snapMirrorResp.setResultJobid(jobId);

        if (status.equals("failed")) {
            // set messageId
            Integer errorCode = results.getChildIntValue("result-error-code", -1);
            snapMirrorResp.setResultErrorCode(errorCode);

            // set result message
            String message = results.getChildContent("result-error-message");

            if (message != null && !message.isEmpty()) {
                snapMirrorResp.setResultErrorMessage(message);
            }
        }
        return snapMirrorResp;
    }

    private SnapmirrorInfoResp parseSnapMirrorRelationShipInfo(NaElement resultElem) {
        SnapmirrorInfoResp snapMirrorResp = new SnapmirrorInfoResp();

        if (resultElem != null) {
            NaElement results = resultElem.getChildByName("attributes").getChildByName("snapmirror-info");

            // relationship-id
            String relationShipId = results.getChildContent("relationship-id");
            if (relationShipId != null && !relationShipId.isEmpty()) {
                snapMirrorResp.setRelationshipId(relationShipId);
            }

            String relationStatus = results.getChildContent("relationship-status");
            // relationship-status
            if (relationStatus != null && !relationStatus.isEmpty()) {
                snapMirrorResp.setRelationshipStatus(SnapmirrorRelationshipStatus.valueOf(relationStatus));
            }

            // mirror-state
            String mirrorStatus = results.getChildContent("mirror-state");
            if (mirrorStatus != null && !mirrorStatus.isEmpty()) {
                SnapmirrorState currentMirrorStatus = SnapmirrorState.valueOfLabel(mirrorStatus);
                snapMirrorResp.setMirrorState(currentMirrorStatus);
            }

            // current-transfer-type
            String currentTransferType = results.getChildContent("current-transfer-type");

            if (currentTransferType != null && !currentTransferType.isEmpty()) {
                snapMirrorResp.setCurrentTransferType(SnapmirrorTransferType.valueOf(currentTransferType));
            }

            String currentTransferError = results.getChildContent("current-transfer-error");

            if (currentTransferError != null && !currentTransferError.isEmpty()) {
                snapMirrorResp.setCurrentTransferError(currentTransferError);
            }

            // last-transfer-type
            String lastTransferType = results.getChildContent("last-transfer-type");

            if (lastTransferType != null && !mirrorStatus.isEmpty()) {
                snapMirrorResp.setLastTransferType(SnapmirrorTransferType.valueOf(lastTransferType));
            }

            // schedule
            String scheduleName = results.getChildContent("schedule");
            if (scheduleName != null && !scheduleName.isEmpty()) {
                snapMirrorResp.setScheduleName(scheduleName);
            }

            // source details

            // source-volume
            String sourceVolume = results.getChildContent("source-volume");

            if (sourceVolume != null && !sourceVolume.isEmpty()) {
                snapMirrorResp.setSourceVolume(sourceVolume);
            }

            // source-vserver
            String sourceVServer = results.getChildContent("source-vserver");

            if (sourceVServer != null && !sourceVServer.isEmpty()) {
                snapMirrorResp.setSourceVserver(sourceVServer);
            }

            // source-cluster
            String sourceCluster = results.getChildContent("source-cluster");

            if (sourceCluster != null && !sourceCluster.isEmpty()) {
                snapMirrorResp.setSourceCluster(sourceCluster);
            }

            // source-location
            String sourceLocation = results.getChildContent("source-location");

            if (sourceLocation != null && !sourceLocation.isEmpty()) {
                snapMirrorResp.setSourceLocation(sourceLocation);
            }
            // target details

            // destination-volume
            String destinationVolume = results.getChildContent("destination-volume");

            if (destinationVolume != null && !destinationVolume.isEmpty()) {
                snapMirrorResp.setDestinationVolume(destinationVolume);
            }
            // destination-vserver
            String destinationVServer = results.getChildContent("destination-vserver");
            if (destinationVServer != null && !destinationVServer.isEmpty()) {
                snapMirrorResp.setDestinationVserver(destinationVServer);
            }
            // destination-cluster
            String destinationCluster = results.getChildContent("destination-cluster");
            if (destinationCluster != null && !destinationCluster.isEmpty()) {
                snapMirrorResp.setDestinationCluster(destinationCluster);
            }
            // destination-location
            String destinationLocation = results.getChildContent("destination-location");
            if (destinationLocation != null && !destinationLocation.isEmpty()) {
                snapMirrorResp.setDestinationLocation(destinationLocation);
            }
        }
        return snapMirrorResp;
    }
}