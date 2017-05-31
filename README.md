This is a proxy service for WebID / SoLiD.

It maintains a list of TLS client certificates on behalf of agents.

Agents are authenticated with username and password, and a session is established.

See https://github.com/solid/solid/issues/22 for a description and discussion of the use case.  

**Warning:** This is highly experimental, do not use in a production environment.

### Information

"Normal" WebID-TLS flow:

	RDF Resource access:
	
	|------------|  WEBID-TLS   |--------------|
	| USER AGENT | -----------> | RDF RESOURCE |
	| * tls cert |              | * profile    |
	|            | <----------- |              |
	|            | RDF RESPONSE |              |
	|------------|              |--------------|

"Proxy" WebID-TLS flow:

	Proxy Authentication:
	
	|------------|    UN / PW   |---------------|
	| USER AGENT | -----------> | WEBID PROXY   |
	|            |              | * users       |
	|            | <----------- | * tls cert(s) |
	|            |    COOKIE    |               |
	|------------|              |---------------|
	
	RDF Resource Access:
	
	|------------|    COOKIE    |---------------|  WEBID-TLS   |--------------|
	| USER AGENT | -----------> | WEBID PROXY   | -----------> | RDF RESOURCE |
	| * cookie   |              | * users       |              | * profile    |
	|            | <----------- | * tls cert(s) | <----------- |              |
	|            | RDF RESPONSE |               | RDF RESPONSE |              |
	|------------|              |---------------|              |--------------|

E.g. if you want to access the resource

	https://testuser1.localhost:8443/profile/card

Then via the proxy you would instead access

	http://localhost:8111/proxy?url=https://testuser1.localhost:8443/profile/card

### User management

Register:

	POST: http://localhost:8111/register
	Parameters: username, password, name (optional), email (optional)

Verify E-Mail:

	POST: http://localhost:8111/verifyemail
	Parameters: username, code

Login:

	POST: http://localhost:8111/login
	Parameters: username, password

Logout:

	POST: http://localhost:8111/logout 

Forgot Password:

	POST: http://localhost:8111/forgotpassword
	Parameters: username

Reset Password:

	POST: http://localhost:8111/resetpassword
	Parameters: username, code, password

Export Private Key:

	REDIRECT: http://localhost:8111/exportkey

Import Private Key:

	POST: http://localhost:8111/importkey
	Parameters: .p12 file (multipart/form-data)

Users are stored in **./users/** and also created on the SoLiD server.

For each user, there is a key/value text file and a PKCS#12 key store.

### How to build

Just run

    mvn clean install jetty:run

To build and run the WebID proxy.

The **host** and **port** of the SoLiD server can be configured as follows:

    mvn clean install jetty:run -Dwebid.host=my.host.com:8443

Support for **vhosts** style WebIDs can be enabled as follows:

    mvn clean install jetty:run -Dvhosts

Then open **http://localhost:8111** in your browser for a demo interface.
