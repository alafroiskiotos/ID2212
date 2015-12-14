package gr.kzps.id2212.project.client.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.kzps.id2212.project.client.commands.Command;
import gr.kzps.id2212.project.client.exceptions.NotEnoughArguments;
import gr.kzps.id2212.project.client.exceptions.UnknownCommand;

public class ClientConsole {
	private final Logger LOG = LogManager.getLogger(ClientConsole.class);
	private Boolean running;
	private BufferedReader reader;
	private String input;
	private final Parser parser;
	
	public ClientConsole() {
		running = true;
		parser = new Parser();
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public void console() {
		LOG.debug("Client console");
		System.out.println("++ Hi my name is Moneypenny ++");
		
		while(isRunning()) {
			try {
				input = reader.readLine();
				LOG.debug("Raw user input: {}", input);
				Command command = parser.parse(input);
				command.execute();
				
			} catch (UnknownCommand | NotEnoughArguments ex) {
				System.out.println("> " + ex.getMessage());
				System.out.print("> ");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private Boolean isRunning() {
		return running;
	}
	
	private void halt() {
		running = false;
	}
}
