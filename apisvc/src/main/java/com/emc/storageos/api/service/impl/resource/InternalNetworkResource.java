/*
 * Copyright (c) 2013 EMC Corporation
 * All Rights Reserved
 */
package com.emc.storageos.api.service.impl.resource;

import com.emc.storageos.db.client.model.Network;
import com.emc.storageos.model.varray.NetworkEndpointParam;
import com.emc.storageos.model.varray.NetworkRestRep;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import static com.emc.storageos.api.mapper.VirtualArrayMapper.map;

/*
 * Internal api for managing the object transport zone
 */
@Path("/internal/vdc/networks")
public class InternalNetworkResource extends ResourceService {
    private NetworkService networkService;

    public void setNetworkService(NetworkService service) {
        networkService = service;
    }

    /*
     * POST to add or remove endpoints
     */
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("/{id}/endpoints")
    public NetworkRestRep updateNetworkEndpointsInternal(@PathParam("id") URI id,
            NetworkEndpointParam param) {
        Network tz = networkService.doUpdateEndpoints(id, param);
        return map(tz);
    }
}
