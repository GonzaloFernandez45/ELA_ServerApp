package interfaces;
import pojos.Administrator;

public interface AdministratorManager {
    public void insertAdministrator(Administrator administrator);
    public Administrator getAdministratorByDNI(String dni);
}
