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
package com.emc.storageos.api.service.impl.resource;

import com.emc.storageos.api.mapper.DbObjectMapper;
import com.emc.storageos.api.service.impl.resource.utils.OpenStackSynchronizationTask;
import com.emc.storageos.db.client.URIUtil;
import com.emc.storageos.db.client.model.AuthnProvider;
import com.emc.storageos.db.client.model.DataObject;
import com.emc.storageos.db.client.model.OSTenant;
import com.emc.storageos.keystone.restapi.KeystoneApiClient;
import com.emc.storageos.keystone.restapi.model.response.TenantListRestResp;
import com.emc.storageos.keystone.restapi.model.response.TenantV2;
import com.emc.storageos.keystone.restapi.utils.KeystoneUtils;
import com.emc.storageos.model.ResourceTypeEnum;
import com.emc.storageos.model.keystone.CoprhdOsTenant;
import com.emc.storageos.model.keystone.CoprhdOsTenantListRestRep;
import com.emc.storageos.model.keystone.OpenStackTenantListParam;
import com.emc.storageos.model.keystone.OpenStackTenantParam;
import com.emc.storageos.security.authentication.StorageOSUser;
import com.emc.storageos.security.authorization.CheckPermission;
import com.emc.storageos.security.authorization.DefaultPermissions;
import com.emc.storageos.security.authorization.Role;
import com.emc.storageos.svcs.errorhandling.resources.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * API for manipulating OpenStack Keystone service.
 */
@Path("/v2/keystone")
@DefaultPermissions(readRoles = { Role.SECURITY_ADMIN }, writeRoles = { Role.SECURITY_ADMIN })
public class KeystoneService extends TaskResourceService {

    private static final Logger _log = LoggerFactory.getLogger(KeystoneService.class);

    private KeystoneUtils _keystoneUtils;
    private OpenStackSynchronizationTask _openStackSynchronizationTask;
    private AuthnConfigurationService _authService;

    public void setAuthService(AuthnConfigurationService authService) {
        this._authService = authService;
    }

    public void setOpenStackSynchronizationTask(OpenStackSynchronizationTask openStackSynchronizationTask) {
        this._openStackSynchronizationTask = openStackSynchronizationTask;
    }

    public void setKeystoneUtils(KeystoneUtils keystoneUtils) {
        this._keystoneUtils = keystoneUtils;
    }

    /**
     * Get a list of OpenStack Tenants.
     * Uses data from Keystone Authentication Provider to connect Keystone and retrieve Tenants information.
     *
     * @brief Show OpenStack Tenants.
     * @return OpenStack Tenants details.
     * @see TenantListRestResp
     */
    @GET
    @Path("/tenants")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public TenantListRestResp getOpenstackTenants() {

        _log.debug("Keystone Service - getOpenstackTenants");

        StorageOSUser user = getUserFromContext();
        if (!_permissionsHelper.userHasGivenRoleInAnyTenant(user, Role.SECURITY_ADMIN, Role.TENANT_ADMIN)) {
            throw APIException.forbidden.insufficientPermissionsForUser(user.getName());
        }

        AuthnProvider keystoneProvider = _keystoneUtils.getKeystoneProvider();

        // Get OpenStack Tenants only when Keystone Provider exists.
        if (keystoneProvider != null) {

            KeystoneApiClient keystoneApiClient = _keystoneUtils.getKeystoneApi(keystoneProvider.getManagerDN(),
                    keystoneProvider.getServerUrls(), keystoneProvider.getManagerPassword());

            List<TenantV2> OSTenantList = new ArrayList<>(Arrays.asList(keystoneApiClient.getKeystoneTenants().getTenants()));

            TenantListRestResp response = new TenantListRestResp();
            response.setOpenstack_tenants(OSTenantList);

            return response;
        }

        throw APIException.internalServerErrors.targetIsNullOrEmpty("Keystone Authentication Provider");
    }

    /**
     * Get OpenStack Tenant with given ID.
     * Uses data from Keystone Authentication Provider to connect Keystone and retrieve Tenant information.
     *
     * @param id OpenStack Tenant ID.
     * @brief Show OpenStack Tenant.
     * @return OpenStack Tenant details.
     * @see TenantListRestResp
     */
    @GET
    @Path("/tenants/{id}")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public OpenStackTenantParam getOpenstackTenant(@PathParam("id") URI id) {

        _log.debug("Keystone Service - getOpenstackTenant with id: {}", id.toString());

        List<TenantV2> tenants = getOpenstackTenants().getOpenstack_tenants();

        for (TenantV2 tenant : tenants) {
            if (tenant.getId().equals(id.toString())) {
                return mapToOpenstackParam(tenant);
            }
        }

        throw APIException.internalServerErrors.targetIsNullOrEmpty("Openstack Tenant");
    }

    /**
     * Creates representation of OpenStack Tenants in CoprHD.
     *
     * @param param OpenStackTenantListParam OpenStack Tenants representation with all necessary elements.
     * @brief Creates representation of OpenStack Tenants in CoprHD.
     * @return Newly created Tenants.
     * @see
     */
    @POST
    @Path("/tenants")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @CheckPermission(roles = { Role.SECURITY_ADMIN })
    public CoprhdOsTenantListRestRep saveOpenstackTenants(OpenStackTenantListParam param) {

        List<OSTenant> openstackTenants = new ArrayList<>();

        for (OpenStackTenantParam openStackTenantParam : param.getOpenstack_tenants()) {
            openstackTenants.add(prepareOpenstackTenant(openStackTenantParam));
        }

        if (!openstackTenants.isEmpty()) {
            _dbClient.createObject(openstackTenants);
        }

        AuthnProvider keystoneProvider = _keystoneUtils.getKeystoneProvider();

        if (keystoneProvider == null) {
            throw APIException.internalServerErrors.targetIsNullOrEmpty("Keystone Authentication Provider");
        }

        if (keystoneProvider.getAutoRegCoprHDNImportOSProjects()) {

            if (_openStackSynchronizationTask.getSynchronizationTask() == null) {
                // Do not create Tenants and Projects once synchronization task is running.
                _authService.createTenantsAndProjectsForAutomaticKeystoneRegistration(keystoneProvider);
                _openStackSynchronizationTask.startSynchronizationTask(_openStackSynchronizationTask.getTaskInterval());
            }
        }

        return map(openstackTenants);
    }

    private OSTenant prepareOpenstackTenant(OpenStackTenantParam param) {

        OSTenant openstackTenant = new OSTenant();
        openstackTenant.setId(URIUtil.createId(OSTenant.class));
        openstackTenant.setName(param.getName());
        openstackTenant.setDescription(param.getDescription());
        openstackTenant.setEnabled(param.getEnabled());
        openstackTenant.setExcluded(param.getExcluded());
        openstackTenant.setOsId(param.getOsId());
        return openstackTenant;
    }

    private CoprhdOsTenantListRestRep map(List<OSTenant> tenants) {

        CoprhdOsTenantListRestRep response = new CoprhdOsTenantListRestRep();
        List<CoprhdOsTenant> coprhdOsTenants = new ArrayList<>();
        for (OSTenant osTenant : tenants) {
            coprhdOsTenants.add(mapOsTenant(osTenant));
        }
        response.setCoprhd_os_tenants(coprhdOsTenants);

        return response;
    }

    private OpenStackTenantParam mapToOpenstackParam(TenantV2 from) {
        OpenStackTenantParam to = new OpenStackTenantParam();

        to.setOsId(from.getId());
        to.setDescription(from.getDescription());
        to.setEnabled(Boolean.parseBoolean(from.getEnabled()));
        to.setExcluded(false);
        to.setName(from.getName());

        return to;
    }

    private CoprhdOsTenant mapOsTenant(OSTenant from) {

        CoprhdOsTenant to = new CoprhdOsTenant();
        DbObjectMapper.mapDataObjectFields(from, to);
        to.setExcluded(from.getExcluded());
        to.setDescription(from.getDescription());
        to.setOsId(from.getOsId());
        to.setEnabled(from.getEnabled());

        return to;
    }

    @Override
    protected DataObject queryResource(URI id) {
        return null;
    }

    @Override
    protected URI getTenantOwner(URI id) {
        return null;
    }

    @Override
    protected ResourceTypeEnum getResourceType() {
        return null;
    }
}