import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class LeaseManagerImpl implements LeaseManager {

    private final static Logger log = LoggerFactory.getLogger(DragonManagerImpl.class);

    private final DataSource dataSource;

    private final TimeService timeService;

    private DragonManagerImpl dragonManager;
    private CustomerManagerImpl customerManager;



    public LeaseManagerImpl(DataSource dataSource, TimeService timeService) {
        this.dataSource = dataSource;
        this.timeService = timeService;
        dragonManager = new DragonManagerImpl(dataSource, timeService);
        customerManager = new CustomerManagerImpl(dataSource);
    }

    @Override
    public void createLease(Lease lease) {
        checkLease(lease);

        if(lease.getId() != null){
            throw new IllegalArgumentException("id should be null");
        }

        if(lease.getStartDate() != null){
            throw new IllegalArgumentException("startDate should be null");
        }

        if(lease.getEndDate().before(timeService.getCurrentDate())){
            throw new IllegalArgumentException("end lease is not in future.");
        }

        if(isDragonBorrowed(lease.getDragon())){
            throw new IllegalArgumentException("dragon is already borrowed");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("INSERT INTO LEASES (IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, PRICE) VALUES (?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {

            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getDragon().getId());
            st.setTimestamp(3, new Timestamp(timeService.getCurrentDate().getTime()));
            st.setTimestamp(4, new Timestamp(lease.getEndDate().getTime()));

            try {
                st.setBigDecimal(5, lease.getPrice().setScale(2));
            } catch (ArithmeticException ex){
                throw new ServiceFailureException("bad BigDecimal value");
            }

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert lease " + lease);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            lease.setId(getKey(keyRS, lease));
            lease.setStartDate(timeService.getCurrentDate());
        } catch (SQLException ex) {
            log.error("db connection problem in createDragon()", ex);
            throw new ServiceFailureException("Error when creating dragons", ex);
        }
    }

    private Long getKey(ResultSet keyRS, Lease lease) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + lease
                    + " - no key found");
        }
    }

    private void checkLease(Lease lease){
        if(lease.getPrice() == null){
            throw new IllegalArgumentException("lease price is null");
        }

        if(lease.getPrice().compareTo(new BigDecimal(0)) < 0){
            throw new IllegalArgumentException("lease price is negative");
        }

        if(lease.getEndDate() == null){
            throw new IllegalArgumentException("end date is null");
        }

        if(lease.getDragon() == null){
            throw new IllegalArgumentException("dragon is null");
        }

        if(lease.getDragon().getId() == null){
            throw new IllegalArgumentException("dragon id is null");
        }

        if(lease.getDragon().getId() < 0){
            throw new IllegalArgumentException("dragon id is negative");
        }

        if(lease.getCustomer() == null){
            throw new IllegalArgumentException("customer is null");
        }

        if(lease.getCustomer().getId() == null){
            throw new IllegalArgumentException("customer id is null");
        }

        if(lease.getCustomer().getId() < 0){
            throw new IllegalArgumentException("customer id is negative");
        }
    }

    @Override
    public Lease getLeaseByID(Long id) {
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }

        if(id < 0){
            throw new IllegalArgumentException("id is negative");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE ID=?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                Lease lease = resultSetToLease(rs);
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                }
                return lease;
            }else{
                return null;
            }
        } catch (SQLException ex) {
            log.error("db connection problem while retrieving lease by id.", ex);
            throw new ServiceFailureException("Error when retrieving lease by id", ex);
        }
    }

    private Lease resultSetToLease(ResultSet rs) throws SQLException {
        Lease lease=new Lease();
        lease.setId(rs.getLong("ID"));
        lease.setCustomer(customerManager.getCustomerByID(rs.getLong("IDCUSTOMER")));
        lease.setDragon(dragonManager.getDragonById(rs.getLong("IDDRAGON")));
        lease.setStartDate(rs.getTimestamp("STARTDATE"));
        lease.setEndDate(rs.getTimestamp("ENDDATE"));
        lease.setReturnDate(rs.getTimestamp("RETURNDATE"));
        try {
            lease.setPrice(rs.getBigDecimal("PRICE").setScale(2));
        } catch (ArithmeticException ex) {
            throw new ServiceFailureException("bad BigDecimal value");
        }
        return lease;
    }

    private boolean isDragonBorrowed(Dragon dragon){
        if(dragon == null){
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            throw new IllegalArgumentException("dragon id is null");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT IDDRAGON, RETURNDATE FROM LEASES WHERE IDDRAGON=? AND RETURNDATE IS NULL")){
            st.setLong(1, dragon.getId());
            ResultSet rs = st.executeQuery();

            if(rs.next()){
                return true;
            }else{
                return false;
            }

        } catch (SQLException ex){
            log.error("db connection problem when retrieving leases for dragon", ex);
            throw new ServiceFailureException("Error when leases for dragon", ex);
        }
    }

    @Override
    public Collection<Lease> getAllLeases() {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES")) {
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving all leases", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }

    @Override
    public Collection<Lease> getAllLeasesByEndDate(Date endDate) {
        if(endDate == null){
            throw new IllegalArgumentException("cendlease is null");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE ENDDATE=?")) {
            st.setTimestamp(1, new Timestamp(endDate.getTime()));
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving lease for customer.", ex);
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }
    }

    @Override
    public Collection<Lease> findLeasesForCustomer(Customer customer) {
        if(customer == null){
            throw new IllegalArgumentException("customer is null");
        }

        if(customer.getId() == null){
            throw new IllegalArgumentException("customer id is null");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE IDCUSTOMER=?")) {
            st.setLong(1, customer.getId());
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving lease for customer.", ex);
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }
    }

    @Override
    public Collection<Lease> findLeasesForDragon(Dragon dragon) {
        if(dragon == null){
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            throw new IllegalArgumentException("dragon id is null");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE IDDRAGON=?")) {
            st.setLong(1, dragon.getId());
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            log.error("db connection problem when retrieving lease for dragon.", ex);
            throw new ServiceFailureException("Error when retrieving lease for dragon", ex);
        }
    }

    private boolean checkDragonID(Long leaseID, Dragon dragon){
        if(dragon == null){
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            throw new IllegalArgumentException("dragon id is null");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ID, IDDRAGON, RETURNDATE FROM LEASES WHERE IDDRAGON=? AND RETURNDATE IS NULL")){
            st.setLong(1, dragon.getId());
            ResultSet rs = st.executeQuery();

            if(rs.next()){
                return leaseID != rs.getLong("ID");
            }else{
                return false;
            }

        } catch (SQLException ex){
            log.error("db connection problem when retrieving lease for dragon", ex);
            throw new ServiceFailureException("Error when lease for dragon", ex);
        }
    }

    @Override
    public void updateLease(Lease lease) {
        checkLease(lease);
        if (lease.getId() == null) {
            throw new IllegalArgumentException("lease id is null");
        }

        if(lease.getStartDate() == null){
            throw new IllegalArgumentException("startDate is null");
        }

        if(lease.getReturnDate() != null && lease.getReturnDate().before(lease.getStartDate())){
            throw new IllegalArgumentException("startDate is after return date.");
        }

        if(lease.getEndDate().before(lease.getStartDate())){
            throw new IllegalArgumentException("end date is after return date.");
        }

        if(lease.getReturnDate() == null){
            if(checkDragonID(lease.getId(), lease.getDragon())){
                throw new IllegalArgumentException("Dragon is borrowed in another lease");
            }
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("UPDATE LEASES SET IDCUSTOMER=?, IDDRAGON=?, STARTDATE=?, ENDDATE=?, RETURNDATE=?, PRICE=? WHERE id=?")) {
            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getDragon().getId());
            st.setTimestamp(3, new Timestamp(lease.getStartDate().getTime()));
            st.setTimestamp(4, new Timestamp(lease.getEndDate().getTime()));

            if(lease.getReturnDate() == null){
                st.setNull(5,Types.TIMESTAMP);
            }else{
                st.setTimestamp(5, new Timestamp(lease.getReturnDate().getTime()));
            }

            try {
                st.setBigDecimal(6, lease.getPrice().setScale(2));
            } catch (ArithmeticException ex){
                throw new ServiceFailureException("bad BigDecimal value");
            }

            st.setLong(7, lease.getId());
            if(st.executeUpdate() != 1) {
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
        } catch(SQLException ex) {
            log.error("db connection problem when updating lease.", ex);
            throw new ServiceFailureException("Error when updating lease", ex);
        }
    }
    
    @Override
    public void deleteLease(Lease lease) {
        if(lease == null){
            throw new IllegalArgumentException("lease is null");
        }

        if(lease.getId() == null){
            throw new IllegalArgumentException("lease id is null");
        }

        if(lease.getReturnDate() == null){
            throw new IllegalArgumentException("dragon is not returned yet");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("DELETE FROM LEASES WHERE id=?")) {
            st.setLong(1, lease.getId());
            if(st.executeUpdate() != 1) {
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
        } catch(SQLException ex) {
            log.error("db connection problem while deleting lease", ex);
            throw new ServiceFailureException("Error when deleting lease", ex);
        }
    }
}
