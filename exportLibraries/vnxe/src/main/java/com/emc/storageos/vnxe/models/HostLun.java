/*
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.vnxe.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostLun extends VNXeBase {
    private VNXeBase host;
    private int type;
    private int hlu;
    private VNXeBase lun;
    private VNXeBase lunSnap;
    private boolean isReadOnly;

    public VNXeBase getHost() {
        return host;
    }

    public void setHost(VNXeBase host) {
        this.host = host;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHlu() {
        return hlu;
    }

    public void setHlu(int hlu) {
        this.hlu = hlu;
    }

    public VNXeBase getLun() {
        return lun;
    }

    public void setLun(VNXeBase lun) {
        this.lun = lun;
    }

    public VNXeBase getLunSnap() {
        return lunSnap;
    }

    public void setLunSnap(VNXeBase lunSnap) {
        this.lunSnap = lunSnap;
    }

    public boolean getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public static enum HostLUNTypeEnum {
        LUN_SNAP(1),
        LUN(2);

        private int value;

        private HostLUNTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
