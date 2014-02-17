package voodoo.tvdb.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA1Hash {
	
	private static String convertToHex(byte[] data){
		
		StringBuilder buffer = new StringBuilder();
		
		for(byte b: data){
			
			int halfbyte = (b >>> 4) & 0x0F;
			
			int two_halfs = 0;
			
			do{
				
				buffer.append( (0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0X0F;
				
			}while(two_halfs++ < 1);
			
		}
		
		return buffer.toString();
		
	}
	
	public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		
		return convertToHex(sha1hash);
		
	}
	
}
