package gr.kzps.id2212.marketplace.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.StringTokenizer;

import gr.kzps.id2212.marketplace.client.Commands.BankNewAccount;
import gr.kzps.id2212.marketplace.client.Commands.BaseCommand;
import gr.kzps.id2212.marketplace.client.Commands.BuyCommand;
import gr.kzps.id2212.marketplace.client.Commands.Commands;
import gr.kzps.id2212.marketplace.client.Commands.Exit;
import gr.kzps.id2212.marketplace.client.Commands.Help;
import gr.kzps.id2212.marketplace.client.Commands.ListCommand;
import gr.kzps.id2212.marketplace.client.Commands.NotEnoughParams;
import gr.kzps.id2212.marketplace.client.Commands.RegisterMarketplace;
import gr.kzps.id2212.marketplace.client.Commands.SellCommand;
import gr.kzps.id2212.marketplace.client.Commands.UnknownCommand;
import gr.kzps.id2212.marketplace.client.Commands.UnregisterMarketplace;
import gr.kzps.id2212.marketplace.server.BaseItem;
import gr.kzps.id2212.marketplace.server.MarketServer;
import gr.kzps.id2212.marketplace.server.exceptions.BankBalance;
import gr.kzps.id2212.marketplace.server.exceptions.ItemDoesNotExists;
import gr.kzps.id2212.marketplace.server.exceptions.NoBankAccountException;
import gr.kzps.id2212.marketplace.server.exceptions.NoUserException;
import gr.kzps.id2212.marketplace.server.exceptions.UserAlreadyExists;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.kth.id2212.ex2.bankrmi.Account;
import se.kth.id2212.ex2.bankrmi.Bank;
import se.kth.id2212.ex2.bankrmi.RejectedException;

public class ClientConsole {
	private static final Logger LOG = LogManager.getLogger(ClientConsole.class);

	private boolean running;
	private final Bank bank;
	private final MarketServer market;
	// private Callbacks callbacks;
	// private Client currentUser;
	private Parser parser;

	public ClientConsole(Bank bank, MarketServer market) {
		this.bank = bank;
		this.market = market;

		// callbacks = null;
		// currentUser = null;
		running = true;
		parser = new Parser();
	}

	public void console() throws RemoteException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Welcome to " + market.getName());

		while (isRunning()) {
			try {
				String input = in.readLine();
				LOG.debug("Raw user input: {}", input);
				BaseCommand command = parser.parse(input);

				execute(command);

			} catch (RemoteException ex) {
				LOG.error("RMI error");
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void execute(BaseCommand command) throws RemoteException {
		if (command instanceof UnknownCommand) {
			System.out.println("> Unknown command, try help");
		} else if (command instanceof NotEnoughParams) {
			System.out.println("> Wrong parameters, try help");
		} else if (command instanceof BankNewAccount) {
			LOG.debug("Create new bank acount");
			try {
				Account account = bank
						.newAccount(command.getClient().getName());
				account.deposit(((BankNewAccount) command).getInitialBalance());
				System.out.println("> Account created\n");
			} catch (RejectedException ex) {
				LOG.info("Could not create new account");
				System.out.println("> " + ex.getMessage());
			}
		} else if (command instanceof RegisterMarketplace) {
			LOG.debug("Register user to marketplace");

			// Unexport previously exported callbacks
			try {
				UnicastRemoteObject.unexportObject(UserCache.getInstance().getCallbacks(), true);
			} catch (NoSuchObjectException ex) {
				LOG.debug("No previous callbacks exported");
			}
			
			// If this is inside the try-cache block we will have troubles
			// when quit the program. It will hang waiting for exported callbacks
			UserCache.getInstance().setCallbacks(
					((RegisterMarketplace) command).getCallbacks());
			
			try {
				market.register(command.getClient(),
						((RegisterMarketplace) command).getCallbacks());
				// currentUser = command.getClient();
				UserCache.getInstance().setCurrentUser(command.getClient());
				
				System.out.println("> User registered");
			} catch (NoBankAccountException ex) {
				LOG.debug("User does not have a bank account");
				System.out.println("> " + ex.getMessage());
			} catch (UserAlreadyExists ex) {
				LOG.debug(ex.getMessage());
				System.out.println("> " + ex.getMessage());
			}
		} else if (command instanceof UnregisterMarketplace) {
			LOG.debug("Unregistering user: {}", command.getClient().getEmail());

			try {
				market.unregister(command.getClient().getEmail());
			} catch (NoUserException ex) {
				System.out
						.println("> The user you are trying to unregister, does not exist");
			}

		} else if (command instanceof SellCommand) {
			LOG.debug("Sell an item");

			try {
				market.sell(command.getClient().getEmail(),
						((SellCommand) command).getItemName(),
						((SellCommand) command).getPrice());
			} catch (NoUserException ex) {
				System.out.println("> " + ex.getMessage());
			}
		} else if (command instanceof ListCommand) {
			LOG.debug("List command");
			List<BaseItem> items = market.listItems();

			if (items.size() == 0) {
				System.out.println("> No items are available for buying");
			} else {
				for (BaseItem item : items) {
					System.out.println("> Name: " + item.getName() + " Price: "
							+ item.getPrice());
				}
			}
		} else if (command instanceof BuyCommand) {
			LOG.debug("Buy command");
			try {
				market.buy(command.getClient().getEmail(),
						((BuyCommand) command).getItemName());
			} catch (ItemDoesNotExists | NoUserException | BankBalance ex) {
				System.out.println("> " + ex.getMessage());
			}
		} else if (command instanceof Help) {
			System.out.println("> Help menu");
			for (Commands com : Commands.values()) {
				System.out.println(com);
			}
		} else if (command instanceof Exit) {
			System.out.println("> Bye");
			try {
				UnicastRemoteObject.unexportObject(UserCache.getInstance()
						.getCallbacks(), true);
			} catch (NoSuchObjectException ex) {
				ex.printStackTrace();
			}
			exit();
		}
	}

	private boolean isRunning() {
		return running;
	}

	private void exit() {
		running = false;
	}
}
