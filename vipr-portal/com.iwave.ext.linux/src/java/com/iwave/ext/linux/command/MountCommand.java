/*
 * Copyright (c) 2012-2015 iWave Software LLC
 * All Rights Reserved
 */
package com.iwave.ext.linux.command;

/**
 * Executes the linux Mount command.
 * 
 * @author Chris Dail
 */
public class MountCommand extends LinuxCommand {
    public MountCommand() {
        setCommand(CommandConstants.MOUNT);
        setRunAsRoot(true);
    }

    public void setPath(String path) {
        addArgument(path);
    }

    public void setMountAll() {
        addArgument("-a");
    }
}
