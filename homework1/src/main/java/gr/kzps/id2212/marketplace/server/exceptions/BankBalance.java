package gr.kzps.id2212.marketplace.server.exceptions;

/**
 * Something is wrong with the bank balance
 */
public class BankBalance extends Exception {

	private static final long serialVersionUID = 3469453928738865215L;

	public BankBalance(String msg) {
		super(msg);
	}
}
