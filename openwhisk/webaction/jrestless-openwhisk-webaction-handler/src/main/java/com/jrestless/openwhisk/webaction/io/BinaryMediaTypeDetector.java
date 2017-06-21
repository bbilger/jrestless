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
package com.jrestless.openwhisk.webaction.io;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

/**
 * Utility class to check if a media type is considered to be a "binary" media
 * type by OpenWhisk, or not.
 *
 * @author Bjoern Bilger
 *
 */
final class BinaryMediaTypeDetector {

	private BinaryMediaTypeDetector() {
	}

	/**
	 * all "notBinary" content types - as defined in Spray which is used by OpenWhisk
	 * <a href="https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282">
	 * https://github.com/spray/spray/blob/master/spray-http/src/main/scala/spray/http/MediaType.scala#L282</a>.
	 */
	private static final Set<String> NON_BINARY_CONTENT_TYPES;
	static {
		Set<String> nonBinaryContentTypes = new HashSet<>();
		nonBinaryContentTypes.add(MediaType.APPLICATION_ATOM_XML);
		nonBinaryContentTypes.add("application/javascript");
		nonBinaryContentTypes.add("application/rss+xml");
		nonBinaryContentTypes.add("application/soap+xml");
		nonBinaryContentTypes.add("application/vnd.google-earth.kml+xml");
		nonBinaryContentTypes.add("application/x-vrml");
		nonBinaryContentTypes.add("application/x-www-form-urlencoded");
		nonBinaryContentTypes.add("application/xhtml+xml");
		nonBinaryContentTypes.add("application/xml-dtd");
		nonBinaryContentTypes.add("application/xml");
		nonBinaryContentTypes.add("image/svg+xml");
		nonBinaryContentTypes.add("message/http");
		nonBinaryContentTypes.add("message/delivery-status");
		nonBinaryContentTypes.add("message/rfc822");
		nonBinaryContentTypes.add("text/asp");
		nonBinaryContentTypes.add("text/cache-manifest");
		nonBinaryContentTypes.add("text/calendar");
		nonBinaryContentTypes.add("text/css");
		nonBinaryContentTypes.add("text/csv");
		nonBinaryContentTypes.add("text/html");
		nonBinaryContentTypes.add("text/mcf");
		nonBinaryContentTypes.add("text/plain");
		nonBinaryContentTypes.add("text/richtext");
		nonBinaryContentTypes.add("text/tab-separated-values");
		nonBinaryContentTypes.add("text/uri-list");
		nonBinaryContentTypes.add("text/vnd.wap.wml");
		nonBinaryContentTypes.add("text/vnd.wap.wmlscript");
		nonBinaryContentTypes.add("text/x-asm");
		nonBinaryContentTypes.add("text/x-c");
		nonBinaryContentTypes.add("text/x-component");
		nonBinaryContentTypes.add("text/x-h");
		nonBinaryContentTypes.add("text/x-java-source");
		nonBinaryContentTypes.add("text/x-pascal");
		nonBinaryContentTypes.add("text/x-script");
		nonBinaryContentTypes.add("text/x-scriptcsh");
		nonBinaryContentTypes.add("text/x-scriptelisp");
		nonBinaryContentTypes.add("text/x-scriptksh");
		nonBinaryContentTypes.add("text/x-scriptlisp");
		nonBinaryContentTypes.add("text/x-scriptperl");
		nonBinaryContentTypes.add("text/x-scriptperl-module");
		nonBinaryContentTypes.add("text/x-scriptphyton");
		nonBinaryContentTypes.add("text/x-scriptrexx");
		nonBinaryContentTypes.add("text/x-scriptscheme");
		nonBinaryContentTypes.add("text/x-scriptsh");
		nonBinaryContentTypes.add("text/x-scripttcl");
		nonBinaryContentTypes.add("text/x-scripttcsh");
		nonBinaryContentTypes.add("text/x-scriptzsh");
		nonBinaryContentTypes.add("text/x-server-parsed-html");
		nonBinaryContentTypes.add("text/x-setext");
		nonBinaryContentTypes.add("text/x-sgml");
		nonBinaryContentTypes.add("text/x-speech");
		nonBinaryContentTypes.add("text/x-uuencode");
		nonBinaryContentTypes.add("text/x-vcalendar");
		nonBinaryContentTypes.add("text/x-vcard");
		nonBinaryContentTypes.add("text/xml");
		NON_BINARY_CONTENT_TYPES = Collections.unmodifiableSet(nonBinaryContentTypes);
	}

	/**
	 * checks if a media type is considered to be a "binary" media type by
	 * OpenWhisk, or not.
	 *
	 * @param mediaType
	 * @return {@code true} if it's a binary media type, {@code false} else
	 */
	static boolean isBinaryMediaType(MediaType mediaType) {
		if (mediaType == null) {
			return true;
		}
		return !NON_BINARY_CONTENT_TYPES.contains(mediaType.toString());
	}
}
