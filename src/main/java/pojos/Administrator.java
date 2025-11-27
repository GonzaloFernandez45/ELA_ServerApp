package pojos;

public class Administrator {
    private Integer id;
    private String email;
    private String dni;



    public Administrator() {
    }


    public Administrator(String dni,String email) {
        this.dni = dni;
        this.email = email;

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

    public String getEmail() { return  email; }

    public void setEmail(String email) { this.email = email; }


    public String getDni() {
        return dni;
    }


    public void setDni(String dni) {
        this.dni = dni;
    }





    @Override
    public String toString() {
        return "Administrator{"
                + "id=" + id
                + ", email='" + email + '\''
                + ", dni='" + dni + '\''
                + '}';
    }
}
