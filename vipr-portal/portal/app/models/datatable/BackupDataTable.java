/*
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 */
package models.datatable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


import play.Logger;
import util.BackupUtils;
import util.datatable.DataTable;

import com.emc.vipr.model.sys.backup.BackupSets.BackupSet;
import com.emc.vipr.model.sys.backup.BackupUploadStatus.Status;
import com.google.common.collect.Lists;

public class BackupDataTable extends DataTable {
    private static final int MINIMUM_PROGRESS = 10;

    public enum Type {
        LOCAL, REMOTE
    }

    public BackupDataTable() {
        setupTable(Type.LOCAL);
    }

    public BackupDataTable(Type type) {
        setupTable(type);
    }

    private void setupTable(Type type) {
        addColumn("name");
        addColumn("creationtime").setCssClass("time").setRenderFunction(
                "render.localDate");
        if (type == Type.LOCAL) {
            addColumn("size");
        }
        addColumn("actionstatus").setSearchable(false).setRenderFunction(
                "render.uploadProgress");
        addColumn("action").setSearchable(false).setRenderFunction(
                "render.uploadBtn");
        sortAllExcept("action", "actionstatus");
        setDefaultSort("name", "asc");
        setRowCallback("createRowLink");
    }

	public static List<Backup> fetch() {
		List<Backup> results = Lists.newArrayList();
		for (BackupSet backup : BackupUtils.getBackups()) {
			results.add(new Backup(backup));
		}
		return results;
	}

	public static class Backup {
		public String name;
		public long creationtime;
		public long size;
		public String id;
		public String upload;
		public String status;
		public String error;
		public Integer progress = 0;

		public Backup(BackupSet backup) {
			try {
			    id = URLEncoder.encode(backup.getName(), "UTF-8");
			} catch(UnsupportedEncodingException e) {
			    Logger.error("Could not encode backup name");
			}
			name = backup.getName();
			creationtime = backup.getCreateTime();
			size = backup.getSize();
			status = backup.getUploadStatus().getStatus().name();
			if(backup.getUploadStatus().getProgress()!=null){
				progress = Math.max(backup.getUploadStatus().getProgress(), MINIMUM_PROGRESS);
			}
			if(backup.getUploadStatus().getErrorCode()!=null){
				error = backup.getUploadStatus().getErrorCode().name();
			}
			if(status.equals(Status.FAILED.toString())){
				progress = 100;
			}
			if (status.equals(Status.NOT_STARTED.toString())
					|| status.equals(Status.FAILED.toString())) {
				upload = backup.getName() + "_enable";
			} else {
				upload = backup.getName() + "_disable";
			}
		}

	}

}
