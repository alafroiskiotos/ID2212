package gr.kzps.id2212.project.client.agent;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import gr.kzps.id2212.project.agentserver.agentservice.AgentRunningContainer;
import gr.kzps.id2212.project.agentserver.overlay.PeerAgent;
import gr.kzps.id2212.project.client.query.Result;

public interface Agent extends Serializable {
	public void agentArrived(AgentRunningContainer container,
			PeerAgent serverReference) throws RemoteException;
	public String getResult();
	public List<VisitedServer> getVisitedServers();
	public List<Result> getResultSet();
	public RemoteAgent getRemoteInterface();
	public UUID getId();
}
