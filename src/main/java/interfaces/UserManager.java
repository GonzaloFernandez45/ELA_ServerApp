package interfaces;

import pojos.Role;
import pojos.User;

public interface UserManager {
    public void addUser(User user);
    public User checkPassword(String password, String email);
}
