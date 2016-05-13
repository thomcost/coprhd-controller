/*
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.volumecontroller.impl.netappc.job;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.storageos.exceptions.DeviceControllerErrors;
import com.emc.storageos.netapp.NetAppApi;
import com.emc.storageos.svcs.errorhandling.model.ServiceError;
import com.emc.storageos.volumecontroller.Job;
import com.emc.storageos.volumecontroller.JobContext;
import com.emc.storageos.volumecontroller.TaskCompleter;
import com.emc.storageos.volumecontroller.impl.JobPollResult;

public class NetAppSnapMirrorAbortJob extends Job implements Serializable {

    private static final Logger _logger = LoggerFactory.getLogger(NetAppSnapMirrorCreateJob.class);

    private static final long ERROR_TRACKING_LIMIT = 60 * 1000; // tracking limit for transient errors. set for 2 hours

    private String _jobName;
    private URI _storageSystemUri;
    private TaskCompleter _taskCompleter;
    private List<String> _jobIds = new ArrayList<String>();

    private long _error_tracking_time = 0L;
    private JobStatus _status = JobStatus.IN_PROGRESS;

    private JobPollResult _pollResult = new JobPollResult();
    private String _errorDescription = null;

    public NetAppSnapMirrorAbortJob(String jobId, URI storageSystemUri, TaskCompleter taskCompleter, String jobName) {
        this._storageSystemUri = storageSystemUri;
        this._taskCompleter = taskCompleter;
        this._jobName = jobName;
        this._jobIds.add(jobId);
    }

    public NetAppSnapMirrorAbortJob(String jobId, URI storageSystemUri, TaskCompleter taskCompleter) {
        this._storageSystemUri = storageSystemUri;
        this._taskCompleter = taskCompleter;
        this._jobName = "netAppSnapMirrorStartJob";
        this._jobIds.add(jobId);
    }

    @Override
    public JobPollResult poll(JobContext jobContext, long trackingPeriodInMillis) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskCompleter getTaskCompleter() {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateStatus(JobContext jobContext) throws Exception {
        if (_status == JobStatus.SUCCESS) {
            _taskCompleter.ready(jobContext.getDbClient());
        } else if (_status == JobStatus.FAILED || _status == JobStatus.FATAL_ERROR) {
            ServiceError error = DeviceControllerErrors.isilon.jobFailed(_errorDescription);
            _taskCompleter.error(jobContext.getDbClient(), error);
        }
    }

    public void setErrorStatus(String errorDescription) {
        _status = JobStatus.FATAL_ERROR;
        _errorDescription = errorDescription;
    }

    public void setErrorTrackingTime(long trackingTime) {
        _error_tracking_time = trackingTime;
    }

    private void processTransientError(String jobId, long trackingInterval, String errorMessage, Exception ex) {
        _status = JobStatus.ERROR;
        _errorDescription = errorMessage;
        if (ex != null) {
            _logger.error(String.format("Error while processing SnapMirror Job - Name: %s, ID: %s, Desc: %s Status: %s",
                    _jobName, jobId, _errorDescription, _status), ex);
        } else {
            _logger.error(String.format("Error while processing SnapMirror - Name: %s, ID: %s, Desc: %s Status: %s",
                    _jobName, jobId, _errorDescription, _status));
        }

        // Check if job tracking limit was reached. Set status to FAILED in such a case.
        setErrorTrackingTime(_error_tracking_time + trackingInterval);
        _logger.info(String.format("Tracking time of SnapMirror in transient error status - %s, Name: %s, ID: %s. Status %s .",
                _error_tracking_time, _jobName, jobId, _status));
        if (_error_tracking_time > ERROR_TRACKING_LIMIT) {
            _status = JobStatus.FATAL_ERROR;
            _logger.error(String.format("Reached tracking time limit for SnapMirror - Name: %s, ID: %s. Set status to %s .",
                    _jobName, jobId, _status));
        }
    }

    /**
     * Get NetAppC Mode API client
     * 
     * @param jobContext
     * @return
     */
    public NetAppApi getNetappCApi(JobContext jobContext) {
        return null;
    }

}