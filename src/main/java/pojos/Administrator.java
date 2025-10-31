package pojos;

public class Administrator {
    private int id;
    private String dni;
    private String password;


    public Administrator() {
    }


    public Administrator(String dni, String password) {
        this.dni = dni;
        this.password = password;
    }

    public Administrator(Integer id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getDni() {
        return dni;
    }


    public void setDni(String dni) {
        this.dni = dni;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "Administrator{"
                + "id=" + id
                + ", dni='" + dni + '\''
                + ", password='" + password + '\''
                + '}';
    }
}
