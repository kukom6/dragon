import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.Assert.*;

public class DragonManagerImplTest {

    private DragonManagerImpl manager;

    @Resource(name="jdbc/my")
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception{
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl("jdbc:derby:memory:GraveManagerTest;create=true");
        this.dataSource = bds;
        //create new empty table before every test
        try (Connection conn = bds.getConnection()) {
            conn.prepareStatement("CREATE TABLE DRAGONS ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "\"NAME\" VARCHAR(50),"
                    + "BORN TIMESTAMP,"
                    + "RACE VARCHAR(50),"
                    + "HEADS INTEGER,"
                    + "WEIGHT INTEGER)").executeUpdate();
        }
        manager = new DragonManagerImpl(bds);
    }

    @After
    public void tearDown() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            con.prepareStatement("DROP TABLE DRAGONS").executeUpdate();
        }
    }

    @Test
    public void testCreateDragon() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "trhac", 5, 150);

        manager.createDragon(dragon);

        Long dragonId = dragon.getId();
        assertNotNull(dragonId);

        Dragon result = manager.getDragonById(dragonId);
        assertEquals(dragon, result);
        assertNotSame(dragon, result);
        assertDeepEquals(dragon, result);


        Dragon anotherDragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);

        manager.createDragon(anotherDragon);

        Long anotherDragonId = anotherDragon.getId();
        assertNotNull(anotherDragonId);

        Dragon anotherResult = manager.getDragonById(anotherDragonId);
        assertNotEquals(result, anotherResult);
        assertDeepNotEquals(result, anotherResult);
    }

    @Test
    public void testCreateDragonWithWrongArguments() throws Exception {
        try {
            manager.createDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);

        dragon.setId(1l);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon(null, sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "", 1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), null, 1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 0, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", -1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, -1);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "lung", 1, 0);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);
        Date plusHour = cal.getTime();

        dragon = newDragon("Ugly dragon", plusHour, "lung", 1, 100);
        try {
            manager.createDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void testGetDragonByID() throws Exception {
        assertNull(manager.getDragonById(1l));

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "trhac", 5, 150);

        manager.createDragon(dragon);
        Long dragonId = dragon.getId();

        Dragon result = manager.getDragonById(dragonId);
        assertEquals(dragon, result);
        assertDeepEquals(dragon, result);
    }

    @Test
    public void testGetAllDragons() throws Exception {
        assertTrue(manager.getAllDragons().isEmpty());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon1 = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scrathcer", 1, 100);

        manager.createDragon(dragon1);
        manager.createDragon(dragon2);

        List<Dragon> expected = Arrays.asList(dragon1, dragon2);
        List<Dragon> actual = new ArrayList<>(manager.getAllDragons());

        Collections.sort(expected, idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testGetDragonsByName() throws Exception {
        assertTrue(manager.getDragonsByName("Matej").isEmpty());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon1 = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scrathcer", 2, 100);
        Dragon dragon3 = newDragon("Ugly dragon", sdf.parse("17-03-1994 12:00:00"), "burster", 5, 120);

        manager.createDragon(dragon1);
        manager.createDragon(dragon2);
        manager.createDragon(dragon3);

        List<Dragon> actual = new ArrayList<>(manager.getDragonsByName("Nice dragon"));
        assertEquals(1, actual.size());
        assertEquals(dragon1, actual.get(0));
        assertDeepEquals(dragon1, actual.get(0));

        List<Dragon> expected = Arrays.asList(dragon2, dragon3);
        actual = new ArrayList<>(manager.getDragonsByName("Ugly dragon"));
        assertEquals(2, actual.size());

        assertNotEquals(actual.get(0), actual.get(1));

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testGetDragonsByRace() throws Exception {
        assertTrue(manager.getDragonsByRace("Matej").isEmpty());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon1 = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scratcher", 2, 100);
        Dragon dragon3 = newDragon("Ugly dragon", sdf.parse("17-03-1994 12:00:00"), "burster", 5, 120);

        manager.createDragon(dragon1);
        manager.createDragon(dragon2);
        manager.createDragon(dragon3);

        List<Dragon> actual = new ArrayList<>(manager.getDragonsByRace("scratcher"));
        assertEquals(1, actual.size());
        assertEquals(dragon2, actual.get(0));
        assertDeepEquals(dragon2, actual.get(0));

        List<Dragon> expected = Arrays.asList(dragon1, dragon3);
        actual = new ArrayList<>(manager.getDragonsByRace("burster"));
        assertEquals(2, actual.size());

        assertNotEquals(actual.get(0), actual.get(1));


        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testGetDragonsByNumberOfHeads() throws Exception {
        assertTrue(manager.getDragonsByNumberOfHeads(2).isEmpty());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon1 = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scrathcer", 2, 100);
        Dragon dragon3 = newDragon("Ugly dragon", sdf.parse("17-03-1994 12:00:00"), "burster", 5, 120);

        manager.createDragon(dragon1);
        manager.createDragon(dragon2);
        manager.createDragon(dragon3);

        List<Dragon> actual = new ArrayList<>(manager.getDragonsByNumberOfHeads(2));
        List<Dragon> expectedList = Arrays.asList(dragon2);
        assertEquals(1, actual.size());
        assertEquals(expectedList, actual);
        assertDeepEquals(expectedList, actual);

        expectedList = Arrays.asList(dragon1, dragon3);
        actual = new ArrayList<>(manager.getDragonsByNumberOfHeads(5));
        assertEquals(2, actual.size());

        assertNotEquals(actual.get(0), actual.get(1));

        Collections.sort(expectedList, idComparator);
        Collections.sort(actual, idComparator);

        assertEquals(expectedList, actual);
        assertDeepEquals(expectedList, actual);
    }

    @Test
    public void testUpdateDragon() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scrathcer", 2, 100);

        manager.createDragon(dragon);
        manager.createDragon(dragon2);
        Long dragonId = dragon.getId();

        dragon = manager.getDragonById(dragonId);
        dragon.setName("Bad dragon");
        manager.updateDragon(dragon);
        dragon = manager.getDragonById(dragonId);
        assertEquals("Bad dragon", dragon.getName());
        assertEquals(sdf.parse("15-03-1994 12:00:00"), dragon.getBorn());
        assertEquals("burster", dragon.getRace());
        assertEquals(5, dragon.getNumberOfHeads());
        assertEquals(150, dragon.getWeight());

        dragon.setBorn(sdf.parse("15-03-1994 12:30:00"));
        manager.updateDragon(dragon);
        dragon = manager.getDragonById(dragonId);
        assertEquals("Bad dragon", dragon.getName());
        assertEquals(sdf.parse("15-03-1994 12:30:00"), dragon.getBorn());
        assertEquals("burster", dragon.getRace());
        assertEquals(5, dragon.getNumberOfHeads());
        assertEquals(150, dragon.getWeight());

        dragon.setRace("damager");
        manager.updateDragon(dragon);
        dragon = manager.getDragonById(dragonId);
        assertEquals("Bad dragon", dragon.getName());
        assertEquals(sdf.parse("15-03-1994 12:30:00"), dragon.getBorn());
        assertEquals("damager", dragon.getRace());
        assertEquals(5, dragon.getNumberOfHeads());
        assertEquals(150, dragon.getWeight());

        dragon.setNumberOfHeads(4);
        manager.updateDragon(dragon);
        dragon = manager.getDragonById(dragonId);
        assertEquals("Bad dragon", dragon.getName());
        assertEquals(sdf.parse("15-03-1994 12:30:00"), dragon.getBorn());
        assertEquals("damager", dragon.getRace());
        assertEquals(4, dragon.getNumberOfHeads());
        assertEquals(150, dragon.getWeight());

        dragon.setWeight(120);
        manager.updateDragon(dragon);
        dragon = manager.getDragonById(dragonId);
        assertEquals("Bad dragon", dragon.getName());
        assertEquals(sdf.parse("15-03-1994 12:30:00"), dragon.getBorn());
        assertEquals("damager", dragon.getRace());
        assertEquals(4, dragon.getNumberOfHeads());
        assertEquals(120, dragon.getWeight());

        assertDeepEquals(dragon2, manager.getDragonById(dragon2.getId()));
    }

    @Test
    public void testUpdateDragonWithWrongArguments() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);

        manager.createDragon(dragon);
        Long dragonId = dragon.getId();

        try{
            manager.updateDragon(null);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon.setId(null);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setId(null);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setName(null);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setName("");
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            Date plusHour = cal.getTime();
            dragon.setBorn(plusHour);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setRace(null);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setRace("");
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setNumberOfHeads(0);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setNumberOfHeads(-1);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setWeight(0);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }

        try{
            dragon = manager.getDragonById(dragonId);
            dragon.setWeight(-1);
            manager.updateDragon(dragon);
            fail();
        }catch(IllegalArgumentException ex){
            //OK
        }
    }

    @Test
    public void testDeleteDragon() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);
        Dragon dragon2 = newDragon("Ugly dragon", sdf.parse("16-03-1994 12:00:00"), "scrathcer", 2, 100);

        manager.createDragon(dragon);
        manager.createDragon(dragon2);

        manager.deleteDragon(dragon);

        assertNull(manager.getDragonById(dragon.getId()));
        assertNotNull(manager.getDragonById(dragon2.getId()));
    }

    @Test
    public void testDeleteDragonWithWrongArguments() throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        Dragon dragon = newDragon("Nice dragon", sdf.parse("15-03-1994 12:00:00"), "burster", 5, 150);

        manager.createDragon(dragon);

        try {
            manager.deleteDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon.setId(null);
            manager.deleteDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon.setId(1l);
            manager.deleteDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }


    private static Comparator<Dragon> idComparator = new Comparator<Dragon>() {

        @Override
        public int compare(Dragon o1, Dragon o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private static Dragon newDragon(String name, Date bornDate, String race, int numOfHeads, int weight){
        Dragon dragon = new Dragon();
        dragon.setName(name);
        dragon.setBorn(bornDate);
        dragon.setRace(race);
        dragon.setNumberOfHeads(numOfHeads);
        dragon.setWeight(weight);
        return dragon;
    }

    private void assertDeepEquals(List<Dragon> expectedList, List<Dragon> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Dragon expected = expectedList.get(i);
            Dragon actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Dragon expected, Dragon actual){
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBorn(), actual.getBorn());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getNumberOfHeads(), actual.getNumberOfHeads());
        assertEquals(expected.getRace(), actual.getRace());
        assertEquals(expected.getWeight(), actual.getWeight());
    }

    private void assertDeepNotEquals(Dragon expected, Dragon actual){
        assertNotEquals(expected.getId(), actual.getId());
        assertNotEquals(expected.getBorn(), actual.getBorn());
        assertNotEquals(expected.getName(), actual.getName());
        assertNotEquals(expected.getNumberOfHeads(), actual.getNumberOfHeads());
        assertNotEquals(expected.getRace(), actual.getRace());
        assertNotEquals(expected.getWeight(), actual.getWeight());
    }
}