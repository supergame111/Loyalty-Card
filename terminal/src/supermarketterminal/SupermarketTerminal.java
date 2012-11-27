package supermarketterminal;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;

import common.CLI;
import common.KeyManager;
import common.CONSTANTS;
import common.AppletCommunication;
import common.AppletSession;
import common.Formatter;
import common.Response;
import common.TerminalCrypto;

public class SupermarketTerminal {
	
	/** communication gateway */
	AppletCommunication com;

	/** communication session */
	AppletSession session;

	/** Cryptographic functions */
	TerminalCrypto crypto;

	/** Issuer public key */
	RSAPublicKey supermarketPublicKey;

	/** Cash register's private key */
	RSAPrivateKey privKey;

	/** keys directory */
	String keyDir = "./keys/";

	/** Current card id */
	int cardId;
	
	/** Current cash register id */
	int cashRegisterId;
	
	public SupermarketTerminal (int cashRegisterId) {
		this.cashRegisterId = cashRegisterId;
		System.out.println("Welcome to cash register " + cashRegisterId);
		loadKeyFiles();
		
		session = new AppletSession(supermarketPublicKey, privKey, cashRegisterId);
		com = new AppletCommunication(session);
		crypto = new TerminalCrypto();
		
		while (true) {
			main();
			System.out.print("\nPress return when card has been inserted.");
			waitForInput();
			waitToTryAgain();
		}
	}
	
	private void loadKeyFiles() {
		try {
			String cashRegister = "CashRegister_" + this.cashRegisterId;
			privKey = (RSAPrivateKey) KeyManager.loadKeyPair(cashRegister).getPrivate();
			supermarketPublicKey = (RSAPublicKey) KeyManager.loadKeyPair("supermarket")
					.getPublic();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void main() {
		// wait until a card is inserted
		com.waitForCard();
		// authenticate the card and the terminal
		System.out.println("Authenticating card...");
		if (!session.authenticate(CONSTANTS.P1_AUTHENTICATE_SUPERMARKET)) {
			System.err.println("Authentication error.");
			return;
		}
		cardId = session.getCardId();
		
		System.out.println("Succesfully authenticated card " + cardId);
		
		int input = Integer.parseInt(CLI.prompt("1: add credits to card | 2: ... "));
		
	}
	
	/**
	 * Waits for the user to press any key
	 */
	private void waitForInput() {
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits 5 seconds
	 */
	private void waitToTryAgain() {
		sleep(1);
		System.out.print("Try to authenticate card again in ");
		for (int i = 5; i > 0; i--) {
			System.out.print(i + "... ");
			sleep(1);
		}
		System.out.println();
	}

	/**
	 * Sleep for x seconds
	 */
	private void sleep(int x) {
		try {
			Thread.sleep(1000 * x);
		} catch (InterruptedException e) {
			System.err.println("Session interrupted!");
		}
	}

	/**
	 * Start up the supermarket terminal
	 * 
	 * @param arg
	 */
	public static void main(String[] arg) {
		// Change integer to x to represent another supermarket with ID x
		new SupermarketTerminal(1);
	}
}