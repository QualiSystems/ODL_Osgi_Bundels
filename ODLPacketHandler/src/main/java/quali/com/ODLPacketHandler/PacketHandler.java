package quali.com.ODLPacketHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.action.SetNwDst;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.*;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.NodeConnectorCreator;
import org.opendaylight.controller.sal.utils.NodeCreator;
import org.opendaylight.controller.sal.utils.Status;
import org.openflow.protocol.OFFlowRemoved;
import org.opendaylight.controller.sal.flowprogrammer.*;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonSerializer;

import sun.misc.IOUtils;

import org.json.*;
import org.json.simple.parser.JSONParser;

public class PacketHandler implements IListenDataPacket{
 
    private static final Logger log = LoggerFactory.getLogger(PacketHandler.class);
    private IDataPacketService dataPacketService;
    String check_dir = "/home/shellroutes";
    private IFlowProgrammerService flowProgrammerService;
    static private InetAddress intToInetAddress(int i) {
        byte b[] = new byte[] { (byte) ((i>>24)&0xff), (byte) ((i>>16)&0xff), (byte) ((i>>8)&0xff), (byte) (i&0xff) };
        InetAddress addr;
        try {
            addr = InetAddress.getByAddress(b);
        } catch (UnknownHostException e) {
            return null;
        }
 
        return addr;
    }
 
    /*
     * Sets a reference to the requested DataPacketService
     * See Activator.configureInstance(...):
     * c.add(createContainerServiceDependency(containerName).setService(
     * IDataPacketService.class).setCallbacks(
     * "setDataPacketService", "unsetDataPacketService")
     * .setRequired(true));
     */
    void setDataPacketService(IDataPacketService s) {
    	
        log.trace("Set DataPacketService.");
        
        dataPacketService = s;
    }
    /*
    void setMessageListenerService(IMessageListener listener)
 
    {
        this.addMessageListener(OFType.PACKET_IN, this);
        this.addMessageListener(OFType.FLOW_REMOVED, this);
        this.addMessageListener(OFType.ERROR, this);
    }
    */
    /*
     * Unsets DataPacketService
     * See Activator.configureInstance(...):
     * c.add(createContainerServiceDependency(containerName).setService(
     * IDataPacketService.class).setCallbacks(
     * "setDataPacketService", "unsetDataPacketService")
     * .setRequired(true));
     */
    void unsetDataPacketService(IDataPacketService s) {
        log.trace("Removed DataPacketService.");
 
        if (dataPacketService == s) {
            dataPacketService = null;
        }
    }
    
    
    
    void setFlowProgrammerService(IFlowProgrammerService s) {
        log.trace("Set FlowProgrammerService.");
     
        flowProgrammerService = s;
    }
     
    /**
     * Unsets FlowProgrammerService
     */
    void unsetFlowProgrammerService(IFlowProgrammerService s) {
        log.trace("Removed FlowProgrammerService.");
     
        if (flowProgrammerService == s) {
            flowProgrammerService = null;
        }
    }
    

 
    @Override
    public PacketResult receiveDataPacket(RawPacket inPkt) {
    	
    	synchronized (this) {
        // The connector, the packet came from ("port")
        NodeConnector ingressConnector = inPkt.getIncomingNodeConnector();
        // The node that received the packet ("switch")
        Node node = ingressConnector.getNode();
        
        //"OF|1@OF|00:00:00:00:00:00:00:01"
        // Use DataPacketService to decode the packet.
        
        Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);
        

        if (l2pkt instanceof Ethernet) {
        	

        	
            Object l3Pkt = l2pkt.getPayload();

            if (l3Pkt instanceof IPv4) {
                IPv4 ipv4Pkt = (IPv4) l3Pkt;
                int dstAddr = ipv4Pkt.getDestinationAddress();
                int srcAddr = ipv4Pkt.getSourceAddress();
                NodeConnector incoming_connector = inPkt.getIncomingNodeConnector();
                System.out.println("incoming_connector");
                System.out.println(incoming_connector);
                NodeConnector nc1 = NodeConnector.fromString("OF|1@OF|00:00:00:00:00:00:00:01");
                
                
                String[] connector_parts = incoming_connector.toString().split("\\|");
                
                String connector_port = connector_parts[1].split("\\@")[0];
                System.out.println("connector_port");
                System.out.println(connector_port);
                String connector_id = connector_parts[2];

                System.out.println(connector_id);
                
                String filePathString = check_dir + "/" + connector_id + "_" + connector_port + ".txt";
                BufferedReader reader = null;
                if(new File(filePathString).isFile())
                {
				try {
					reader = new BufferedReader(new FileReader(filePathString));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					System.out.println(e1);
				}
                String json = "";
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = reader.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = reader.readLine();
                    }
                    json = sb.toString();
                } catch (IOException e) {
					// TODO Auto-generated catch block
                	System.out.println(e);
				} finally {
                    try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(e);
					}
                }
                
                
                JSONObject obj = null;
				try {
					obj = new JSONObject(json);
					System.out.println("obj.length()");
					System.out.println(obj.length());
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					System.out.println(e1);
				} // this will get you the entire JSON node
				PacketResult status_installed_flow =  null;
                
				try {

					for (int i=0; i<obj.getJSONObject("route").length()/2;i++)
					{
						System.out.println("i"+i);
						
						String switch_id = obj.getJSONObject("route").getString("switch" + i);
						String ports = obj.getJSONObject("route").getString("port" + i);
						String[] port_arr = ports.toString().split("\\-");
						int in_port = Integer.parseInt(port_arr[0]);
						int out_port = Integer.parseInt(port_arr[1]);
						System.out.println(switch_id);
						System.out.println(in_port);
						System.out.println(out_port);
						Node node1 = NodeCreator.createOFNode(Long.parseLong(switch_id));
	                    NodeConnector ingress_port = NodeConnectorCreator.createOFNodeConnector(
	                            (short) in_port, node1);
	                    NodeConnector egress_port = NodeConnectorCreator.createOFNodeConnector(
	                            (short) out_port, node1);
	                    Object l4Datagram = ipv4Pkt.getPayload();
	                    short ethertype = EtherTypes.IPv4.shortValue();
	                    InetAddress dst_addr = intToInetAddress(dstAddr);
	                    InetAddress src_addr = intToInetAddress(srcAddr);
	                    System.out.println("Pkt. to " + dst_addr.toString() + " received by node " + node.getNodeIDString() + " on connector " + ingressConnector.getNodeConnectorIDString());
	                    status_installed_flow =  this.InstallFlow(node1,ingress_port,egress_port,dst_addr,src_addr,ethertype);
						
						
					}
					
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					System.out.println(e1);
				} // put in whatever your JSON data name here, this will get you an array of all the nodes
                
                
               
                    
                
                }
                

                

                /*node1.toString()
                OF|00:00:00:00:00:00:04:1f
                port
                OF|24@OF|00:00:00:00:00:00:04:1f
                */
                
                




            

            
           
        

           
         

         
                       // if (publicInetAddress.equals(dstAddr) && dstPort == SERVICE_PORT) {
                            //log.info("Received packet for load balanced service");
         

                
                
                
                
            } //synchronized
            }
            }
        
        // We did not process the packet -> let someone else do the job.
    	return PacketResult.CONSUME;
        
    }
    
    private PacketResult InstallFlow(Node node1,NodeConnector in_port,NodeConnector out_port,InetAddress dstAddr,InetAddress srcAddr,short ethertype)
    {
        /*
         * Create a SAL Flow aFlow
         */
        Match match = new Match();
        match.setField(MatchType.IN_PORT, in_port);
        match.setField(MatchType.DL_TYPE, ethertype);

        //match.setField(MatchType.NW_PROTO, proto);
        match.setField(MatchType.NW_DST, dstAddr);
        match.setField(MatchType.NW_SRC, srcAddr);
        List<Action> actions = new ArrayList<Action>();
        //actions.add(new SetNwDst(newIP));
        actions.add(new Output(out_port));


        Flow flow = new Flow(match, actions);
        flow.setPriority((short) 500);
        flow.setIdleTimeout((short) 100);

    	
    	

        // Use FlowProgrammerService to program flow.
        Status status = flowProgrammerService.addFlow(node1, flow);
        if (!status.isSuccess()) {
        	System.out.println("EROORRORRORO!!!!!!!!"+ status.getDescription());
            log.error("Could not program flow: " + status.getDescription());
            return PacketResult.IGNORED;
            
        }
        else{
        	System.out.println("Flow installed");
        }
        return PacketResult.CONSUME;
    	
    }
	
	/*

	@Override
	public void receive(ISwitch arg0, OFMessage arg1) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
	}
    */



}