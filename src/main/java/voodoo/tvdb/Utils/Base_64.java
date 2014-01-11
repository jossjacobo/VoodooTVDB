package voodoo.tvdb.Utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Base_64 {
	
	public static String encode(String password){
		
		String base64 = "";
		
		try {
			
			base64 = Base64.encodeToString(password.getBytes("iso-8859-1"), Base64.DEFAULT);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return password;
		}
		
		return base64;
	}

}
