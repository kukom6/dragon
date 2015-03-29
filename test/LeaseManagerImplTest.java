import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class LeaseManagerImplTest {

    private LeaseManagerImpl managerLease;
    private DragonManagerImpl managerDragon;
    private CustomerManagerImpl managerCustomer;

    private DataSource dataSource;

    private TimeServiceImpl timeService = new TimeServiceImpl(){
        @Override
        public Date getCurrentDate(){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            try {
                return sdf.parse("15-03-2014 12:00:00");
            }catch(ParseException ex){
                throw new NullPointerException("Can't parse date.");
            }
        }
    };

    @Before
    public void setUp() throws Exception {
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

            conn.prepareStatement("CREATE TABLE DRAGONS ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "\"NAME\" VARCHAR(50),"
                    + "BORN TIMESTAMP,"
                    + "RACE VARCHAR(50),"
                    + "HEADS INTEGER,"
                    + "WEIGHT INTEGER)").executeUpdate();

            conn.prepareStatement("CREATE TABLE LEASES ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "CUSTOMER BIGINT,"
                    + "DRAGON BIGINT,"
                    + "STARTDATE TIMESTAMP,"
                    + "ENDDATE TIMESTAMP,"
                    + "RETURNDATE TIMESTAMP,"
                    + "PRICE INTEGER)").executeUpdate(); //TODO big decimal
        }
        managerLease = new LeaseManagerImpl(bds,timeService);
        managerDragon = new DragonManagerImpl(bds,timeService);
        managerCustomer = new CustomerManagerImpl(bds);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE CUSTOMERS").executeUpdate();
            con.prepareStatement("DROP TABLE DRAGONS").executeUpdate();
            con.prepareStatement("DROP TABLE LEASES").executeUpdate();
        }
    }

    @Test
    public void testCreateLease() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1995 12:00:00"),new BigDecimal(50000.00));
        managerLease.createLease(lease1);
        assertNotNull(lease1.getId());

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-09-1995 12:00:00"),new BigDecimal(30000.00));
        managerLease.createLease(lease2);
        assertNotNull(lease2.getId());

        assertNotNull(managerLease.getLeaseByID(lease1.getId()));
        assertNotNull(managerLease.getLeaseByID(lease2.getId()));

        Lease getLease1 = managerLease.getLeaseByID(lease1.getId());
        assertEquals(lease1,getLease1);
        assertDeepEquals(lease1,getLease1);
        assertNotSame(lease1,getLease1);

        assertNotEquals(customer2, getLease1);
        assertNotSame(customer2, getLease1);
    }

    @Test
    public void testCreateLeaseWithWrongArguments() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Lease lease1 = newLease(null,null,null,null,null);
        try { //all is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);

        lease1 = newLease(customer1,null,null,null,null);
        try { //all (except customer)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        managerCustomer.deleteCustomer(customer1);
        customer1.setId(null);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        lease1 = newLease(null,dragon1,null,null,null);
        try { //all (except dragon)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        managerDragon.deleteDragon(dragon1);
        dragon1.setId(null);

        lease1 = newLease(null,null,sdf.parse("16-03-1994 12:00:00"),null,null);
        try { //all (except start day)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(null,null,null,sdf.parse("16-03-1994 12:00:00"),null);
        try { //all (except end day)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(null,null,null,null,new BigDecimal(50000.00));
        try { //all (except price)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        managerCustomer.createCustomer(customer1);
        managerDragon.createDragon(dragon1);

        lease1 = newLease(null,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1995 12:00:00"),new BigDecimal(50000.00));
        try { // customer is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,null,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1995 12:00:00"),new BigDecimal(50000.00));
        try { // dragon is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,null,sdf.parse("16-05-1995 12:00:00"),new BigDecimal(50000.00));
        try { // start day is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-1995 12:00:00"),null,new BigDecimal(50000.00));
        try { // end day is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1995 12:00:00"),null);
        try { // price is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        managerCustomer.deleteCustomer(customer1);
        customer1.setId(null);

        managerDragon.deleteDragon(dragon1);
        dragon1.setId(null);

        try { //create lease when dragon and customer aren't in DB
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        //TODO priamy pristup do lease
        customer1.setId(12l);
        dragon1.setId(13l);

        try { //create lease when dragon and customer have wrong ID
            managerLease.createLease(lease1);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }

        customer1.setId(null);
        dragon1.setId(null);

        managerCustomer.createCustomer(customer1); //customer is correct

        try { //create lease when dragon aren't in DB
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        dragon1.setId(13l);
        try { //create lease when only dragon have wrong ID
            managerLease.createLease(lease1);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }

        dragon1.setId(null);
        managerCustomer.deleteCustomer(customer1);
        managerDragon.createDragon(dragon1);

        try { //create lease when customer aren't in DB
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        customer1.setId(13l);
        try { //create lease when only customer have wrong ID
            managerLease.createLease(lease1);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }
        dragon1.setId(null);

        lease1 = newLease(customer1,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1992 12:00:00"),new BigDecimal(50000.00));
        try { //end day is before start day
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }


        lease1 = newLease(customer1,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1996 12:00:00"),new BigDecimal(-50000.00));
        try { //price is negativ
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-03-1995 12:00:00"),sdf.parse("16-05-1996 12:00:00"),new BigDecimal(50000.001));
        try { //price is three numbers after dot
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

    }

    @Test
    public void testGetLeaseByID() throws Exception {
        fail();
    }

    @Test
    public void testGetAllLeases() throws Exception {
        fail();
    }

    @Test
    public void testGetAllLeasesByEndDate() throws Exception {
        fail();
    }

    @Test
    public void testGetAllLeasesByStartDate() throws Exception {
        fail();
    }

    @Test
    public void testFindLeasesForCustomer() throws Exception {
        fail();
    }

    @Test
    public void testFindLeasesForDragon() throws Exception {
        fail();
    }

    @Test
    public void testUpdateLease() throws Exception {
        fail();
    }

    @Test
    public void testDeleteLease() throws Exception {
        fail();
    }

    private static Lease newLease(Customer customer,Dragon dragon,Date startLease,Date endLease,BigDecimal price){
        Lease lease=new Lease();
        lease.setCustomer(customer);
        lease.setDragon(dragon);
        lease.setStartLease(startLease);
        lease.setEndLease(endLease);
        lease.setPrice(price);
        lease.setReturnDate(null);
        return lease;
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

    private static Dragon newDragon(String name, Date bornDate, String race, int numOfHeads, int weight){
        Dragon dragon = new Dragon();
        dragon.setName(name);
        dragon.setBorn(bornDate);
        dragon.setRace(race);
        dragon.setNumberOfHeads(numOfHeads);
        dragon.setWeight(weight);
        return dragon;
    }

    private static Comparator<Lease> idComparator = new Comparator<Lease>() {

        @Override
        public int compare(Lease o1, Lease o2) {
            return o1.getId().compareTo(o2.getId());

        }
    };

    private void assertDeepEquals(Lease first,Lease second){
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getCustomer(),second.getCustomer());
        assertDeepEquals(first.getCustomer(),second.getCustomer());
        assertEquals(first.getDragon(),second.getDragon());
        assertDeepEquals(first.getDragon(), second.getDragon());
        assertEquals(first.getStartLease(),second.getStartLease());
        assertEquals(first.getEndLease(),second.getEndLease());
        assertEquals(first.getPrice(),second.getPrice());
        assertEquals(first.getReturnDate(),second.getReturnDate());

    }

    private void assertDeepEquals(List<Lease> first, List<Lease> second){
        for (int i = 0; i < first.size(); i++) {
            Lease firstSample=first.get(i);
            Lease secondSample=second.get(i);
            assertDeepEquals(firstSample,secondSample);
        }
    }

    private void assertDeepEquals(Customer first,Customer second){
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getName(), second.getName());
        assertEquals(first.getSurname(), second.getSurname());
        assertEquals(first.getIdentityCard(), second.getIdentityCard());
        assertEquals(first.getAddress(), second.getAddress());
        assertEquals(first.getPhoneNumber(), second.getPhoneNumber());
    }

    private void assertDeepEquals(Dragon expected, Dragon actual){
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBorn(), actual.getBorn());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getNumberOfHeads(), actual.getNumberOfHeads());
        assertEquals(expected.getRace(), actual.getRace());
        assertEquals(expected.getWeight(), actual.getWeight());
    }

}
