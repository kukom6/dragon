import static org.junit.Assert.*;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;



public class CustomerManagerImplTest {


    private CustomerManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException{
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby://localhost:1527/dragonDB");
        this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE CUSTOMERS ("
                    + "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "NAME  VARCHAR(50),"
                    + "SURNAME  VARCHAR(50),"
                    + "ADDRESS  VARCHAR(100),"
                    + "IDENTITYCARD VARCHAR(20) UNIQUE,"
                    + "PHONENUMBER VARCHAR(20))").executeUpdate();

            manager = new CustomerManagerImpl(bds);
        }
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE CUSTOMERS").executeUpdate();
        }
    }

    @Test
    public void testCreateCustomer() throws Exception {

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);

        assertNotNull(manager.getCustomerByID(customer1.getId()));
        assertNotNull(manager.getCustomerByID(customer2.getId()));

        Long customer1Id = customer1.getId();
        assertNotNull(customer1Id);
        assertNotNull(customer1.getName());
        assertNotNull(customer1.getSurname());
        assertNotNull(customer1.getAddress());
        assertNotNull(customer1.getIdentityCard());
        assertNotNull(customer1.getPhoneNumber());

        Long customer2Id = customer2.getId();
        assertNotNull(customer2Id);
        assertNotNull(customer2.getName());
        assertNotNull(customer2.getSurname());
        assertNotNull(customer2.getAddress());
        assertNotNull(customer2.getIdentityCard());
        assertNotNull(customer2.getPhoneNumber());

        Customer getCustomer1 = manager.getCustomerByID(customer1Id);
        assertEquals(customer1, getCustomer1);
        assertDeepEquals(customer1,getCustomer1);
        assertNotSame(customer1, getCustomer1);

        assertNotEquals(customer2, getCustomer1);
        assertNotSame(customer2, getCustomer1);


    }

    @Test
    public void testCreateCustomerWithWrongArgument() throws Exception {
        try {
            manager.createCustomer(null); //null
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        Customer customer = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");  //wrong ID
        customer.setId(10l);
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer(null,"Oravec","Brezno 123","SK321","+421 944 222 222"); //name missing
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("","Oravec","Brezno 123","SK321","+421 944 222 222"); //name empty string
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas",null,"Brezno 123","SK321","+421 944 222 222"); //surname missing
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas","","Brezno 123","SK321","+421 944 222 222"); //surname empty string
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer = newCustomer("Tomas","Oravec","Brezno 123",null,"+421 944 222 222");  // ID card missing
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer = newCustomer("Tomas","Oravec","Brezno 123","","+421 944 222 222");  // ID empty string
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas","Oravec",null,"SK321",null); //wrong argument, must by one contact
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas","Oravec","","SK321",""); // both contact argument are empty string
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas","Oravec",null,"SK321",""); // both contact argument are empty string or null
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer=newCustomer("Tomas","Oravec","","SK321",null); // both contact argument are empty string or null
        try {
            manager.createCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        customer=newCustomer("Tomas","Oravec","Bratislava 8","SK321","+420586985");
        Customer customer2;
        customer2=newCustomer("Juraj","Blahovec","Brno 20","SK321","+421568955");
       try { //create customer with two same IDCard
            manager.createCustomer(customer);
            manager.createCustomer(customer2);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }
    }

    @Test
    public void testGetCustomerByID() throws Exception {

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);

        Customer getCustomer1 = manager.getCustomerByID(customer1.getId());
        assertEquals(customer1, getCustomer1);
        assertDeepEquals(customer1,getCustomer1);
        assertNotSame(customer1, getCustomer1);

        Customer getCustomer2 = manager.getCustomerByID(customer2.getId());
        assertEquals(customer2, getCustomer2);
        assertDeepEquals(customer2,getCustomer2);
        assertNotSame(customer2, getCustomer2);

        Customer getCustomer3=manager.getCustomerByID(11l);
        assertNull(getCustomer3);


    }

    @Test
    public void testGetCustomerByIDWithWrongArgument() throws Exception{
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        manager.createCustomer(customer1);

        try {
            manager.getCustomerByID(null); // wrong argument
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        try {
            manager.getCustomerByID(-1l);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }

    @Test
    public void testGetCustomerByIDCard() throws Exception {

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");

        manager.createCustomer(customer1);

        Customer customer=manager.getCustomerByIDCard("SK321");
        assertNotNull(customer);
        assertEquals(customer1,customer);
        assertDeepEquals(customer1,customer);

        customer=manager.getCustomerByIDCard("SK25");
        assertNull(customer);
    }

    @Test
    public void testGetCustomerByIDCardWithWrongArgument() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");

        manager.createCustomer(customer1);

        try {
            manager.getCustomerByIDCard("");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        try {
            manager.getCustomerByIDCard(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testGetAllCustomers() throws Exception {

        Collection<Customer> allCustomers=manager.getAllCustomers();

        assertTrue(allCustomers.isEmpty());

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);

        List<Customer> sample= new ArrayList<Customer>();
        List<Customer> actual= new ArrayList<Customer>();

        sample.add(customer1);
        sample.add(customer2);

        allCustomers=manager.getAllCustomers();
        actual.addAll(allCustomers);

        Collections.sort(sample,idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

    }

    @Test
    public void testGetAllCustomersByName() throws Exception{

        Collection<Customer> allCustomersByName=manager.getAllCustomers();

        assertTrue(allCustomersByName.isEmpty());

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        Customer customer3 = newCustomer("Tomas","Oravec","Bardejov 125","SK48","+421 933 222 222");
        Customer customer4 = newCustomer("Tomas","Zajac","Kosice 89","SK3556","+421 955 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);
        manager.createCustomer(customer3);
        manager.createCustomer(customer4);

        List<Customer> sample= new ArrayList<Customer>();
        List<Customer> actual= new ArrayList<Customer>();

        sample.add(customer1);
        sample.add(customer3);

        allCustomersByName=manager.getAllCustomersByName("Tomas","Oravec");
        actual.addAll(allCustomersByName);

        Collections.sort(sample,idComparator);
        Collections.sort(actual,idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

        allCustomersByName=manager.getAllCustomersByName("Tomas","Brezovec"); // empty list
        assertTrue(allCustomersByName.isEmpty());
    }

    @Test
    public void testGetAllCustomersByNameWithWrongArgument() throws Exception {
        Collection<Customer> customers =new ArrayList<>();
        try {
            customers=manager.getAllCustomersByName("","");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName(null,null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName(null,"");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName("",null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName("Tomas","");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName("","Mician");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName("Tomas",null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        try {
            customers=manager.getAllCustomersByName(null,"Mician");
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);

        Long customer1Id = customer1.getId();
        Long customer2Id = customer2.getId();

        Customer modifyCustomer;

        modifyCustomer=manager.getCustomerByID(customer1Id);
        modifyCustomer.setName("Juraj");                                    // change name
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());
        assertEquals("Juraj",modifyCustomer.getName());
        assertEquals("Oravec",modifyCustomer.getSurname());
        assertEquals("Brezno 123",modifyCustomer.getAddress());
        assertEquals("SK321",modifyCustomer.getIdentityCard());
        assertEquals("+421 944 222 222",modifyCustomer.getPhoneNumber());


        modifyCustomer=manager.getCustomerByID(customer1Id);  // change surname
        modifyCustomer.setSurname("Galko");
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());
        assertEquals("Juraj",modifyCustomer.getName());
        assertEquals("Galko",modifyCustomer.getSurname());
        assertEquals("Brezno 123",modifyCustomer.getAddress());
        assertEquals("SK321",modifyCustomer.getIdentityCard());
        assertEquals("+421 944 222 222", modifyCustomer.getPhoneNumber());

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change Address
        modifyCustomer.setAddress("Cadca 56");
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());
        assertEquals("Juraj",modifyCustomer.getName());
        assertEquals("Galko",modifyCustomer.getSurname());
        assertEquals("Cadca 56", modifyCustomer.getAddress());
        assertEquals("SK321", modifyCustomer.getIdentityCard());
        assertEquals("+421 944 222 222",modifyCustomer.getPhoneNumber());

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change id card
        modifyCustomer.setIdentityCard("SK66");
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id, modifyCustomer.getId());
        assertEquals("Juraj",modifyCustomer.getName());
        assertEquals("Galko",modifyCustomer.getSurname());
        assertEquals("Cadca 56", modifyCustomer.getAddress());
        assertEquals("SK66", modifyCustomer.getIdentityCard());
        assertEquals("+421 944 222 222",modifyCustomer.getPhoneNumber());

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change phone
        modifyCustomer.setPhoneNumber("+421 907 222 333");
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());
        assertEquals("Juraj", modifyCustomer.getName());
        assertEquals("Galko", modifyCustomer.getSurname());
        assertEquals("Cadca 56",modifyCustomer.getAddress());
        assertEquals("SK66", modifyCustomer.getIdentityCard());
        assertEquals("+421 907 222 333",modifyCustomer.getPhoneNumber());

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change phone to null
        modifyCustomer.setPhoneNumber(null);
        manager.updateCustomer(modifyCustomer);
        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());
        assertEquals("Juraj",modifyCustomer.getName());
        assertEquals("Galko",modifyCustomer.getSurname());
        assertEquals("Cadca 56", modifyCustomer.getAddress());
        assertEquals("SK66", modifyCustomer.getIdentityCard());
        assertNull(modifyCustomer.getPhoneNumber());


        assertDeepEquals(customer2,manager.getCustomerByID(customer2Id)); //consistency

    }

    @Test
    public void testUpdateCustomerWithWrongArgument() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321",null);
        manager.createCustomer(customer1);
        Long customer1Id = customer1.getId();

        Customer modifyCustomer;

        modifyCustomer=manager.getCustomerByID(customer1Id);
        modifyCustomer.setId(2l);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change address to null (phone was null)
        modifyCustomer.setAddress(null);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change address to empty string (phone was null)
        modifyCustomer.setAddress("");
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // set phone and delete address
        modifyCustomer.setPhoneNumber("+421 944 222 222");
        modifyCustomer.setAddress(null);
        manager.updateCustomer(modifyCustomer);

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change phone to null (address was null)
        modifyCustomer.setPhoneNumber(null);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change phone to empty string (address was null)
        modifyCustomer.setPhoneNumber("");
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change name to null
        modifyCustomer.setName(null);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change name to empty string
        modifyCustomer.setName("");
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change surname to null
        modifyCustomer.setSurname(null);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change surname to empty list
        modifyCustomer.setSurname("");
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change id card to null
        modifyCustomer.setIdentityCard(null);
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  // change id card to empty list
        modifyCustomer.setIdentityCard("");
        try {
            manager.updateCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);

        assertEquals(customer1Id,modifyCustomer.getId());  // check final object (consistency)
        assertEquals("Tomas",modifyCustomer.getName());
        assertEquals("Oravec",modifyCustomer.getSurname());
        assertNull(modifyCustomer.getAddress());
        assertEquals("SK321",modifyCustomer.getIdentityCard());
        assertEquals("+421 944 222 222",modifyCustomer.getPhoneNumber());


    }

    @Test
    public void testDeleteCustomer() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");

        manager.createCustomer(customer1);
        manager.createCustomer(customer2);
        manager.deleteCustomer(customer1);

        assertNull(manager.getCustomerByID(customer1.getId()));
        assertNotNull(manager.getCustomerByID(customer2.getId()));
        assertDeepEquals(customer2, manager.getCustomerByID(customer2.getId())); //consistency

    }

    @Test
    public void testDeleteCustomerWithWrongArgument() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");

        manager.createCustomer(customer1);
        Long customer1Id=customer1.getId();

        try {
            manager.deleteCustomer(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //manager.createCustomer(customer1);
        Customer modifyCustomer;
        modifyCustomer=manager.getCustomerByID(customer1Id);  //delete where ID is null
        modifyCustomer.setId(null);
        try {
            manager.deleteCustomer(modifyCustomer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        modifyCustomer=manager.getCustomerByID(customer1Id);  //delete with wrong ID
        modifyCustomer.setId(11l);
        try {
            manager.deleteCustomer(modifyCustomer);
            fail();
        } catch (ServiceFailureException ex) {
            //OK
        }

        assertDeepEquals(customer1,manager.getCustomerByID(customer1Id)); //consistency

    }

    private static Customer newCustomer(String name,String surname,String address,String identityCard,String numberPhone){
        Customer customer=new Customer();
        customer.setName(name);
        customer.setSurname(surname);
        customer.setAddress(address);
        customer.setIdentityCard(identityCard);
        customer.setPhoneNumber(numberPhone);
        return customer;
    }

    private static Comparator<Customer> idComparator = new Comparator<Customer>() {

        @Override
        public int compare(Customer o1, Customer o2) {
            return o1.getId().compareTo(o2.getId());

        }
    };

    private void assertDeepEquals(Customer first,Customer second){
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getName(), second.getName());
        assertEquals(first.getSurname(), second.getSurname());
        assertEquals(first.getIdentityCard(), second.getIdentityCard());
        assertEquals(first.getAddress(), second.getAddress());
        assertEquals(first.getPhoneNumber(), second.getPhoneNumber());
    }

    private void assertDeepEquals(List<Customer> first, List<Customer> second){
        for (int i = 0; i < first.size(); i++) {
            Customer firstSample=first.get(i);
            Customer secondSample=second.get(i);
            assertDeepEquals(firstSample,secondSample);
        }
    }
}