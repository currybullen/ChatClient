package controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A class used to decompress and compress messages using GZIP.
 * @author c12mkn
 *
 */
public class GZIP {

	/**
	 * Compresses a given message using GZIP compression.
	 * @param message a message to be compressed.
	 * @return the compressed message.
	 * @throws Exception if the message couldn't be compressed.
	 */
	public static byte[] compress(byte[] message) throws Exception {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
			zipStream.write(message);
			zipStream.close();
			byteStream.close();

			return byteStream.toByteArray();
	}

	/**
	 *
	 * @param message a message to be decompressed.
	 * @param length the length of the message decompressed.
	 * @return the decompressed message.
	 */
	public static byte[] decompress(byte[] message, int length)
			throws Exception  {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		GZIPInputStream zipStream;
		zipStream = new GZIPInputStream(new ByteArrayInputStream(message));

		for (int i = 0; i < length; i++) {
			byteStream.write(zipStream.read());
		}
		zipStream.close();
		byteStream.close();

		return byteStream.toByteArray();
	}
}
