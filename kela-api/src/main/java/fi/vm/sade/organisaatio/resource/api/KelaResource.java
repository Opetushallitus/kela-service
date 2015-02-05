package fi.vm.sade.organisaatio.resource.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/kela")
public interface KelaResource {
    public String OID_SEPARATOR = "/";
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{hakukohdeOid}/tutkinnontaso")
    public String tutkinnontaso(@PathParam("hakukohdeOid") String hakukohdeOid);
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{hakukohdeOid}/koulutusaste")
    public String koulutusaste(@PathParam("hakukohdeOid") String hakukohdeOid);
}

