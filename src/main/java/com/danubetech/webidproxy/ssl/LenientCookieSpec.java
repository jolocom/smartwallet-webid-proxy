package com.danubetech.webidproxy.ssl;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.util.TextUtils;

public class LenientCookieSpec extends BrowserCompatSpec {

	public LenientCookieSpec() {
	
		super();
		
		registerAttribHandler(ClientCookie.EXPIRES_ATTR, new BasicExpiresHandler(null) {

			@Override 
			public void parse(SetCookie cookie, String value) throws MalformedCookieException {
				if (TextUtils.isEmpty(value)) {
					// You should set whatever you want in cookie
					cookie.setExpiryDate(null);
				} else {
					super.parse(cookie, value);
				}
			}
		});
	}
}