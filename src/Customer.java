import java.sql.*;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class Customer {
    private Long id;
    private String name;
    private String surname;
    private String address;
    private String identityCard;
    private String phoneNumber;

    /*public Customer() {
        throw new UnsupportedOperationException("not implemented");
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {this.id = id;}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdentityCard() {
        return identityCard;
    }

    public void setIdentityCard(String identityCard) {
        this.identityCard = identityCard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        if (address != null ? !address.equals(customer.address) : customer.address != null) return false;
        if (!id.equals(customer.id)) return false;
        if (!identityCard.equals(customer.identityCard)) return false;
        if (!name.equals(customer.name)) return false;
        if (!surname.equals(customer.surname)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + identityCard.hashCode();
        return result;
    }

    public static void main(String[] args) throws Exception{
        String url="jdbc:derby://localhost:1527/dragonDB";

        try (Connection conn = DriverManager.getConnection(url, "mitko", "123456789")) {
            try (PreparedStatement st = conn.prepareStatement("SELECT usr FROM DRAGONDB.persons")) {
                st.execute();
                ResultSet rs = st.getResultSet();

                while (rs.next()) {
                    String a = rs.getString(1);
                    System.out.println(a);
                }

            }
        }
    }
}
