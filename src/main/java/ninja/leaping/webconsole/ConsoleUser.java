package ninja.leaping.webconsole;

import java.security.Principal;

public class ConsoleUser implements Principal {

    public String getName() {
        return null;
    }

    boolean authenticate(String password) {
        return false;
    }

    public boolean authenticate(String password, String totp) {
        return false;
    }

    public String getTotpCode() {
        return null;
    }

}
