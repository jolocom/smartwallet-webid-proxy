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
package com.danubetech.webidproxy.users;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UsersMemoryImpl implements Users {

	private Map<String, User> users = new HashMap<String, User> ();

	@Override
	public boolean exists(String username) {

		return this.users.containsKey(username);
	}

	@Override
	public User register(String username, String password, String name, String email) {

		if (this.get(username) != null) throw new RuntimeException("User '" + username + "' exists already.");

		User user = new User(username, password, name, email);

		try {

			WebIDRegistration.registerWebIDAccount(user);
		} catch (IOException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		this.users.put(username, user);
		
		return user;
	}

	@Override
	public User get(String username) {

		return this.users.get(username);
	}
}
