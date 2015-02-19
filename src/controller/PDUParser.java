package controller;

import java.io.IOException;
import java.io.InputStream;

import model.OpCodes;
import model.PDU;


public final class PDUParser {

	/**
	 * Parses an incoming message PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed message PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseMessage(InputStream inputStream) throws IOException {
		PDU pdu = new PDU(12);
		pdu.setByte(0, (byte) OpCodes.MESSAGE);
		byte[] buffer = new byte[11];
		inputStream.read(buffer, 0, 11);
		pdu.setSubrange(1, buffer);

		int nickLength = pdu.getByte(2);
		int messageLength = pdu.getShort(4);
		int paddedLength = Client.getPaddedLength(nickLength,
				messageLength);

		pdu.extendTo(12+paddedLength);
		buffer = new byte[paddedLength];
		inputStream.read(buffer, 0, paddedLength);
		pdu.setSubrange(12, buffer);

		return pdu;
	}

	/**
	 * Parses an incoming nicknames PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed nicknames PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseNicknames(InputStream inputStream)
			throws IOException {
		PDU pdu = new PDU(4);
		pdu.setByte(0, (byte) OpCodes.NICKS);
		byte[] buffer = new byte[3];
		inputStream.read(buffer, 0, 3);
		pdu.setSubrange(1, buffer);

		int paddedNamesLength = Client.getPaddedLength(pdu.getShort(2));
		pdu.extendTo(4 + paddedNamesLength);
		buffer = new byte[paddedNamesLength];
		inputStream.read(buffer, 0, paddedNamesLength);
		pdu.setSubrange(4, buffer);
		return pdu;
	}

	/**
	 * Parses an incoming user join PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed user join PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseUserJoin(InputStream inputStream)
			throws IOException {
		PDU pdu = new PDU(8);
		pdu.setByte(0, (byte) OpCodes.UJOIN);
		byte[] buffer = new byte[7];
		inputStream.read(buffer, 0, 7);
		pdu.setSubrange(1, buffer);

		int paddedNameLength = Client.getPaddedLength(pdu.getByte(1));
		pdu.extendTo(8 + paddedNameLength);
		buffer = new byte[paddedNameLength];
		inputStream.read(buffer, 0, paddedNameLength);
		pdu.setSubrange(8, buffer);

		return pdu;
	}

	/**
	 * Parses an incoming user leave PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed user leave PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseUserLeave(InputStream inputStream)
			throws IOException {
		PDU pdu = parseUserJoin(inputStream);
		pdu.setByte(0, (byte) OpCodes.ULEAVE);
		return pdu;
	}

	/**
	 * Parses an incoming change nickname PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed change nickname PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseChangeNickname(InputStream inputStream)
			throws IOException {
		PDU pdu = new PDU(8);
		pdu.setByte(0, (byte) OpCodes.UCNICK);
		byte[] buffer = new byte[7];
		inputStream.read(buffer, 0, 7);
		pdu.setSubrange(1, buffer);

		int nameLength1 = pdu.getByte(1);
		int nameLength2 = pdu.getByte(2);
		int totalPaddedLength = Client.getPaddedLength(nameLength1,
				nameLength2);
		pdu.extendTo(8 + totalPaddedLength);
		buffer = new byte[totalPaddedLength];
		inputStream.read(buffer, 0, totalPaddedLength);
		pdu.setSubrange(8, buffer);

		return pdu;
	}

	/**
	 * Parses an incoming quit PDU.
	 * @param inputStream an input stream from the socket receiving the PDU.
	 * @return a parsed quit PDU.
	 * @throws IOException if an I/O exception occurs during the reading.
	 */
	public static PDU parseQuit(InputStream inputStream) throws IOException {
		PDU pdu = new PDU(4);
		byte[] buffer = new byte[3];
		inputStream.read(buffer, 0, 3);
		pdu.setSubrange(1, buffer);
		return pdu;
	}
}
