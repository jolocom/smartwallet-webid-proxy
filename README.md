This is a proxy service for WebID.

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

	http://localhost:8111/proxy/https://testuser1.localhost:8443/profile/card

### User management

Register:

	POST: http://localhost:8111/register
	Parameters: username, password, name (optional), email (optional)

Login:

	POST: http://localhost:8111/login
	Parameters: username, password

Logout:

	POST: http://localhost:8111/logout 

### How to build

Just run

    mvn clean install jetty:run

To build and run the WebID proxy.

Then open **http://localhost:8111** in your browser for a demo interface.
