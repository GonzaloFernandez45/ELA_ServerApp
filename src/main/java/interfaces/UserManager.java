package interfaces;

import pojos.Role;
import pojos.User;

public interface UserManager {
    /**
     * Handles core user operations such as registration and authentication.
     * Implementations manage DB access and password verification.
     */
    public void addUser(User user);

    /**
     * Validates a password for the user with the given email.
     * Typically: retrieve user → hash entered password + stored salt → compare.
     *
     * @param password plain-text password entered by the user.
     * @param email    user email (unique identifier).
     * @return true if password is correct, false otherwise.
     */
    public boolean checkPassword(String password, String email);
}
