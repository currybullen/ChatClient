package controller;

import model.CompressAlgorithms;
import model.CryptAlgorithms;
import model.OpCodes;
import model.PDU;

/**
 * A class used to construct different PDU's.
 * @author c12mkn
 *
 */
public final class PDUFactory {

	/**
	 * Returns a server list request PDU.
	 * @return a server list request PDU.
	 */
	static public PDU makeServerListRequest() {
		PDU pdu = new PDU(4);
		pdu.setByte(0, (byte) OpCodes.GETLIST);
		pdu.setByte(1, (byte) 0);
		pdu.setByte(2, (byte) 0);
		pdu.setByte(3, (byte) 0);

		return pdu;
	}

	/**
	 * Returns a join request PDU.
	 * @param nickname the nickname of the user to join in bytes.
	 * @return a join request PDU.
	 */
	static public PDU makeJoinRequest(byte[] nickname) {
		PDU pdu = new PDU(4 + Client.getPaddedLength(nickname.length));
		pdu.setByte(0, (byte) OpCodes.JOIN);
		pdu.setByte(1, (byte) nickname.length);
		pdu.setSubrange(4, nickname);

		return pdu;
	}

	/**
	 * Returns a message PDU.
	 * @param messageType the type of message to be sent.
	 * @param message the message in bytes.
	 * @return a message PDU.
	 */
	static public PDU makeMessage(int messageType, byte[] message) {
		PDU pdu = new PDU(12 + Client.getPaddedLength(message.length));

		pdu.setByte(0, (byte) OpCodes.MESSAGE);
		pdu.setByte(1, (byte) messageType);
		pdu.setShort(4, (short) message.length);
		pdu.setSubrange(12, message);

		pdu.setByte(3, Checksum.calc(pdu.getBytes(), pdu.length()));

		return pdu;
	}

	/**
	 * Returns a compressed message PDU.
	 * @param algorithm the algorithm to be used.
	 * @param message the message in bytes.
	 * @return a message PDU.
	 * @throws Exception if the message couldn't be compressed.
	 */
	static public PDU makeCompressedMessage(int algorithm, byte[] message)
			throws Exception {
		if (algorithm == CompressAlgorithms.GZIP) {
			byte[] compressedMessage = GZIP.compress(message);

			PDU pdu = new PDU(8 + Client.getPaddedLength(compressedMessage.
					length));
			pdu.setByte(0, (byte) algorithm);
			pdu.setShort(2, (short) compressedMessage.length);
			pdu.setShort(4, (short) message.length);
			pdu.setSubrange(8, compressedMessage);
			pdu.setByte(1, Checksum.calc(pdu.getBytes(), pdu.length()));

			return pdu;
		} else {
			return null;
		}
	}

	/**
	 * Returns an encrypted message PDU.
	 * @param algorithm the encryption algorithm to be used.
	 * @param message the message in bytes.
	 * @param cryptKey the encryption key to be used.
	 * @return a message PDU.
	 */
	static public PDU makeEncryptedMessage(int algorithm, byte[] message,
			String cryptKey) {
		if (algorithm == CryptAlgorithms.STANDARD) {
			PDU pdu = new PDU(8);
			pdu.setByte(0, (byte) algorithm);
			pdu.setShort(4, (short) message.length);
			Crypt.encrypt(message, message.length, cryptKey.getBytes(),
					cryptKey.length());
			pdu.setShort(2, (short) message.length);
			pdu.extendTo(8 + Client.getPaddedLength(message.length));
			pdu.setSubrange(8, message);
			pdu.setByte(1, Checksum.calc(pdu.getBytes(), pdu.length()));

			return pdu;
		} else {
			return null;
		}
	}

	/**
	 * Returns a change nickname PDU.
	 * @param nickname the nickname to change to in bytes.
	 * @return a change nickname PDU.
	 */
	static public PDU makeChangeNicknameMessage(String nickname) {
		PDU pdu = new PDU(4 + Client.getPaddedLength(nickname.length()));
		pdu.setByte(0, (byte) OpCodes.CHNICK);
		pdu.setByte(1, (byte) nickname.length());
		pdu.setSubrange(4, nickname.getBytes());

		return pdu;
	}

	/**
	 * Returns a quit PDU.
	 * @return a quit PDU.
	 */
	public static PDU makeQuit() {
		PDU pdu = new PDU(4);
		pdu.setByte(0, (byte) OpCodes.QUIT);

		return pdu;
	}
}
