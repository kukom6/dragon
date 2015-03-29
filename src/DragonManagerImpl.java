import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonManagerImpl implements DragonManager {

    private final static Logger log = LoggerFactory.getLogger(DragonManagerImpl.class);

    private final DataSource dataSource;

    private final TimeService timeService;

    public DragonManagerImpl(DataSource dataSource, TimeService timeService) {
        this.dataSource = dataSource;
        this.timeService = timeService;
    }

    @Override
    public void createDragon(Dragon dragon) throws ServiceFailureException {
        checkDragon(dragon);
        if (dragon.getId() != null) {
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
                throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert dragon " + dragon);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            dragon.setId(getKey(keyRS, dragon));
        } catch (SQLException ex) {
            log.error("db connection problem in createDragon()", ex);
            throw new ServiceFailureException("Error when creating dragons", ex);
        }
    }

    private void checkDragon(Dragon dragon){
        if (dragon == null) {
            throw new IllegalArgumentException("dragon is null");
        }

        if (dragon.getName() == null || dragon.getName().isEmpty()) {
            throw new IllegalArgumentException("dragon name is emptystring or null");
        }

        Date dateNow = timeService.getCurrentDate();
        if(dragon.getBorn().after(dateNow)){
            throw new IllegalArgumentException("born date is in future");
        }

        if (dragon.getRace() == null || dragon.getRace().isEmpty()) {
            throw new IllegalArgumentException("dragon race is emptystring or null");
        }

        if(dragon.getNumberOfHeads() <= 0){
            throw new IllegalArgumentException("dragon number of heads is negative or zero");
        }

        if(dragon.getWeight() <= 0){
            throw new IllegalArgumentException("dragon weight is negative or zero");
        }
    }

    private Long getKey(ResultSet keyRS, Dragon dragon) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + dragon
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + dragon
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + dragon
                    + " - no key found");
        }
    }

    @Override
    public Dragon getDragonById(Long id) throws ServiceFailureException {
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }

        if(id < 0){
            throw new IllegalArgumentException("id is negative or zero");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS WHERE ID=?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                Dragon dragon = resultSetToDragon(rs);
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + dragon + " and " + resultSetToDragon(rs));
                }
                return dragon;
            }else{
                return null;
            }
        } catch (SQLException ex) {
            log.error("db connection problem while retrieving dragon by id.", ex);
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
        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, \"NAME\", BORN, RACE, HEADS, WEIGHT FROM DRAGONS")) {
                ResultSet rs = st.executeQuery();
                List<Dragon> dragons= new ArrayList<>();
                while(rs.next()){
                    dragons.add(resultSetToDragon(rs));
                }
                return dragons;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving all dragons", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByName(String name) throws ServiceFailureException {
        if(name == null || name.isEmpty()){
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
            return dragons;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving dragon by name.", ex);
            throw new ServiceFailureException("Error when retrieving dragons by name", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByRace(String race) throws ServiceFailureException {
        if(race == null || race.isEmpty()){
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
            return dragons;
        } catch(SQLException ex) {
            log.error("db connection problem when retrieving dragons by race.", ex);
            throw new ServiceFailureException("Error when retrieving dragons by race", ex);
        }
    }

    @Override
    public Collection<Dragon> getDragonsByNumberOfHeads(int number) throws ServiceFailureException {
        if(number <= 0){
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
            return dragons;
        } catch(SQLException ex) {
            log.error("db connection problem when retrieving dragons by number of heads", ex);
            throw new ServiceFailureException("Error when retrieving dragons by number of heads", ex);
        }
    }

   @Override
    public void updateDragon(Dragon dragon) throws ServiceFailureException {
       checkDragon(dragon);
       if (dragon.getId() == null) {
           throw new IllegalArgumentException("dragon id is null");
       }

       try (Connection conn = dataSource.getConnection();
           PreparedStatement st = conn.prepareStatement("UPDATE DRAGONS SET \"NAME\"=?, BORN=?, RACE=?, HEADS=?, WEIGHT=? WHERE id=?")) {
           st.setString(1, dragon.getName());
           st.setTimestamp(2, new Timestamp(dragon.getBorn().getTime()));
           st.setString(3, dragon.getRace());
           st.setInt(4, dragon.getNumberOfHeads());
           st.setInt(5, dragon.getWeight());
           st.setLong(6,dragon.getId());
           if(st.executeUpdate() != 1) {
               throw new IllegalArgumentException("dragon with id=" + dragon.getId() + " do not exist");
           }
       } catch(SQLException ex) {
           log.error("db connection problem when updating dragon.", ex);
           throw new ServiceFailureException("Error when updating dragon", ex);
       }
    }

    @Override
    public void deleteDragon(Dragon dragon) throws ServiceFailureException {
        if(dragon == null){
            throw new IllegalArgumentException("dragon is null");
        }
        if(dragon.getId() == null){
            throw new IllegalArgumentException("dragon id is null");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("DELETE FROM DRAGONS WHERE id=?")) {
            st.setLong(1,dragon.getId());
            if(st.executeUpdate() != 1) {
                throw new IllegalArgumentException("dragon with id=" + dragon.getId() + " do not exist");
            }
        } catch(SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when deleting dragon", ex);
        }
    }


}
