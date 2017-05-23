
package org.quali.com.routeprogrammer.northbound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.ContextResolver;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.opendaylight.controller.containermanager.IContainerManager;

import org.opendaylight.controller.northbound.commons.RestMessages;
import org.opendaylight.controller.northbound.commons.exception.InternalServerErrorException;
import org.opendaylight.controller.northbound.commons.exception.MethodNotAllowedException;
import org.opendaylight.controller.northbound.commons.exception.NotAcceptableException;
import org.opendaylight.controller.northbound.commons.exception.ResourceNotFoundException;
import org.opendaylight.controller.northbound.commons.exception.ServiceUnavailableException;
import org.opendaylight.controller.northbound.commons.exception.UnauthorizedException;
import org.opendaylight.controller.northbound.commons.query.QueryContext;
import org.opendaylight.controller.northbound.commons.utils.NorthboundUtils;
import org.opendaylight.controller.sal.authorization.Privilege;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.utils.GlobalConstants;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.switchmanager.ISwitchManager;

import com.fasterxml.jackson.core.JsonParser;

import org.json.*;
//import org.json.simple.*;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;


/**
 * Flow Configuration Northbound API provides capabilities to program flows.
 * <p>
 * <br>
 * <br>
 * Authentication scheme : <b>HTTP Basic</b><br>
 * Authentication realm : <b>opendaylight</b><br>
 * Transport : <b>HTTP and HTTPS</b><br>
 * <br>
 * HTTPS Authentication is disabled by default.
 */
@Path("/")
public class RouteProgrammerNorthbound {

    private static final String ROUTES_PATH = "/home/shellroutes";
    private String username;
    private QueryContext queryContext;

    @Context
    public void setQueryContext(ContextResolver<QueryContext> queryCtxResolver) {
        if (queryCtxResolver != null) {
            queryContext = queryCtxResolver.getContext(QueryContext.class);
        }
    }

    @Context
    public void setSecurityContext(SecurityContext context) {
        if (context != null && context.getUserPrincipal() != null) {
            username = context.getUserPrincipal().getName();
        }
    }

    protected String getUserName() {
        return username;
    }


    @Path("/shellroute")
    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Flow Config processed successfully"),
            @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The Container Name or Node-id or Flow Name passed is not found"),
            @ResponseCode(code = 406, condition = "Failed to delete Flow config due to invalid operation. Failure details included in HTTP Error response"),
            @ResponseCode(code = 500, condition = "Failed to delete Flow config. Failure Reason included in HTTP Error response"),
            @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable")})
    public Response toggleFlow1(
            @PathParam("shellroute") String shellroute) {


        System.out.println("#######ROUTE2!!!!!!!");


        return Response.ok().build();
    }


    @Path("/hi/{id}")
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //throws Exceptionp
    public Response sayPlainTextHello(@PathParam("id") JSONObject inputJsonObj) throws JSONException {
        System.out.println("jsjsjsjsj");
        String input = (String) inputJsonObj.get("input");


        return Response.ok().build();
    }


    @Path("/shellroute/sourcenode/{nodeId}/sourceport/{portId}/{route}")
    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @StatusCodes({
            @ResponseCode(code = 200, condition = "routingg processed successfully"),
            @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The path not found"),
            @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable")})
    public Response toggleFlow(@PathParam(value = "nodeId") String nodeId, @PathParam(value = "portId") String portId, @PathParam(value = "route") JSONObject inputJsonObj) {

        File routesFolder = new File(ROUTES_PATH);

        if (!routesFolder.exists()) {
            routesFolder.mkdir();
        }

        String destination_file = ROUTES_PATH + "/" + nodeId + "_" + portId + ".txt";
        System.out.println("The path " + destination_file);
        ObjectOutputStream outputStream = null;
        FileWriter file = null;

        try {
            file = new FileWriter(destination_file);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println(e1);
        }
        try {
            file.write(inputJsonObj.toString());
            file.flush();
            file.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println(e1);
        }
        System.out.println("Successfully Copied JSON Object to File...");
        System.out.println("\nJSON Object: " + inputJsonObj.toString());


        return Response.ok().build();
    }

    @Path("/shellroute/sourcenode/{nodeId}/sourceport/{portId}")
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    @StatusCodes({
            @ResponseCode(code = 200, condition = "Route was successfully deleted"),
            @ResponseCode(code = 401, condition = "User is not authorized to perform this operation"),
            @ResponseCode(code = 404, condition = "The path not found"),
            @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable")})
    public Response deleteFlow(@PathParam(value = "nodeId") String nodeId, @PathParam(value = "portId") String portId) {

        String routeFilePath = ROUTES_PATH + "/" + nodeId + "_" + portId + ".txt";
        System.out.println("Routes file path: " + routeFilePath);

        File routeFile = new File(routeFilePath);

        if (routeFile.exists()) {
            routeFile.delete();
        } else {
            System.out.println("Route " + routeFilePath + "was already deleted");
        }

        System.out.println("Successfully deleted route " + routeFilePath);

        return Response.ok().build();
    }

    private Node handleNodeAvailability(String containerName, String nodeType, String nodeId) {

        Node node = Node.fromString(nodeType, nodeId);
        if (node == null) {
            throw new ResourceNotFoundException(nodeId + " : " + RestMessages.NONODE.toString());
        }

        ISwitchManager sm = (ISwitchManager) ServiceHelper.getInstance(ISwitchManager.class, containerName, this);

        if (sm == null) {
            throw new ServiceUnavailableException("Switch Manager " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        if (!sm.getNodes().contains(node)) {
            throw new ResourceNotFoundException(node.toString() + " : " + RestMessages.NONODE.toString());
        }
        return node;
    }

    private void handleDefaultDisabled(String containerName) {
        IContainerManager containerManager = (IContainerManager) ServiceHelper.getGlobalInstance(
                IContainerManager.class, this);
        if (containerManager == null) {
            throw new InternalServerErrorException(RestMessages.INTERNALERROR.toString());
        }
        if (containerName.equals(GlobalConstants.DEFAULT.toString()) && containerManager.hasNonDefaultContainer()) {
            throw new NotAcceptableException(RestMessages.DEFAULTDISABLED.toString());
        }
    }

    private void handleResourceCongruence(String resource, String configured) {
        if (!resource.equals(configured)) {
            throw new MethodNotAllowedException("Path's resource name conflicts with payload's resource name");
        }
    }


}