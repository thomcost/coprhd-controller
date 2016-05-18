/*
 * Copyright 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.emc.storageos.keystone.restapi.model.response;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Keystone API get Tenants response class.
 */
@XmlRootElement(name = "openstack_tenants")
public class TenantListRestResp {

    List<TenantV2> openstack_tenants;

    @XmlElementRef
    public List<TenantV2> getOpenstack_tenants() {
        if (openstack_tenants == null) {
            openstack_tenants = new ArrayList<>();
        }
        return openstack_tenants;
    }

    public void setOpenstack_tenants(List<TenantV2> openstack_tenants) {
        this.openstack_tenants = openstack_tenants;
    }
}