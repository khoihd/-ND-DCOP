package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * REVIEWED 
 * @author khoihd
 *
 */
public class INIT_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

  private static final long serialVersionUID = 6619734019693007342L;

  AgentPDDCOP agent;
  
  public INIT_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @Override
  public void action() {    
    double utilFromChildren = 0;
    
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(INIT_LS_UTIL);
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
        
    // Send the partial quality of the subtree to parent
    double localSearchQuality = utilFromChildren + 
        agent.utilityLSWithParentAndPseudoAndUnary() - agent.computeSwitchingCostAllTimeStep();
    
    agent.stopStimulatedTiming();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, INIT_LS_UTIL, agent.getSimulatedTime());
    }
    else {
      // First time
      agent.setLocalSearchQuality(-1, localSearchQuality);
      agent.setLocalSearchRuntime(-1, agent.getSimulatedTime());
    }
  }
  
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDSet().size()) {
      agent.startSimulatedTiming();
      
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
        
      agent.stopStimulatedTiming();
//      if (receivedMessage != null) {
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
          
        if (timeFromReceiveMessage > agent.getSimulatedTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        }
        
        messageList.add(receivedMessage); 
//      }
//      else {
//          block();
//      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}