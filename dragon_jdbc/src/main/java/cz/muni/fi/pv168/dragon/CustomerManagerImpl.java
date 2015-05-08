package cz.muni.fi.pv168.dragon;
import java.io.IOException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;


public class CustomerManagerImpl implements CustomerManager {
    private final DataSource source;
    private static final Logger log = Logger.getLogger(CustomerManagerImpl.class.getCanonicalName());

    public CustomerManagerImpl(DataSource dataSource) {
        this.source = dataSource;
    }

    private void checkDataSource() {
        if (source == null) {
            log.log(Level.SEVERE, "DataSource is null.");
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createCustomer(Customer customer) {
        checkDataSource();
        log.log(Level.INFO, "Create customer: "+customer);
        if(customer==null){
            log.log(Level.SEVERE, "Create customer: Illegal argument exception: customer is null.");
            throw new IllegalArgumentException("Customer is null");
        }
        if(customer.getId()!=null) {
            log.log(Level.SEVERE, "Create customer: Illegal argument exception: customer is in DB.");
            throw new IllegalArgumentException("Customer is in DB");
        }

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
                    log.log(Level.SEVERE, "Create customer: Service failure exception: Create more update also one.");
                    throw new ServiceFailureException("Create more update also one");
                }
                ResultSet keyRS = st.getGeneratedKeys();
                customer.setId(getKey(keyRS, customer));
                log.log(Level.INFO, "Create customer "+customer+ " is OK.");
            }
        }catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem or two customer have same IDCard", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Customer getCustomerByID(Long id) {
        log.log(Level.INFO,"Get customer by ID: "+id+" from DB");
        checkDataSource();
        if(id==null){
            log.log(Level.SEVERE, "Get customer: Illegal argument exception: argumentis null.");
            throw new IllegalArgumentException("argumentis null");
        }
        if(id < 0){
            log.log(Level.SEVERE, "Get customer: Illegal argument exception: id is negative or zero.");
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
                        log.log(Level.SEVERE, "Get customer: Service failure exception: " +
                                "More customers have same ID, ID: "+ id + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                        throw new ServiceFailureException("More customers have same ID, ID: "+ id + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                    }
                    log.log(Level.INFO,"Get customer :"+customer+" is OK.");
                    return customer;
                } else {
                    return null;
                }
            }
        }catch(SQLException ex){
            log.log(Level.SEVERE,"db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Customer getCustomerByIDCard(String idCard){
        log.log(Level.INFO,"Get customer by number idcard: "+idCard+" from DB.");
        checkDataSource();
        if(idCard==null||idCard.isEmpty()){
            log.log(Level.SEVERE, "Get customer by IDCard: Illegal argument exception: idCard is null or empty string.");
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
                        log.log(Level.SEVERE, "Get customer by IDCard: Service failure exception: " +
                                "More customers have same number ID card, ID card" +
                                "number is : "+ idCard + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                        throw new ServiceFailureException("More customers have same number ID card, ID card" +
                                "number is : "+ idCard + " have "
                                + customer + " and " + resultToCustomer(rs) + " too.");
                    }
                    log.log(Level.INFO,"Get customer :"+customer+" is OK.");
                    return customer;
                } else {
                    return null;
                }
            }
        }catch(SQLException ex){
            log.log(Level.SEVERE,"db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Collection<Customer> getAllCustomers() {
        log.log(Level.INFO, "Get all customers from DB.");
        checkDataSource();

        List<Customer> customers = new ArrayList<>();
        try(Connection conn = source.getConnection()){
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM CUSTOMERS")) {
                ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    customers.add(resultToCustomer(rs));
                }
                log.log(Level.INFO, "Get all customers from DB is OK.");
                return Collections.unmodifiableCollection(customers);
            }
        }catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public Collection<Customer> getAllCustomersByName(String name,String surname){
        log.log(Level.INFO, "Get all customers from DB by name:"+name);
        checkDataSource();
        if(name==null||surname==null||name.isEmpty()||surname.isEmpty()){
            log.log(Level.SEVERE, "Get all customers from DB by name: Illegal argument exception: Name or surname or both are valid.");
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
                log.log(Level.INFO, "Get all customers by name: "+name+" is ok");
                return Collections.unmodifiableCollection(customers);
            }
        }catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public void updateCustomer(Customer customer) {
        checkDataSource();
        log.log(Level.INFO, "Update Customer: "+customer);
        if (customer == null) {
            log.log(Level.SEVERE, "Update customer: Illegal argument exception: Customer is null.");
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            log.log(Level.SEVERE, "Update customer: Illegal argument exception: Customer isn't in DB.");
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
                log.log(Level.INFO, "Update customer "+customer+" is OK.");
                if (numbUpdate != 1) {
                    log.log(Level.SEVERE, "Update customer: Illegal argument exception: Customer with id" + customer.getId() +" do not exist.");
                    throw new IllegalArgumentException("Customer with id=" + customer.getId() + " do not exist");
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem or two customer have same IDCard or invalid ID", ex);
            throw new ServiceFailureException("Error with DB", ex);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) {
        log.log(Level.INFO, "Delete customer: "+customer);
        checkDataSource();
        if (customer == null) {
            log.log(Level.SEVERE, "Delete customer: Illegal argument exception: Customer is null.");
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            log.log(Level.SEVERE, "Delete customer: Illegal argument exception: Customer isn't in DB.");
            throw new IllegalArgumentException("Customer isn't in DB");
        }


        try (Connection conn = source.getConnection()) {
            try(PreparedStatement st = conn.prepareStatement("DELETE FROM CUSTOMERS WHERE ID=?")) {
                st.setLong(1,customer.getId());
                log.log(Level.INFO, "Delete Customer "+customer+" is OK.");
                if(st.executeUpdate()!=1) {
                    log.log(Level.SEVERE, "Delete customer: Illegal argument exception: Customer with id "+customer.getId()+" doesn't deleted.");
                    throw new IllegalArgumentException("Customer with id "+customer.getId()+" doesn't deleted");
                }
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem or invalid ID", ex);
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
            log.log(Level.SEVERE, "Check customer: Illegal argument exception: Customer name is null or empty string.");
            throw new IllegalArgumentException("Customer name is null or empty string");
        }
        if(customer.getSurname()==null||customer.getSurname().isEmpty()){
            log.log(Level.SEVERE, "Check customer: Illegal argument exception: Customer surname is null or empty string.");
            throw new IllegalArgumentException("Customer surname is null or empty string");
        }
        if(customer.getIdentityCard()==null||customer.getIdentityCard().isEmpty()){
            log.log(Level.SEVERE, "Check customer: Illegal argument exception: Customer Identity card is null or empty string.");
            throw new IllegalArgumentException("Customer Identity card is null or empty string");
        }
        if(customer.getPhoneNumber()==null||customer.getPhoneNumber().isEmpty()){
            if(customer.getAddress()==null||customer.getAddress().isEmpty()){
                log.log(Level.SEVERE, "Check customer: Illegal argument exception: Both customer contact (phone and address) is null or empty string.");
                throw new IllegalArgumentException("Both customer contact (phone and address) is null or empty string");
            }
        }
    }

    private Long getKey(ResultSet keyRS, Customer customer) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());

                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - more keys found");

                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                    "Internal Error: Generated key"
                    + "retriving failed when trying to insert customer " + customer
                    + " - no key found");
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert customer " + customer
                    + " - no key found");
        }
    }
}

