This is an experimental proxy service for WebID.

It maintains a list of client certificates on behalf of agents.

Agents are authenticated with username and password, and a session is established.

See https://github.com/solid/solid/issues/22 for a description and discussion of the use case.  

### Information

E.g. if you want to access the resource

	https://testuser1.localhost:8443/profile/card

Then via the WebID proxy it would be

	http://localhost:8111/proxy/https://testuser1.localhost:8443/profile/card

### How to build

Just run

    mvn clean install jetty:run

To build and run the WebID proxy.

Then open http://localhost:8111 in your browser for a demo interface.
