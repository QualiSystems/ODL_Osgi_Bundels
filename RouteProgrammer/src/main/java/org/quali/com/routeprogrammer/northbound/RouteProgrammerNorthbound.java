
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
 *
 * <br>
 * <br>
 * Authentication scheme : <b>HTTP Basic</b><br>
 * Authentication realm : <b>opendaylight</b><br>
 * Transport : <b>HTTP and HTTPS</b><br>
 * <br>
 * HTTPS Authentication is disabled by default.
 *
 */
@Path("/")
public class RouteProgrammerNorthbound {

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



    /**
     * Returns a list of Flows configured on a Node in a given container
     *
     * @param containerName
     *            Name of the Container (Eg. 'default')
     * @param nodeType
     *            Type of the node being programmed (Eg. 'OF')
     * @param nodeId
     *            Node Identifier (Eg. '00:00:00:00:00:00:00:01')
     * @return List of flows configured on a Node in a container
     *
     *         <pre>
     *
     * Example:
     *
     * Request URL:
     * http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:00:00:00:00:01
     *
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * &lt;list&gt;
     *     &#x20;&#x20;&#x20;&lt;flowConfig&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;installInHw&gt;true&lt;/installInHw&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;name&gt;flow1&lt;/name&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;node&gt;
     *             &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;id&gt;00:00:00:00:00:00:00:01&lt;/id&gt;
     *             &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;type&gt;OF&lt;/type&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;/node&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;ingressPort&gt;1&lt;/ingressPort&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;priority&gt;500&lt;/priority&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;etherType&gt;0x800&lt;/etherType&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;nwSrc&gt;9.9.1.1&lt;/nwSrc&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;actions&gt;OUTPUT=2&lt;/actions&gt;
     *     &#x20;&#x20;&#x20;&lt;/flowConfig&gt;
     * &lt;/list&gt;
     *
    * Response body in JSON:
     * {
     *   "flowConfig": [
     *      {
     *         "installInHw": "true",
     *         "name": "flow1",
     *         "node": {
     *            "type": "OF",
     *            "id": "00:00:00:00:00:00:00:01"
     *         },
     *         "ingressPort": "1",
     *         "priority": "500",
     *         "etherType": "0x800",
     *         "nwSrc":"9.9.1.1",
     *         "actions": [
     *           "OUTPUT=2"
     *         ]
     *       }
     *    ]
     * }
     * </pre>
     */

    /**
     * Returns the flow configuration matching a human-readable name and nodeId
     * on a given Container.
     *
     * @param containerName
     *            Name of the Container (Eg. 'default')
     * @param nodeType
     *            Type of the node being programmed (Eg. 'OF')
     * @param nodeId
     *            Node Identifier (Eg. '00:00:00:00:00:00:00:01')
     * @param name
     *            Human-readable name for the configured flow (Eg. 'Flow1')
     * @return Flow configuration matching the name and nodeId on a Container
     *
     *         <pre>
     *
     * Example:
     *
     * Request URL:
     * http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:00:00:00:00:01/staticFlow/flow1
     *
     * Response body in XML:
     * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     * &lt;flowConfig&gt;
     *     &#x20;&#x20;&#x20;&lt;installInHw&gt;true&lt;/installInHw&gt;
     *     &#x20;&#x20;&#x20;&lt;name&gt;flow1&lt;/name&gt;
     *     &#x20;&#x20;&#x20;&lt;node&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;id&gt;00:00:00:00:00:00:00:01&lt;/id&gt;
     *         &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;type&gt;OF&lt;/type&gt;
     *     &#x20;&#x20;&#x20;&lt;/node&gt;
     *     &#x20;&#x20;&#x20;&lt;ingressPort&gt;1&lt;/ingressPort&gt;
     *     &#x20;&#x20;&#x20;&lt;priority&gt;500&lt;/priority&gt;
     *     &#x20;&#x20;&#x20;&lt;etherType&gt;0x800&lt;/etherType&gt;
     *     &#x20;&#x20;&#x20;&lt;nwSrc&gt;9.9.1.1&lt;/nwSrc&gt;
     *     &#x20;&#x20;&#x20;&lt;actions&gt;OUTPUT=2&lt;/actions&gt;
     * &lt;/flowConfig&gt;
     *
    * Response body in JSON:
     * {
     *    "installInHw":"true",
     *    "name":"flow1",
     *    "node":{
     *       "id":"00:00:00:00:00:00:00:01",
     *       "type":"OF"
     *    },
     *    "ingressPort":"1",
     *    "priority":"500",
     *    "etherType":"0x800",
     *    "nwSrc":"9.9.1.1",
     *    "actions":[
     *       "OUTPUT=2"
     *    ]
     * }
     *
     * </pre>
     */
    /*
    @Path("/{containerName}/node/{nodeType}/{nodeId}/staticFlow/{name}")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @TypeHint(FlowConfig.class)
    @StatusCodes({ @ResponseCode(code = 200, condition = "Operation successful"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The containerName or NodeId or Configuration name is not found"),
        @ResponseCode(code = 503, condition = "One or more of Controller Services are unavailable") })
    public FlowConfig getStaticFlow(@PathParam("containerName") String containerName,
            @PathParam("nodeType") String nodeType, @PathParam("nodeId") String nodeId, @PathParam("name") String name) {
        if (!NorthboundUtils.isAuthorized(getUserName(), containerName, Privilege.READ, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container "
                    + containerName);
        }
        IForwardingRulesManager frm = getForwardingRulesManagerService(containerName);

        if (frm == null) {
            throw new ServiceUnavailableException("Flow Programmer " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        Node node = handleNodeAvailability(containerName, nodeType, nodeId);

        FlowConfig staticFlow = frm.getStaticFlow(name, node);
        if (staticFlow == null) {
            throw new ResourceNotFoundException(RestMessages.NOFLOW.toString());
        }

        return new FlowConfig(staticFlow);
    }
    */
    /**
     * Add or Modify a flow configuration. If the flow exists already, it will replace the current flow.
     *
     * @param containerName
     *            Name of the Container (Eg. 'default')
     * @param nodeType
     *            Type of the node being programmed (Eg. 'OF')
     * @param nodeId
     *            Node Identifier (Eg. '00:00:00:00:00:00:00:01')
     * @param name
     *            Name of the Static Flow configuration (Eg. 'Flow2')
     * @param FlowConfig
     *            Flow Configuration in JSON or XML format
     * @return Response as dictated by the HTTP Response Status code
     *
     *         <pre>
     *
     * Example:
     *
     * Request URL:
     * http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:00:00:00:00:01/staticFlow/flow1
     *
     * Request body in XML:
     * &lt;flowConfig&gt;
     *         &#x20;&#x20;&#x20;&lt;installInHw&gt;true&lt;/installInHw&gt;
     *         &#x20;&#x20;&#x20;&lt;name&gt;flow1&lt;/name&gt;
     *         &#x20;&#x20;&#x20;&lt;node&gt;
     *             &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;id&gt;00:00:00:00:00:00:00:01&lt;/id&gt;
     *             &#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&lt;type&gt;OF&lt;/type&gt;
     *         &#x20;&#x20;&#x20;&lt;/node&gt;
     *         &#x20;&#x20;&#x20;&lt;ingressPort&gt;1&lt;/ingressPort&gt;
     *         &#x20;&#x20;&#x20;&lt;priority&gt;500&lt;/priority&gt;
     *         &#x20;&#x20;&#x20;&lt;etherType&gt;0x800&lt;/etherType&gt;
     *         &#x20;&#x20;&#x20;&lt;nwSrc&gt;9.9.1.1&lt;/nwSrc&gt;
     *         &#x20;&#x20;&#x20;&lt;actions&gt;OUTPUT=2&lt;/actions&gt;
     * &lt;/flowConfig&gt;
     *
     * Request body in JSON:
      * {
     *    "installInHw":"true",
     *    "name":"flow1",
     *    "node":{
     *       "id":"00:00:00:00:00:00:00:01",
     *       "type":"OF"
     *    },
     *    "ingressPort":"1",
     *    "priority":"500",
     *    "etherType":"0x800",
     *    "nwSrc":"9.9.1.1",
     *    "actions":[
     *       "OUTPUT=2"
     *    ]
     * }
     * </pre>
     */
/*
    @Path("/{containerName}/node/{nodeType}/{nodeId}/staticFlow/{name}")
    @PUT
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @StatusCodes({
        @ResponseCode(code = 200, condition = "Static Flow modified successfully"),
        @ResponseCode(code = 201, condition = "Flow Config processed successfully"),
        @ResponseCode(code = 400, condition = "Failed to create Static Flow entry due to invalid flow configuration"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The Container Name or nodeId is not found"),
        @ResponseCode(code = 406, condition = "Cannot operate on Default Container when other Containers are active"),
        @ResponseCode(code = 409, condition = "Failed to create Static Flow entry due to Conflicting Name or configuration"),
        @ResponseCode(code = 500, condition = "Failed to create Static Flow entry. Failure Reason included in HTTP Error response"),
        @ResponseCode(code = 503, condition = "One or more of Controller services are unavailable") })
    public Response addOrModifyFlow(@PathParam(value = "containerName") String containerName,
            @PathParam(value = "name") String name, @PathParam("nodeType") String nodeType,
            @PathParam(value = "nodeId") String nodeId, @TypeHint(FlowConfig.class) FlowConfig flowConfig) {

        if (!NorthboundUtils.isAuthorized(getUserName(), containerName, Privilege.WRITE, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container "
                    + containerName);
        }

        if (flowConfig.getNode() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Configuration. Node is null or empty")
                    .build();
        }
        handleResourceCongruence(name, flowConfig.getName());
        handleResourceCongruence(nodeType, flowConfig.getNode().getType());
        handleResourceCongruence(nodeId, flowConfig.getNode().getID() == null ? null : flowConfig.getNode().getNodeIDString());
        handleDefaultDisabled(containerName);

        IForwardingRulesManager frm = getForwardingRulesManagerService(containerName);

        if (frm == null) {
            throw new ServiceUnavailableException("Flow Programmer " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        Node node = handleNodeAvailability(containerName, nodeType, nodeId);
        Status status;

        FlowConfig staticFlow = frm.getStaticFlow(name, node);

        if (staticFlow == null) {
          status = frm.addStaticFlow(flowConfig);
          if(status.isSuccess()){
              NorthboundUtils.auditlog("Flow Entry", username, "added",
                      name + " on Node " + NorthboundUtils.getNodeDesc(node, containerName, this), containerName);
              return Response.status(Response.Status.CREATED).entity("Success").build();
          }
        } else {
          status = frm.modifyStaticFlow(flowConfig);
          if(status.isSuccess()){
              NorthboundUtils.auditlog("Flow Entry", username, "updated",
                      name + " on Node " + NorthboundUtils.getNodeDesc(node, containerName, this), containerName);
              return NorthboundUtils.getResponse(status);
          }
        }
        return NorthboundUtils.getResponse(status);
    }
*/
    /**
     * Delete a Flow configuration
     *
     * @param containerName
     *            Name of the Container (Eg. 'default')
     * @param nodeType
     *            Type of the node being programmed (Eg. 'OF')
     * @param nodeId
     *            Node Identifier (Eg. '00:00:00:00:00:00:00:01')
     * @param name
     *            Name of the Static Flow configuration (Eg. 'Flow1')
     * @return Response as dictated by the HTTP Response code
     *
     *         <pre>
     *
     * Example:
     *
     * RequestURL:
     * http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:00:00:00:00:01/staticFlow/flow1
     *
     * </pre>
     */
/*
    @Path("/{containerName}/node/{nodeType}/{nodeId}/staticFlow/{name}")
    @DELETE
    @StatusCodes({
        @ResponseCode(code = 204, condition = "Flow Config deleted successfully"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The Container Name or Node-id or Flow Name passed is not found"),
        @ResponseCode(code = 406, condition = "Failed to delete Flow config due to invalid operation. Failure details included in HTTP Error response"),
        @ResponseCode(code = 500, condition = "Failed to delete Flow config. Failure Reason included in HTTP Error response"),
        @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable") })
    public Response deleteFlow(@PathParam(value = "containerName") String containerName,
            @PathParam(value = "name") String name, @PathParam("nodeType") String nodeType,
            @PathParam(value = "nodeId") String nodeId) {

        if (!NorthboundUtils.isAuthorized(getUserName(), containerName, Privilege.WRITE, this)) {
            throw new UnauthorizedException("User is not authorized to perform this operation on container "
                    + containerName);
        }
        handleDefaultDisabled(containerName);

        IForwardingRulesManager frm = getForwardingRulesManagerService(containerName);

        if (frm == null) {
            throw new ServiceUnavailableException("Flow Programmer " + RestMessages.SERVICEUNAVAILABLE.toString());
        }

        Node node = handleNodeAvailability(containerName, nodeType, nodeId);

        FlowConfig staticFlow = frm.getStaticFlow(name, node);
        if (staticFlow == null) {
            throw new ResourceNotFoundException(name + " : " + RestMessages.NOFLOW.toString());
        }

        Status status = frm.removeStaticFlow(name, node);
        if (status.isSuccess()) {
            NorthboundUtils.auditlog("Flow Entry", username, "removed",
                    name + " from Node " + NorthboundUtils.getNodeDesc(node, containerName, this), containerName);
            return Response.noContent().build();
        }
        return NorthboundUtils.getResponse(status);
    }
*/
    /**
     * Toggle a Flow configuration
     *
     * @param containerName
     *            Name of the Container (Eg. 'default')
     * @param nodeType
     *            Type of the node being programmed (Eg. 'OF')
     * @param nodeId
     *            Node Identifier (Eg. '00:00:00:00:00:00:00:01')
     * @param name
     *            Name of the Static Flow configuration (Eg. 'Flow1')
     * @return Response as dictated by the HTTP Response code
     *
     *         <pre>
     *
     * Example:
     *
     * RequestURL:
     * http://localhost:8080/controller/nb/v2/flowprogrammer/default/node/OF/00:00:00:00:00:00:00:01/staticFlow/flow1
     *
     * </pre>
     */
    
    
    @Path("/shellroute")
    @GET
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @StatusCodes({
        @ResponseCode(code = 200, condition = "Flow Config processed successfully"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The Container Name or Node-id or Flow Name passed is not found"),
        @ResponseCode(code = 406, condition = "Failed to delete Flow config due to invalid operation. Failure details included in HTTP Error response"),
        @ResponseCode(code = 500, condition = "Failed to delete Flow config. Failure Reason included in HTTP Error response"),
        @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable") })
    public Response toggleFlow1(
            @PathParam("shellroute") String shellroute) {


        
        System.out.println("#######ROUTE2!!!!!!!");

        
          
        return Response.ok().build();
    }
    
     
      @Path("/hi/{id}")
      @POST
      @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
      @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
      //throws Exceptionp
      public Response sayPlainTextHello(@PathParam("id") JSONObject inputJsonObj) throws JSONException  
      {
    	System.out.println("jsjsjsjsj");
        String input = (String) inputJsonObj.get("input");
        




        return Response.ok().build();
      }
      
    

    @Path("/shellroute/sourcenode/{nodeId}/sourceport/{portId}/{route}")
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @StatusCodes({
        @ResponseCode(code = 200, condition = "routingg processed successfully"),
        @ResponseCode(code = 401, condition = "User not authorized to perform this operation"),
        @ResponseCode(code = 404, condition = "The path not found"),
        @ResponseCode(code = 503, condition = "One or more of Controller service is unavailable") })
    public Response toggleFlow(@PathParam(value = "nodeId") String nodeId,@PathParam(value = "portId") String portId,@PathParam(value = "route") JSONObject inputJsonObj)
    {
    	


        String workingDir = System.getProperty("user.dir");
        

        System.out.println("Current relative path is: " + workingDir);
        
        String check_path = workingDir + "/temp";
        
        if (new File(check_path).exists() == false)
        {
        	File dir = new File(check_path);
        	dir.mkdir();
        	
        }
        
        String destination_file = check_path + "/" + nodeId + "_" + portId + ".txt";
        System.out.println("The path "+ destination_file);
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