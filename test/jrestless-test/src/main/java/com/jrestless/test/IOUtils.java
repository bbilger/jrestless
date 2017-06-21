/*
 * Copyright 2017 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * I/O utilities.
 *
 * @author Bjoern Bilger
 *
 */
public final class IOUtils {

	private static final int BUFFER_LENGTH = 1024;

	private IOUtils() {
	}

	/**
	 * Converts the given input stream into a byte array.
	 * <p>
	 * Possible {@link IOException} are wrapped into a {@link RuntimeException}.
	 *
	 * @param is
	 *            the input stream to read from
	 * @return the bytes read from the input stream
	 */
	public static byte[] toBytes(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[BUFFER_LENGTH];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * Converts the given input stream into a string with UTF8 as charset.
	 *
	 * @see #toString(InputStream, Charset)
	 * @param is
	 *            the input stream to read from
	 * @return the string created from the given input stream
	 */
	public static String toString(InputStream is) {
		return toString(is, StandardCharsets.UTF_8);
	}


	/**
	 * Converts the given input stream into a string with the given charset.
	 *
	 * @see #toBytes(InputStream)
	 * @param is
	 *            the input stream to read from
	 * @param charset
	 *            the charset for the created string
	 * @return the string created from the given input stream
	 */
	public static String toString(InputStream is, Charset charset) {
		return new String(toBytes(is), charset);
	}

}
