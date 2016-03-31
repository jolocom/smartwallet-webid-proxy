/*
 *  WebID-TLS Proxy
 *  Copyright (C) 2016 Danube Tech GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.danubetech.webidproxy.test;


import com.danubetech.webidproxy.ssl.AllTrustManager;
import com.danubetech.webidproxy.users.Users;
import com.danubetech.webidproxy.users.UsersFileImpl;

public class TestRegisterWebID {

	public static void main(String args[]) throws Exception {

		AllTrustManager.enable();

		Users users = new UsersFileImpl();

		users.register("lalala1", "mypw", "Lal Laaa", "lala@gmail.com");
	}
}
