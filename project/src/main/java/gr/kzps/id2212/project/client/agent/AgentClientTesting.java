package gr.kzps.id2212.project.client.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.QueryParam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.kzps.id2212.project.classloader.QueryPlanClassLoader;
import gr.kzps.id2212.project.client.query.Query;
import gr.kzps.id2212.project.client.query.QueryParameter;
import gr.kzps.id2212.project.client.query.QueryPlan;

/*
 * Just for TESTING purposes
 */
public class AgentClientTesting {
	private static final Logger LOG = LogManager.getLogger(AgentClientTesting.class);
	private static Socket socket;
	
	public static void main(String[] args) {
		try {
			InetAddress server = InetAddress.getByName("localhost");
			Integer port = 6060;
			UUID id = UUID.randomUUID();
			QueryPlanClassLoader loader = new QueryPlanClassLoader(AgentClientTesting.class.getClassLoader());
			QueryPlan queryPlan = loader.loadPlan("userplan.UserQueryPlan");
			
			QueryParameter<String> author = queryPlan.getAuthor();
			QueryParameter<String> title = queryPlan.getTitle();
			QueryParameter<Date> date = queryPlan.getDate();
			QueryParameter<List<String>> keywords = queryPlan.getKeywords();
			
			Query query = new Query(author, date, keywords, title);
			
			Agent agent = new AgentImpl(id, InetAddress.getByName("localhost"),
					5050, query);

			LOG.debug("Agent created and sending to server");
			socket = new Socket(server, port);
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			
			outStream.writeObject(agent);
			
			outStream.flush();
			if (outStream != null)
				outStream.close();
			if (socket != null)
				socket.close();
			
			ServerSocket sSocket = new ServerSocket(5050);
			Socket cSocket = sSocket.accept();
			
			ObjectInputStream inStream = new ObjectInputStream(cSocket.getInputStream());
			Agent comingAgent = (Agent) inStream.readObject();
			
			if (inStream != null)
				inStream.close();
			if (cSocket != null)
				cSocket.close();
			if (sSocket != null)
				sSocket.close();
			
			LOG.debug(comingAgent.getResult());
		} catch (IOException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}

	}

}
