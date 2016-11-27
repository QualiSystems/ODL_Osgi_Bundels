package quali.com.ODLPacketHandler;
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

public class PacketHandler implements IListenDataPacket{
 
    private static final Logger log = LoggerFactory.getLogger(PacketHandler.class);
    private IDataPacketService dataPacketService;

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
             
                NodeConnector nc1 = NodeConnector.fromString("OF|1@OF|00:00:00:00:00:00:00:01");
                


                
                
                InetAddress addr = intToInetAddress(dstAddr);
                System.out.println("Pkt. to " + addr.toString() + " received by node " + node.getNodeIDString() + " on connector " + ingressConnector.getNodeConnectorIDString());
                

                /*node1.toString()
                OF|00:00:00:00:00:00:04:1f
                port
                OF|24@OF|00:00:00:00:00:00:04:1f
                */
                
                Object l4Datagram = ipv4Pkt.getPayload();
                Node node1 = NodeCreator.createOFNode(1l);
                System.out.println("node1.toString()");
                	System.out.println(node1.toString());
                    NodeConnector port = NodeConnectorCreator.createOFNodeConnector(
                            (short) 1, node1);
                    System.out.println("port");
                    System.out.println(port);
                    NodeConnector oport = NodeConnectorCreator.createOFNodeConnector(
                            (short) 30, node1);
                    byte srcMac[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                            (byte) 0x9a, (byte) 0xbc };
                    byte dstMac[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d,
                            (byte) 0x5e, (byte) 0x6f };
                	
                    InetAddress srcIP = null;
                    InetAddress ipMask = null;
                    InetAddress dstIP = null;
                    InetAddress newIP = null;
                    InetAddress ipMask2 = null;
   
					try {
						srcIP = InetAddress.getByName("172.28.30.50");

                     dstIP = InetAddress.getByName("171.71.9.52");
                     newIP = InetAddress.getByName("200.200.100.1");
                    ipMask = InetAddress.getByName("255.255.255.0");
                     ipMask2 = InetAddress.getByName("255.240.0.0");
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    short ethertype = EtherTypes.IPv4.shortValue();
                    short vlan = (short) 27;
                    byte vlanPr = 3;
                    Byte tos = 4;
                    byte proto = IPProtocols.TCP.byteValue();
                    short src = (short) 55000;
                    short dst = 80;

                    /*
                     * Create a SAL Flow aFlow
                     */
                    Match match = new Match();
                    match.setField(MatchType.IN_PORT, port);
                    match.setField(MatchType.DL_SRC, srcMac);
                    match.setField(MatchType.DL_DST, dstMac);
                    match.setField(MatchType.DL_TYPE, ethertype);
                    match.setField(MatchType.DL_VLAN, vlan);
                    match.setField(MatchType.DL_VLAN_PR, vlanPr);
                    match.setField(MatchType.NW_SRC, srcIP, ipMask);
                    match.setField(MatchType.NW_DST, dstIP, ipMask2);
                    match.setField(MatchType.NW_TOS, tos);
                    match.setField(MatchType.NW_PROTO, proto);
                    match.setField(MatchType.TP_SRC, src);
                    match.setField(MatchType.TP_DST, dst);

                    List<Action> actions = new ArrayList<Action>();
                    actions.add(new SetNwDst(newIP));
                    actions.add(new Output(oport));


                    Flow flow = new Flow(match, actions);
                    flow.setPriority((short) 100);
                    flow.setHardTimeout((short) 360);

                	
                	

                    // Use FlowProgrammerService to program flow.
                    Status status = flowProgrammerService.addFlow(node1, flow);
                    if (!status.isSuccess()) {
                    	System.out.println("EROORRORRORO!!!!!!!!"+ status.getDescription());
                        log.error("Could not program flow: " + status.getDescription());
                        return PacketResult.CONSUME;
                    }
                    else{
                    	System.out.println("Flow installed");
                    }
                	

            
            }
        

           
         

         
                       // if (publicInetAddress.equals(dstAddr) && dstPort == SERVICE_PORT) {
                            //log.info("Received packet for load balanced service");
         



                
                
                
                
                return PacketResult.KEEP_PROCESSING;
            }
        
        // We did not process the packet -> let someone else do the job.
        return PacketResult.IGNORED;
    }
    

	
	/*

	@Override
	public void receive(ISwitch arg0, OFMessage arg1) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
	}
    */



}