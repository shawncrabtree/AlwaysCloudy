package com.example.alwayssunny;

import org.apache.commons.codec.binary.Base64;

public class SecurityService {
	public static String decode(String toDecode) {
		String decoded = new String(Base64.decodeBase64(toDecode.getBytes()));
		
		return decoded;
	}
}
