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
import java.util.*;

import static org.junit.Assert.*;

public class LeaseManagerImplTest {

    private LeaseManagerImpl managerLease;
    private DragonManagerImpl managerDragon;
    private CustomerManagerImpl managerCustomer;

    private DataSource dataSource;
    private TimeServiceImplTest timeService;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
    @Before
    public void setUp() throws Exception {
        timeService= new TimeServiceImplTest(); //"15-03-2015 12:00:00"
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
                    + "IDCUSTOMER BIGINT,"
                    + "IDDRAGON BIGINT,"
                    + "STARTDATE TIMESTAMP,"
                    + "ENDDATE TIMESTAMP,"
                    + "RETURNDATE TIMESTAMP,"
                    + "PRICE DECIMAL(20,2),"
                    + "CONSTRAINT customer_fk FOREIGN KEY (IDCUSTOMER) "
                    + "REFERENCES CUSTOMERS(ID), "
                    + "CONSTRAINT dragon_fk FOREIGN KEY (IDDRAGON) "
                    + "REFERENCES DRAGONS(ID))").executeUpdate();
        }
        managerLease = new LeaseManagerImpl(bds,timeService);
        managerDragon = new DragonManagerImpl(bds,timeService);
        managerCustomer = new CustomerManagerImpl(bds);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE LEASES").executeUpdate();
            con.prepareStatement("DROP TABLE CUSTOMERS").executeUpdate();
            con.prepareStatement("DROP TABLE DRAGONS").executeUpdate();
        }
    }

    @Test
    public void testCreateLease() throws Exception {

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);
        assertNotNull(lease1.getId());

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease2);
        assertNotNull(lease2.getId());

        assertNotNull(managerLease.getLeaseByID(lease1.getId()));
        assertNotNull(managerLease.getLeaseByID(lease2.getId()));

        Lease getLease1 = managerLease.getLeaseByID(lease1.getId());
        assertEquals(getLease1.getStartDate(),timeService.getCurrentDate());
        assertEquals(lease1,getLease1);
        assertDeepEquals(lease1,getLease1);
        assertNotSame(lease1,getLease1);

        assertNotEquals(customer2, getLease1);
        assertNotSame(customer2, getLease1);

        Customer customer3 = newCustomer("Ondrej","Zivic","Bytca 1020","SK561","+421 922 222 222");
        managerCustomer.createCustomer(customer3);
        timeService.setCurrentDate(sdf.parse("16-9-2015 12:00:01")); // dragon 2 is free this time
        Lease lease3 = newLease(customer3,dragon2,sdf.parse("16-11-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease3);

    }

    @Test
    public void testCreateLeaseWithWrongArguments() throws Exception{

        Lease lease1 = newLease(null,null,null,null);
        try { //all is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);

        lease1 = newLease(customer1,null,null,null);
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

        lease1 = newLease(null,dragon1,null,null);
        try { //all (except dragon)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
        managerDragon.deleteDragon(dragon1);
        dragon1.setId(null);


        lease1 = newLease(null,null,sdf.parse("16-03-2015 12:00:00"),null);
        try { //all (except end day)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(null,null,null,new BigDecimal("50000.00"));
        try { //all (except price)is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        managerCustomer.createCustomer(customer1);
        managerDragon.createDragon(dragon1);

        lease1 = newLease(null,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        try { // customer is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,null,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        try { // dragon is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,null,new BigDecimal("50000.00"));
        try { // end day is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),null);
        try { // price is null
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));

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

        try { //create lease when dragon isn't in DB
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
        customer1.setId(null);
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
        customer1.setId(null);
        managerCustomer.createCustomer(customer1);

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("-50000.00"));
        try { //price is negativ
            managerLease.createLease(lease1);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.001"));
        try { //price is three numbers after dot
            managerLease.createLease(lease1);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }

        lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.01"));
        managerLease.createLease(lease1);
        Customer customer2 = newCustomer("Lukas","Lipa","Kosice 123","SK35521","+421 944 222 222");
        managerCustomer.createCustomer(customer2);
        Lease lease2 = newLease(customer2,dragon1,sdf.parse("17-05-2015 12:00:00"),new BigDecimal("50000.01"));
        try { // lease 2 have dragon where is borowed in this time
            managerLease.createLease(lease2);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease2 = newLease(customer2,dragon1,sdf.parse("17-05-2015 12:00:00"),new BigDecimal("50000.01"));
        lease2.setReturnDate(sdf.parse("16-05-2015 12:00:00"));
        try { // lease 2 have return date.
            managerLease.createLease(lease2);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        lease2 = newLease(customer2,dragon1,sdf.parse("17-05-2015 12:00:00"),new BigDecimal("50000.01"));
        lease2.setStartDate(sdf.parse("12-05-2015 12:00:00"));
        try { // lease 2 have start date.
            managerLease.createLease(lease2);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testGetLeaseByID() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease2);

        Lease getLease1 = managerLease.getLeaseByID(lease1.getId());
        assertEquals(lease1, getLease1);
        assertDeepEquals(lease1, getLease1);
        assertNotSame(lease1, getLease1);

        Lease getLease2 = managerLease.getLeaseByID(lease2.getId());
        assertEquals(lease2, getLease2);
        assertDeepEquals(lease2, getLease2);
        assertNotSame(lease2, getLease2);

        Lease getLease3 = managerLease.getLeaseByID(11l);
        assertNull(getLease3);


        Dragon dragon3 = newDragon("Nice dragon2", sdf.parse("16-04-1996 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon3);

        //big decimal test
        Lease lease3 = newLease(customer2,dragon3,sdf.parse("16-09-2016 12:00:00"),new BigDecimal("30000.1"));
        managerLease.createLease(lease3);
        getLease3 = managerLease.getLeaseByID(lease3.getId());
        assertEquals(getLease3.getPrice(),new BigDecimal("30000.10"));
    }

    @Test
    public void testGetLeaseByIDWithWrongArgument() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        try {
            managerLease.getLeaseByID(null); // wrong argument
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        try {
            managerLease.getLeaseByID(-41l);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testGetAllLeases() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Collection<Lease> allCustomers=managerLease.getAllLeases();

        assertTrue(allCustomers.isEmpty());

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease2);

        List<Lease> sample= new ArrayList<Lease>();
        List<Lease> actual= new ArrayList<Lease>();

        sample.add(lease1);
        sample.add(lease2);

        allCustomers=managerLease.getAllLeases();
        actual.addAll(allCustomers);

        Collections.sort(sample,idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

    }

    @Test
    public void testGetAllLeasesByEndDate() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease2);

        Customer customer3 = newCustomer("Jan","Blazej","Piestany 1020","SK56","+421 922 222 222");
        managerCustomer.createCustomer(customer3);
        Dragon dragon3 = newDragon("Fat dragon", sdf.parse("16-05-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon3);

        Lease lease3 = newLease(customer3,dragon3,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("40000.00"));
        managerLease.createLease(lease3);

        List<Lease> sample= new ArrayList<Lease>();
        List<Lease> actual= new ArrayList<Lease>();

        sample.add(lease1);
        sample.add(lease2);

        Collection<Lease> allLeasesByEndDate=managerLease.getAllLeasesByEndDate(sdf.parse("16-05-2015 12:00:00"));
        actual.addAll(allLeasesByEndDate);

        Collections.sort(sample,idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

        allLeasesByEndDate=managerLease.getAllLeasesByEndDate(sdf.parse("16-05-2016 12:00:00"));
        assertTrue(allLeasesByEndDate.isEmpty());
    }

    @Test
    public void testGetAllLeasesByEndDateWithWrongArgument() throws Exception {
        Collection<Lease> allLeasesByEndDate =new ArrayList<>();

        try{
            allLeasesByEndDate=managerLease.getAllLeasesByEndDate(null);
        }catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testFindLeasesForCustomer() throws Exception {
        throw new UnsupportedOperationException("not implemented");
    }

    @Test
    public void testFindLeasesForDragon() throws Exception {
        throw new UnsupportedOperationException("not implemented");
    }

    @Test
    public void testUpdateLease() throws Exception {
        throw new UnsupportedOperationException("not implemented");
    }

    @Test
    public void testDeleteLease() throws Exception {
        throw new UnsupportedOperationException("not implemented");
    }

    private static Lease newLease(Customer customer,Dragon dragon,Date endDate,BigDecimal price){
        Lease lease=new Lease();
        lease.setCustomer(customer);
        lease.setDragon(dragon);
        lease.setStartDate(null);
        lease.setEndDate(endDate);
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
        assertEquals(first.getStartDate(),second.getStartDate());
        assertEquals(first.getEndDate(),second.getEndDate());
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
