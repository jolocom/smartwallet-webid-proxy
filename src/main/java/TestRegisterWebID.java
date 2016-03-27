

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
