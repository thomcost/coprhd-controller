/*
 * Copyright (c) 2012-2015 iWave Software LLC
 * All Rights Reserved
 */
package com.emc.sa.service.linux;

import java.util.List;

import com.emc.sa.engine.ExecutionUtils;
import com.emc.sa.engine.bind.Bindable;
import com.emc.sa.engine.service.Service;
import com.emc.sa.service.vipr.block.CreateBlockVolumeHelper;
import com.emc.storageos.model.block.BlockObjectRestRep;
import com.emc.storageos.model.block.VolumeRestRep;
import com.google.common.collect.Lists;

@Service("Linux-CreateAndMountBlockVolume")
public class CreateAndMountVolumeService extends LinuxService {
    @Bindable
    private CreateBlockVolumeHelper createVolumeHelper = new CreateBlockVolumeHelper();

    protected MountBlockVolumeHelper mountBlockVolumeHelper;

    @Override
    public void init() throws Exception {
        super.init();
        mountBlockVolumeHelper = MountBlockVolumeHelper.createHelper(linuxSystem, hostPorts);
    }

    @Override
    public void precheck() throws Exception {
        super.precheck();
        createVolumeHelper.precheck();
        mountBlockVolumeHelper.precheck();
    }

    @Override
    public void execute() throws Exception {
        BlockObjectRestRep volume = createVolume();
        acquireHostsLock();
        refreshStorage(volume);
        mountVolume(volume);
    }

    private BlockObjectRestRep createVolume() {
        List<VolumeRestRep> volumes = createVolumeHelper.createAndExportVolumes();
        if (volumes.size() != 1) {
            ExecutionUtils.fail("failTask.linux.createAndMount", args(volumes.size()));
        }
        return volumes.get(0);
    }

    private void refreshStorage(BlockObjectRestRep volume) {
        mountBlockVolumeHelper.refreshStorage(Lists.newArrayList(volume));
    }

    private void mountVolume(BlockObjectRestRep volume) {
        mountBlockVolumeHelper.mountVolume(volume);
    }
}
