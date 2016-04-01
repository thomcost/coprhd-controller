package com.iwave.ext.netapp.model;

public enum SnapMirrorTransferStatus {

    idle("idle"),
    transferring("transferring"),
    pending("pending"),
    aborting("aborting"),
    migrating("migrating"),
    quiescing("quiescing"),
    resyncing("resyncing"),
    syncing("syncing"),
    insync("in-sync"),
    paused("paused"),
    scheduled("scheduled"),
    retry("retry"),
    none("-");

    private String label;

    SnapMirrorTransferStatus(String label) {
        this.label = label;
    }

    public static SnapMirrorTransferStatus valueOfLabel(String label) {
        for (SnapMirrorTransferStatus t : values()) {
            if (label.equals(t.label))
                return t;
        }
        throw new IllegalArgumentException(label + " is not a valid label for SnapMirror Transfer Status");
    }

    @Override
    public String toString() {
        return label;
    }
}