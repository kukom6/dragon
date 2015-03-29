import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class LeaseManagerImplTest {

    private LeaseManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
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
            manager = new LeaseManagerImpl(bds);
        }
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

    }

    @Test
    public void testGetLeaseByID() throws Exception {

    }

    @Test
    public void testGetAllLeases() throws Exception {

    }

    @Test
    public void testGetAllLeasesByEndDate() throws Exception {

    }

    @Test
    public void testGetAllLeasesByStartDate() throws Exception {

    }

    @Test
    public void testFindLeasesForCustomer() throws Exception {

    }

    @Test
    public void testFindLeasesForDragon() throws Exception {

    }

    @Test
    public void testUpdateLease() throws Exception {

    }

    @Test
    public void testDeleteLease() throws Exception {

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
