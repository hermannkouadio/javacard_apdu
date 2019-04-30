package com.bng.javarmi.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;

public class Start {
	public static final String[][] HEX = new String[][] { { "A", "10" }, { "B", "11" }, { "C", "12" }, { "D", "13" },
			{ "E", "114" }, { "F", "15" } };
	private static InputStreamReader isr = null;
	private static BufferedReader br = null;

	public static void main(String[] args) {
		if (isr == null) {
			isr = new InputStreamReader(System.in);
			br = new BufferedReader(isr);
		}

		CadClientInterface cad;
		Socket sock;

		// show the list of available terminals
		try {
			TerminalFactory factory = TerminalFactory.getDefault();
			List<CardTerminal> terminals = factory.terminals().list();
			int i = 0;
			for (CardTerminal cardTerminal : terminals) {
				System.out.println("No-" + i + "\t" + cardTerminal.getName());
				++i;
			}
			System.out.print("\nSelect target card: ");
			int resp = Integer.parseInt(br.readLine());
			CardTerminal terminal = terminals.get(resp);
			System.out.println("\nCard \"" + terminals.get(resp).getName() + "\" selected.");

			// APDU Send Ins
			byte cla, ins, p1, p2, lc, data, le;
			cla = (byte) 0xB0;
			ins = (byte) 0x20;
			p1 = p2 = (byte) 0x00;
			byte[] app_aid = new byte[] { 0x00, 0x00, 0x01, 0x11, 0x11, 0x10 };

			if (terminal.isCardPresent()) {
				System.out.println("Card is present");
			} else {
				System.out.println("No card");
			}

			// establish a connection with the card
			Card card = terminal.connect("T=0");

			sock = new Socket("localhost", 63336);
			InputStream is = sock.getInputStream();
			OutputStream os = sock.getOutputStream();
			cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T0, is, os);

			cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_PCSC, null, null);

			CardChannel channel = card.getBasicChannel();

			System.out.println("\nTerminal: " + terminal.getName() + "\nCard: " + card + "\nChannel: " + channel);

			// APDU Select
			byte _cla, _ins, _p1, _p2, _lc, _le;
			_cla = (byte) 0x00;
			_ins = (byte) 0xA4;
			_p1 = (byte) 0x04;
			_p2 = (byte) 0x00;
			_lc = (byte) 0x08;
			byte[] _data = new byte[] { 0x00, 0x00, 0x01, 0x11, 0x11, 0x10 };
			_le = (byte) 00;

			ResponseAPDU r = channel.transmit(new CommandAPDU(_cla, _ins, _p1, _p2, _data, _le));
			String sw1 = sw1sw2ToHexa(r)[0];
			String sw2 = sw1sw2ToHexa(r)[1];
			System.out.println("\nResponse: " + sw1 + " " + sw2);
			System.out.println("\nResponse: " + String.valueOf(r.getSW1()) + " " + String.valueOf(r.getSW2()));
			// List card applications
			/*
			 * for ( b : c1) {
			 * 
			 * }
			 */
			// disconnect
			// card.disconnect(false);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static String[] sw1sw2ToHexa(ResponseAPDU rapdu) {
		String[] res = new String[2];
		res[0] = fromDecToHex(rapdu.getSW1());
		res[1] = fromDecToHex(rapdu.getSW2());
		return res;
	}

	private static String fromDecToHex(int in) {
		String res = "", rDigit = "";
		int q = in, r = 0, base = 16;
		do {
			q = in / base;
			r = in % base;
			// cast r value
			rDigit = String.valueOf(r);
			if (r > 9 && r < 16) {
				for (String[] arr : HEX) {
					if (arr[1].equals(String.valueOf(rDigit))) {
						rDigit = arr[0];
						break;
					}
				}
			}
			if (q >= 10 && q < base) {
				for (String[] arr : HEX) {
					if (arr[1].equals(String.valueOf(q))) {
						res += arr[0];
						if (r < base) {
							res = rDigit + res;
						}
						break;
					}
				}
			} else if (q < 10) {
				res = "" + q + rDigit + res;
			} else {
				res = "" + rDigit + res;
			}
			if (q > base) {
				in = q;
			} else {
				break;
			}
			// res = reverse(res);
		} while (r != 0);
		return writeOntwoDigit(res);
	}

	private static String writeOntwoDigit(String in) {
		String out = "";
		if (in.length() % 2 == 1) {
			in = "0"+in;
		}
		for(int i=0; i<in.length(); i++) {
			out += in.charAt(i);
			if(i%2!=0) {
				out +=":";
			}
		}
		in = out.charAt(out.length()-1) == ':' ? out.substring(0, out.length()-1):out;
		return in;
	}

	/*
	 * private static String reverse(String str) { String temp = ""; if
	 * (str.length() > 1) { for (int i = str.length() - 1; i >= 0; i--) { temp +=
	 * str.charAt(str.length()); } str = temp; } return str; }
	 */
}
