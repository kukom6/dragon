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
        assertEquals(timeService.getCurrentDate(),getLease1.getStartDate());
        assertEquals(lease1,getLease1);
        assertDeepEquals(lease1,getLease1);
        assertNotSame(lease1,getLease1);

        assertNotEquals(customer2, getLease1);
        assertNotSame(customer2, getLease1);

        Customer customer3 = newCustomer("Ondrej","Zivic","Bytca 1020","SK561","+421 922 222 222");
        managerCustomer.createCustomer(customer3);

        Lease getLease2=managerLease.getLeaseByID(lease2.getId()); //back dragon2
        getLease2.setReturnDate(sdf.parse("16-09-2015 12:00:00"));
        managerLease.updateLease(getLease2);

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

        lease2 = newLease(customer2,dragon1,sdf.parse("17-05-1994 12:00:00"),new BigDecimal("50000.01"));
        try { // end day is before start date.
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

        Customer customer3 = newCustomer("Jan","Blazej","Piestany 1020","SK565","+421 922 222 222");
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

        Dragon dragon3 = newDragon("Nice dragon", sdf.parse("16-04-1996 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon3);

        Lease lease3 = newLease(customer1,dragon3,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease3);

        List<Lease> sample= new ArrayList<Lease>();
        List<Lease> actual= new ArrayList<Lease>();

        sample.add(lease1);
        sample.add(lease3);

        Collection<Lease> leasesForCustomer=managerLease.findLeasesForCustomer(customer1);
        actual.addAll(leasesForCustomer);

        Collections.sort(sample,idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

        // test with customer who doesn't have dragon
        Customer customer3 = newCustomer("Andrej","Pincik","Brezovec 10","SK695","+421 922 222 222");
        managerCustomer.createCustomer(customer3);
        leasesForCustomer=managerLease.findLeasesForCustomer(customer3);
        assertTrue(leasesForCustomer.isEmpty());

        //consistency
        Lease getLease = managerLease.getLeaseByID(lease2.getId());
        assertDeepEquals(lease2,getLease);

    }

    @Test
    public void testFindLeasesForCustomerWithWrongArgument() throws Exception {

        Collection<Lease> leasesForCustomer=new ArrayList<>();

        try{
            leasesForCustomer=managerLease.findLeasesForCustomer(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        Customer customer2 = newCustomer("Tomas","Mician","Cadca 123","SK685","+421 944 222 222");
        try{
            leasesForCustomer=managerLease.findLeasesForCustomer(customer2);
            fail(); //customer isn't in DB
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
     public void testFindLeasesForDragon() throws Exception {
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

        Customer customer3 = newCustomer("Mario", "Majernik", "Lucenec 183", "SK3666", "+421 944 222 222");
        managerCustomer.createCustomer(customer3);

        lease1=managerLease.getLeaseByID(lease1.getId());
        lease1.setReturnDate(sdf.parse("16-05-2015 12:00:00"));
        managerLease.updateLease(lease1);
        timeService.setCurrentDate(sdf.parse("16-05-2015 12:05:00")); //end lease1

        Lease lease3 = newLease(customer3,dragon1,sdf.parse("16-09-2018 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease3);

        List<Lease> sample= new ArrayList<Lease>();
        List<Lease> actual= new ArrayList<Lease>();

        sample.add(lease1);
        sample.add(lease3);

        Collection<Lease> leasesForDragon=managerLease.findLeasesForDragon(dragon1);
        actual.addAll(leasesForDragon);

        Collections.sort(sample,idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(sample,actual);
        assertDeepEquals(sample,actual);

        // dragon don't have borrowed
        Dragon dragon3 = newDragon("Lonely dragon", sdf.parse("16-04-1992 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon3);
        leasesForDragon=managerLease.findLeasesForDragon(dragon3);
        assertTrue(leasesForDragon.isEmpty());

        //consistency
        Lease getLease = managerLease.getLeaseByID(lease2.getId());
        assertDeepEquals(lease2,getLease);

    }

    @Test
    public void testFindLeasesForDragonWithWrongArgument() throws Exception {
        Collection<Lease> leasesForDragon=new ArrayList<>();

        try{
            leasesForDragon=managerLease.findLeasesForDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        Dragon dragon1 = newDragon("Nice dragon", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        try{
            leasesForDragon=managerLease.findLeasesForDragon(dragon1);
            fail(); //dragon isn't in DB
        } catch (IllegalArgumentException ex) {
            //true
        }
    }

    @Test
    public void testUpdateLease() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        Customer customer2 = newCustomer("Ondrej","Brezovec","Zilina 1020","SK5668","+421 922 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Nice dragon2", sdf.parse("16-04-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-09-2015 12:00:00"),new BigDecimal("30000.00"));
        managerLease.createLease(lease2);


        Long lease1ID=lease1.getId();

        //change customer
        Lease getLeaseBeforeUpdate=managerLease.getLeaseByID(lease1ID);
        Customer customer3 = newCustomer("Juraj","Bezuch","Brezno 03","SK3889","+421 944 222 222");
        managerCustomer.createCustomer(customer3);
        getLeaseBeforeUpdate.setCustomer(customer3);
        managerLease.updateLease(getLeaseBeforeUpdate);
        Lease getLeaseAfterUpdate=managerLease.getLeaseByID(lease1ID);

        assertEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertDeepEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertNotSame(getLeaseBeforeUpdate,getLeaseAfterUpdate);

        //change dragon
        getLeaseBeforeUpdate=managerLease.getLeaseByID(lease1ID);
        Dragon dragon3 = newDragon("Nice dragon", sdf.parse("16-03-1995 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon3);
        getLeaseBeforeUpdate.setDragon(dragon3);
        managerLease.updateLease(getLeaseBeforeUpdate);
        getLeaseAfterUpdate=managerLease.getLeaseByID(lease1ID);

        assertEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertDeepEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertNotSame(getLeaseBeforeUpdate,getLeaseAfterUpdate);

        //set end date
        getLeaseBeforeUpdate=managerLease.getLeaseByID(lease1ID);
        getLeaseBeforeUpdate.setEndDate(sdf.parse("16-08-2019 12:00:00"));
        managerLease.updateLease(getLeaseBeforeUpdate);
        getLeaseAfterUpdate=managerLease.getLeaseByID(lease1ID);

        assertEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertDeepEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertNotSame(getLeaseBeforeUpdate,getLeaseAfterUpdate);

        //change price
        getLeaseBeforeUpdate=managerLease.getLeaseByID(lease1ID);
        getLeaseBeforeUpdate.setPrice(new BigDecimal("30000.00"));
        managerLease.updateLease(getLeaseBeforeUpdate);
        getLeaseAfterUpdate=managerLease.getLeaseByID(lease1ID);

        assertEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertDeepEquals(getLeaseBeforeUpdate,getLeaseAfterUpdate);
        assertNotSame(getLeaseBeforeUpdate,getLeaseAfterUpdate);

        assertDeepEquals(lease2,managerLease.getLeaseByID(lease2.getId())); //consistency

    }

    @Test
    public void testUpdateLeaseWithWrongArgument() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        Lease modifyLease;

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setId(5l);
        try { //wrong ID, id isn't in DB
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setId(null);
        try { // wrong ID, id is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setCustomer(null);
        try { // wrong Customer; Customer is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        Customer customer = newCustomer("Tomas","Majernik","Zelezovce 125","SK345","+421 944 222 222");
        modifyLease.setCustomer(customer);
        try { // wrong Customer; Customer isn't in DB
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setDragon(null);
        try { // wrong dragon; dragon is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        Dragon dragon = newDragon("Ugly dragon2", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        modifyLease.setDragon(dragon);
        try { // wrong dragon; dragon isn't in DB
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        //dragon isn't free
        Customer customer2 = newCustomer("Juraj","Fratrik","Lomnica 123","SK551","+421 944 222 222");
        managerCustomer.createCustomer(customer2);
        Dragon dragon2 = newDragon("Borow Dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon2);

        Lease lease2 = newLease(customer2,dragon2,sdf.parse("16-05-2016 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease2);

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setDragon(dragon2);
        try { // dragon isn't free
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setStartDate(null);
        try { // startDate is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setEndDate(null);
        try { // EndDate is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setEndDate(sdf.parse("16-03-1994 12:00:00"));
        try { // EndDate is before start date
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setReturnDate(sdf.parse("16-03-1994 12:00:00"));
        try { // Return date is before start date
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setPrice(null);
        try { // price is null
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setPrice(new BigDecimal("50000.001"));
        try { // price isn't in correct form
            managerLease.updateLease(modifyLease);
            fail();
        } catch (ServiceFailureException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setPrice(new BigDecimal("-50000.00"));
        try { // price is negativ
            managerLease.updateLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //true
        }

        modifyLease=managerLease.getLeaseByID(lease1.getId());
        assertDeepEquals(lease1,modifyLease);
        assertNotSame(lease1,modifyLease);
    }

    @Test
    public void testDeleteLease() throws Exception {
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

        Lease getLease1=managerLease.getLeaseByID(lease1.getId());
        getLease1.setReturnDate(sdf.parse("16-05-2016 12:00:00"));
        managerLease.deleteLease(getLease1);

        assertNull(managerLease.getLeaseByID(lease1.getId()));
        assertNotNull(managerLease.getLeaseByID(lease2.getId()));
        assertDeepEquals(lease2, managerLease.getLeaseByID(lease2.getId())); //consistency

    }

    @Test
    public void testDeleteLeaseWithWrongArgument() throws Exception {
        Customer customer1 = newCustomer("Tomas","Oravec","Brezno 123","SK321","+421 944 222 222");
        managerCustomer.createCustomer(customer1);
        Dragon dragon1 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        managerDragon.createDragon(dragon1);

        Lease lease1 = newLease(customer1,dragon1,sdf.parse("16-05-2015 12:00:00"),new BigDecimal("50000.00"));
        managerLease.createLease(lease1);

        try {
            managerLease.deleteLease(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            managerLease.deleteLease(lease1);
            fail();  // don't delete lease where haven't return date
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Lease modifyLease=managerLease.getLeaseByID(lease1.getId());
        modifyLease.setId(null);
        try { //ID lease is null
            managerLease.deleteLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        modifyLease.setId(22l);
        try { //ID lease isn't correct
            managerLease.deleteLease(modifyLease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }


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
