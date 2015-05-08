package cz.muni.fi.pv168.dragon;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DragonManagerImpl implements DragonManager {


    private static final Logger log = java.util.logging.Logger.getLogger(DragonManagerImpl.class.getCanonicalName());


    private final DataSource dataSource;

    private final TimeService timeService;

    public DragonManagerImpl(DataSource dataSource, TimeService timeService) {
        this.dataSource = dataSource;
        this.timeService = timeService;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            log.log(Level.SEVERE, "DataSource is null.");
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createDragon(Dragon dragon) throws ServiceFailureException {
        log.log(Level.INFO, "Create dragon: "+dragon);

        checkDataSource();
        checkDragon(dragon);
        if (dragon.getId() != null) {
            log.log(Level.SEVERE, "Create dragon illegal argument exception: dragon id is already created.");
            throw new IllegalArgumentException("dragon id is already created");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("INSERT INTO DRAGONS (\"NAME\", BORN, RACE, HEADS, WEIGHT) VALUES (?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, dragon.getName());
            st.setTimestamp(2, new Timestamp(dragon.getBorn().getTime()));
            st.setString(3, dragon.getRace());
            st.setInt(4, dragon.getNumberOfHeads());
            st.setInt(5, dragon.getWeight());

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                log.log(Level.SEVERE, "Create dragon service failure exception: " + "Internal Error: More rows inserted when trying to insert dragon " + dragon);
                throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert dragon " + dragon);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            dragon.setId(getKey(keyRS, dragon));

            log.log(Level.INFO, "Create dragon "+dragon+" is OK.");
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem in createDragon()", ex);
            throw new ServiceFailureException("Error when creating dragon", ex);
        }
    }

    private void checkDragon(Dragon dragon){

        if (dragon == null) {
            log.log(Level.SEVERE, "Check dragon illegal argument exception: dragon id null");
            throw new IllegalArgumentException("dragon is null");
        }

        if (dragon.getName() == null || dragon.getName().isEmpty()) {
            log.log(Level.SEVERE, "Check dragon illegal argument exception: dragon name is emptystring or null");
            throw new IllegalArgumentException("dragon name is emptystring or null");
        }

        Date dateNow = timeService.getCurrentDate();
        if(dragon.getBorn().after(dateNow)){
            log.log(Level.SEVERE, "Check dragon illegal argument exception: born date is in future");
            throw new IllegalArgumentException("born date is in future");
        }

        if (dragon.getRace() == null || dragon.getRace().isEmpty()) {
            log.log(Level.SEVERE, "Check dragon illegal argument exception: dragon race is emptystring or null");
            throw new IllegalArgumentException("dragon race is emptystring or null");
        }

        if(dragon.getNumberOfHeads() <= 0){
            log.log(Level.SEVERE, "Check dragon illegal argument exception: dragon number of heads is negative or zero");
            throw new IllegalArgumentException("dragon number of heads is negative or zero");
        }

        if(dragon.getWeight() <= 0){
            log.log(Level.SEVERE, "Check dragon illegal argument exception: dragon weight is negative or zero");
            throw new IllegalArgumentException("dragon weight is negative or zero");
        }
    }

    private Long getKey(ResultSet keyRS, Dragon dragon) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert dragon " + dragon
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());

                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert dragon " + dragon
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert dragon " + dragon
                        + " - more keys found");
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert dragon " + dragon
                        + " - more keys found");
            }
            return result;
        } else {
            log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                    "Internal Error: Generated key"
                    + "retriving failed when trying to insert dragon " + dragon
                    + " - no key found");
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert dragon " + dragon
                    + " - no key found");
        }
    }

    @Override
    public Dragon getDragonById(Long id) throws ServiceFailureException {
        log.log(Level.INFO, "Get Dragon by ID:"+id);
        checkDataSource();

        if(id == null){
            log.log(Level.SEVERE, "Get dragon illegal argument exception: id is null.");
            throw new IllegalArgumentException("id is null");
        }

        if(id < 0){
            log.log(Level.SEVERE, "Get dragon illegal argument exception: id is negative or zero.");
            throw new IllegalArgumentException("id is negative or zero");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS WHERE ID=?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                Dragon dragon = resultSetToDragon(rs);
                if (rs.next()) {
                    log.log(Level.SEVERE, "Service failure exception: Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + dragon + " and " + resultSetToDragon(rs));
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + dragon + " and " + resultSetToDragon(rs));
                }
                log.log(Level.INFO, "Get dragon by ID " +id+" is OK");
                return dragon;
            }else{
                return null;
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem while retrieving dragon by id.", ex);
            throw new ServiceFailureException("Error when retrieving dragon by id", ex);
        }
    }

    private Dragon resultSetToDragon(ResultSet rs) throws SQLException{
        Dragon dragon = new Dragon();
        dragon.setId(rs.getLong("ID"));
        dragon.setName(rs.getString("NAME"));
        dragon.setBorn(rs.getTimestamp("BORN"));
        dragon.setRace(rs.getString("RACE"));
        dragon.setNumberOfHeads(rs.getInt("HEADS"));
        dragon.setWeight(rs.getInt("WEIGHT"));
        return dragon;
    }

    @Override
    public Collection<Dragon> getAllDragons() throws ServiceFailureException {
        log.log(Level.INFO, "Get all dragons.");

        checkDataSource();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS")) {
                ResultSet rs = st.executeQuery();
                List<Dragon> dragons= new ArrayList<>();
                while(rs.next()){
                    dragons.add(resultSetToDragon(rs));
                }
                log.log(Level.INFO, "Get all dragons is OK.");
                return dragons;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem when retrieving all dragon", ex);
            throw new ServiceFailureException("Error when retrieving all dragon", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByName(String name) throws ServiceFailureException {
        log.log(Level.INFO, "Get dragons by name:"+name);
        checkDataSource();

        if(name == null || name.isEmpty()){
            log.log(Level.SEVERE, "Get dragon by name illegal argument exception: name is null or empty string.");
            throw new IllegalArgumentException("name is null or empty string");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS WHERE \"NAME\"=?")) {
            st.setString(1,name);
            ResultSet rs = st.executeQuery();
            List<Dragon> dragons= new ArrayList<>();
            while(rs.next()){
                dragons.add(resultSetToDragon(rs));
            }
            log.log(Level.INFO, "Get dragons by name is OK.");
            return dragons;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "db connection problem when retrieving dragon by name.", ex);
            throw new ServiceFailureException("Error when retrieving dragon by name", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByRace(String race) throws ServiceFailureException {
        log.log(Level.INFO, "Get dragons by race: "+race);
        checkDataSource();

        if(race == null || race.isEmpty()){
            log.log(Level.SEVERE, "Get dragon by race illegal argument exception: race is null or empty string.");
            throw new IllegalArgumentException("race is null or empty string");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS WHERE RACE=?")) {
            st.setString(1,race);
            ResultSet rs = st.executeQuery();
            List<Dragon> dragons= new ArrayList<>();
            while(rs.next()){
                dragons.add(resultSetToDragon(rs));
            }
            log.log(Level.INFO, "Get dragons by Race: "+race+" is OK.");
            return dragons;
        } catch(SQLException ex) {
            log.log(Level.SEVERE, "db connection problem when retrieving dragon by race.", ex);
            throw new ServiceFailureException("Error when retrieving dragon by race", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByNumberOfHeads(int number) throws ServiceFailureException {
        log.log(Level.INFO, "Get dragons by number of heads :"+number);
        checkDataSource();

        if(number <= 0){
            log.log(Level.SEVERE, "Get dragon by number of heads illegal argument exception: Number of heads is negative or zero.");
            throw new IllegalArgumentException("Number of heads is negative or zero");
        }
        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS WHERE HEADS=?")){
            st.setInt(1,number);
            ResultSet rs = st.executeQuery();
            List<Dragon> dragons= new ArrayList<>();
            while(rs.next()){
                dragons.add(resultSetToDragon(rs));
            }
            log.log(Level.INFO, "Get dragons by number: "+number+" of heads is OK.");
            return dragons;
        } catch(SQLException ex) {
            log.log(Level.SEVERE, "db connection problem when retrieving dragon by number of heads", ex);
            throw new ServiceFailureException("Error when retrieving dragon by number of heads", ex);
        }
    }

   @Override
    public void updateDragon(Dragon dragon) throws ServiceFailureException {
       log.log(Level.INFO, "Update dragon: "+dragon);
       checkDataSource();
       checkDragon(dragon);
       if (dragon.getId() == null) {
           log.log(Level.SEVERE, "Update dragon illegal argument exception: dragon id is null.");
           throw new IllegalArgumentException("dragon id is null");
       }

       try (Connection conn = dataSource.getConnection();
           PreparedStatement st = conn.prepareStatement("UPDATE DRAGONS SET \"NAME\"=?, BORN=?, RACE=?, HEADS=?, WEIGHT=? WHERE id=?")) {
           st.setString(1, dragon.getName());
           st.setTimestamp(2, new Timestamp(dragon.getBorn().getTime()));
           st.setString(3, dragon.getRace());
           st.setInt(4, dragon.getNumberOfHeads());
           st.setInt(5, dragon.getWeight());
           st.setLong(6, dragon.getId());
           if(st.executeUpdate() != 1) {
               log.log(Level.SEVERE, "Update dragon illegal argument exception: dragon with id=" + dragon.getId() + " do not exist.");
               throw new IllegalArgumentException("dragon with id=" + dragon.getId() + " do not exist");
           }
           log.log(Level.INFO, "Update dragon: " +dragon+ " is OK.");
       } catch(SQLException ex) {
           log.log(Level.SEVERE, "db connection problem when updating dragon.", ex);
           throw new ServiceFailureException("Error when updating dragon", ex);
       }
    }

    @Override
    public void deleteDragon(Dragon dragon) throws ServiceFailureException {
        log.log(Level.INFO, "Delete dragon: "+dragon);

        checkDataSource();

        if(dragon == null){
            log.log(Level.SEVERE, "Delete dragon illegal argument exception: dragon is null.");
            throw new IllegalArgumentException("dragon is null");
        }
        if(dragon.getId() == null){
            log.log(Level.SEVERE, "Delete dragon illegal argument exception: dragon id is null.");
            throw new IllegalArgumentException("dragon id is null");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("DELETE FROM DRAGONS WHERE id=?")) {
            st.setLong(1, dragon.getId());
            if(st.executeUpdate() != 1) {
                log.log(Level.SEVERE, "Delete dragon illegal argument exception: dragon with id=" + dragon.getId() + " do not exist.");
                throw new IllegalArgumentException("dragon with id=" + dragon.getId() + " do not exist");
            }
            log.log(Level.INFO, "Delete dragon "+dragon+" is OK.");

        } catch(SQLException ex) {
            log.log(Level.SEVERE, "db connection problem", ex);
            throw new ServiceFailureException("Error when deleting dragon", ex);
        }
    }


}
