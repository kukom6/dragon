import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class CustomerManagerImpl implements CustomerManager {
    private final DataSource source;
    private final static Logger log = LoggerFactory.getLogger(CustomerManager.class);

    public CustomerManagerImpl(DataSource dataSource) {
        this.source = dataSource;
    }

    @Override
    public void createCustomer(Customer customer) {
        log.debug("create customer");
        if(customer==null){throw new IllegalArgumentException("Customer is null");}
        if(customer.getId()!=null) {throw new IllegalArgumentException("Customer is in DB");}

        checkCustomerArgument(customer);

        try(Connection conn = source.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO CUSTOMERS " +
                            "(\"NAME\",SURNAME,ADDRESS,IDENTITYCARD,PHONENUMBER) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)){
                st.setString(1,customer.getName());
                st.setString(2,customer.getSurname());
                st.setString(3,customer.getAddress());
                st.setString(4,customer.getIdentityCard());
                st.setString(5,customer.getPhoneNumber());
                int numbUpdate = st.executeUpdate();
                if(numbUpdate!=1){
                    throw new ServiceFailureException("Create more update also one");
                }
                ResultSet keyRS = st.getGeneratedKeys();
                customer.setId(getKey(keyRS, customer));
            }
        }catch (SQLException ex) {
            log.error("db connection problem or two customer have same IDCard", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Customer getCustomerByID(Long id) {
        log.debug("get customer by ID from DB");
        if(id==null){
            throw new IllegalArgumentException("argumentis null");
        }
        if(id < 0){
            throw new IllegalArgumentException("id is negative or zero");
        }
        Customer customer;
        try(Connection conn = source.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT ID,\"NAME\",SURNAME,ADDRESS,IDENTITYCARD,PHONENUMBER " +
                    "FROM CUSTOMERS WHERE ID=?")){
                st.setLong(1,id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    customer=resultToCustomer(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException("More customers have same ID, ID: "+ id + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                    }
                    return customer;
                } else {
                    return null;
                }
            }
        }catch(SQLException ex){
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Customer getCustomerByIDCard(String idCard){
        log.debug("get customer by number idcard from DB");
        if(idCard==null||idCard.isEmpty()){
            throw new IllegalArgumentException("idCard is null or empty string");
        }
        Customer customer;
        try(Connection conn = source.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT ID,\"NAME\",SURNAME,ADDRESS,IDENTITYCARD,PHONENUMBER " +
                    "FROM CUSTOMERS WHERE IDENTITYCARD=?")){
                st.setString(1,idCard);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    customer=resultToCustomer(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException("More customers have same number ID card, ID card" +
                                "number is : "+ idCard + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                    }
                    return customer;
                } else {
                    return null;
                }
            }
        }catch(SQLException ex){
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Collection<Customer> getAllCustomers() {
        log.debug("get all customers from DB");
        List<Customer> customers = new ArrayList<>();
        try(Connection conn = source.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM CUSTOMERS")) {
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    customers.add(resultToCustomer(rs));
                }
                return Collections.unmodifiableCollection(customers);
            }
        }catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Collection<Customer> getAllCustomersByName(String name,String surname){
        log.debug("get all customers from DB by name");

        if(name==null||surname==null||name.isEmpty()||surname.isEmpty()){
            throw new IllegalArgumentException("Name or surname or both are valid");
        }

        List<Customer> customers = new ArrayList<>();
        try(Connection conn = source.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM CUSTOMERS WHERE \"NAME\"=? AND SURNAME=?")) {
                st.setString(1,name);
                st.setString(2,surname);
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    customers.add(resultToCustomer(rs));
                }
                return Collections.unmodifiableCollection(customers);
            }
        }catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        log.debug("update Customer");
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer isn't in DB");
        }

        checkCustomerArgument(customer);

        try (Connection conn = source.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE CUSTOMERS " +
                    "SET \"NAME\"=?, SURNAME=?, ADDRESS=?, IDENTITYCARD=?, PHONENUMBER=? WHERE ID=?")) {
                st.setString(1, customer.getName());
                st.setString(2, customer.getSurname());
                st.setString(3, customer.getAddress());
                st.setString(4, customer.getIdentityCard());
                st.setString(5, customer.getPhoneNumber());
                st.setLong(6, customer.getId());
                int numbUpdate = st.executeUpdate();
                if (numbUpdate != 1) {
                    throw new ServiceFailureException("UPDATE more update also one");
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem or two customer have same IDCard or invalid ID", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) {
        log.debug("delete customer from DB by name");
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer isn't in DB");
        }


        try (Connection conn = source.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("DELETE FROM CUSTOMERS WHERE ID=?")) {
                st.setLong(1,customer.getId());
                if(st.executeUpdate()!=1) {
                    throw new ServiceFailureException("Customer with id "+customer.getId()+" doesn't deleted");
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem or invalid ID", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    private Customer resultToCustomer(ResultSet rs) throws SQLException {
        Customer customer=new Customer();
        customer.setId(rs.getLong("ID"));
        customer.setName(rs.getString("NAME"));
        customer.setSurname(rs.getString("SURNAME"));
        customer.setAddress(rs.getString("ADDRESS"));
        customer.setIdentityCard(rs.getString("IDENTITYCARD"));
        customer.setPhoneNumber(rs.getString("PHONENUMBER"));
        return customer;
    }

    private void checkCustomerArgument(Customer customer) throws IllegalArgumentException{

        if(customer.getName()==null||customer.getName().isEmpty()){
            throw new IllegalArgumentException("Customer name is null or empty string");
        }
        if(customer.getSurname()==null||customer.getSurname().isEmpty()){
            throw new IllegalArgumentException("Customer surname is null or empty string");
        }
        if(customer.getIdentityCard()==null||customer.getIdentityCard().isEmpty()){
            throw new IllegalArgumentException("Customer Identity card is null or empty string");
        }
        if(customer.getPhoneNumber()==null||customer.getPhoneNumber().isEmpty()){
            if(customer.getAddress()==null||customer.getAddress().isEmpty()){
                throw new IllegalArgumentException("Both customer contact (phone and address) is null or empty string");
            }
        }
    }

    private Long getKey(ResultSet keyRS, Customer customer) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + customer
                    + " - no key found");
        }
    }
}

