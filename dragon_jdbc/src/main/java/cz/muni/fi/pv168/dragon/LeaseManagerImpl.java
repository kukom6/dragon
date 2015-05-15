package cz.muni.fi.pv168.dragon;

import java.util.logging.Level;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

public class LeaseManagerImpl implements LeaseManager {

    private static final Logger log = Logger.getLogger(LeaseManagerImpl.class.getCanonicalName());

    private final DataSource dataSource;

    private final TimeService timeService;

    private DragonManagerImpl dragonManager;
    private CustomerManagerImpl customerManager;

    private void checkDataSource() {
        if (dataSource == null) {
            log.log(Level.SEVERE, "DataSource is null.");
            throw new IllegalStateException("DataSource is not set");
        }
    }

    public LeaseManagerImpl(DataSource dataSource, TimeService timeService) {
        this.dataSource = dataSource;
        this.timeService = timeService;
        dragonManager = new DragonManagerImpl(dataSource, timeService);
        customerManager = new CustomerManagerImpl(dataSource);
    }

    @Override
    public void createLease(Lease lease) {
        log.log(Level.INFO, "Create lease: "+lease);
        checkDataSource();
        checkLease(lease);

        if(lease.getId() != null){
            log.log(Level.SEVERE, "Create lease illegal argument exception: id should be null.");
            throw new IllegalArgumentException("id should be null");
        }

        if(lease.getStartDate() != null){
            log.log(Level.SEVERE, "Create lease illegal argument exception: startDate should be null.");
            throw new IllegalArgumentException("startDate should be null");
        }

        if(lease.getEndDate().before(timeService.getCurrentDate())){
            log.log(Level.SEVERE, "Create lease illegal argument exception: end lease is not in future.");
            throw new IllegalArgumentException("end lease is not in future.");
        }

        if(isDragonBorrowed(lease.getDragon())){
            log.log(Level.SEVERE, "Create lease illegal argument exception: dragon is already borrowed.");
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
                log.log(Level.SEVERE, "Create lease Service failure exception: bad BigDecimal value.");
                throw new ServiceFailureException("bad BigDecimal value");
            }

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                log.log(Level.SEVERE, "Create lease Service failure exception: Internal Error: More rows inserted when trying to insert lease " + lease);
                throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert lease " + lease);
            }
            ResultSet keyRS = st.getGeneratedKeys();
            lease.setId(getKey(keyRS, lease));
            lease.setStartDate(timeService.getCurrentDate());
            log.log(Level.INFO, "Create lease: "+lease+" is OK.");

        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem in createDragon()", ex);
            throw new ServiceFailureException("Error when creating dragon", ex);
        }
    }

    private Long getKey(ResultSet keyRS, Lease lease) throws SQLException {

        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());

                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                        "Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - more keys found");

                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + lease
                        + " - more keys found");
            }
            return result;
        } else {
            log.log(Level.SEVERE, "getKey: Service Failure exception: " +
                    "Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + lease
                    + " - no key found");

            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + lease
                    + " - no key found");
        }
    }

    private void checkLease(Lease lease){
        if(lease.getPrice() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: lease price is null.");
            throw new IllegalArgumentException("lease price is null");
        }

        if(lease.getPrice().compareTo(new BigDecimal(0)) < 0){
            log.log(Level.SEVERE, "Check lease illegal argument exception: lease price is negative.");
            throw new IllegalArgumentException("lease price is negative");
        }

        if(lease.getEndDate() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: end date is null.");
            throw new IllegalArgumentException("end date is null");
        }

        if(lease.getDragon() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: dragon is null.");
            throw new IllegalArgumentException("dragon is null");
        }

        if(lease.getDragon().getId() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: dragon id is null.");
            throw new IllegalArgumentException("dragon id is null");
        }

        if(lease.getDragon().getId() < 0){
            log.log(Level.SEVERE, "Check lease illegal argument exception: dragon id is negative.");
            throw new IllegalArgumentException("dragon id is negative");
        }

        if(lease.getCustomer() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: customer is null.");
            throw new IllegalArgumentException("customer is null");
        }

        if(lease.getCustomer().getId() == null){
            log.log(Level.SEVERE, "Check lease illegal argument exception: customer id is null.");
            throw new IllegalArgumentException("customer id is null");
        }

        if(lease.getCustomer().getId() < 0){
            log.log(Level.SEVERE, "Check lease illegal argument exception: customer id is negative.");
            throw new IllegalArgumentException("customer id is negative");
        }
    }

    @Override
    public Lease getLeaseByID(Long id) {
        log.log(Level.INFO, "Get lease by ID: "+id);

        checkDataSource();

        if(id == null){
            log.log(Level.SEVERE, "Get lease by ID illegal argument exception: id is null.");
            throw new IllegalArgumentException("id is null");
        }

        if(id < 0){
            log.log(Level.SEVERE, "Get lease by ID illegal argument exception: id is negative.");
            throw new IllegalArgumentException("id is negative");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE ID=?")) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                Lease lease = resultSetToLease(rs);
                if (rs.next()) {
                    log.log(Level.SEVERE, "Get lease by ID Service failure exception: " +
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                }
                log.log(Level.INFO, "Get lease by ID: "+id+" is OK.");
                return lease;
            }else{
                return null;
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem while retrieving lease by id.", ex);
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
            log.log(Level.SEVERE, "Result set to lease Service failure exception: bad BigDecimal value.",ex);
            throw new ServiceFailureException("bad BigDecimal value");
        }
        return lease;
    }

    private boolean isDragonBorrowed(Dragon dragon){
        if(dragon == null){
            log.log(Level.SEVERE, "is dragon borrowed illegal argument exception: dragon is null.");
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            log.log(Level.SEVERE, "is dragon borrowed illegal argument exception: dragon id is null.");
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
            log.log(Level.SEVERE,"db connection problem when retrieving leases for dragon", ex);
            throw new ServiceFailureException("Error when leases for dragon", ex);
        }
    }

    @Override
    public Collection<Lease> getAllLeases() {
        log.log(Level.INFO, "Get all leases.");

        checkDataSource();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES")) {
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            log.log(Level.INFO, "Get all leases is OK.");
            return leases;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem when retrieving all leases", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }

    @Override
    public Collection<Lease> getAllLeasesByEndDate(Date endDate) {
        log.log(Level.INFO, "Get all leases by edn date: "+endDate);

        checkDataSource();

        if(endDate == null){
            log.log(Level.SEVERE, "Get all leases by end date illegal argument exception: endlease is null.");
            throw new IllegalArgumentException("endlease is null");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, RETURNDATE, PRICE FROM LEASES WHERE ENDDATE=?")) {
            st.setTimestamp(1, new Timestamp(endDate.getTime()));
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            log.log(Level.INFO, "Get all leases by edn date: "+endDate+" is OK.");

            return leases;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem when retrieving lease for customer.", ex);
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }
    }

    @Override
    public Collection<Lease> findLeasesForCustomer(Customer customer) {
        log.log(Level.INFO, "Find leases for customer: "+customer);
        checkDataSource();

        if(customer == null){
            log.log(Level.SEVERE, "Find leases for customer illegal argument exception: customer is null.");
            throw new IllegalArgumentException("customer is null");
        }

        if(customer.getId() == null){
            log.log(Level.SEVERE, "Find leases for customer illegal argument exception: customer id is null.");
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
            log.log(Level.INFO, "Find leases for customer: "+customer+" is OK.");
            return leases;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem when retrieving lease for customer.", ex);
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }
    }

    @Override
    public Collection<Lease> findLeasesForDragon(Dragon dragon) {
        log.log(Level.INFO, "Find leases for dragon: "+dragon);

        checkDataSource();

        if(dragon == null){
            log.log(Level.SEVERE, "Find leases for dragon illegal argument exception: dragon is null.");
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            log.log(Level.SEVERE, "Find leases for dragon illegal argument exception: dragon id is null.");
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
            log.log(Level.INFO, "Find leases for dragon: "+dragon+" is OK.");
            return leases;
        } catch (SQLException ex) {
            log.log(Level.SEVERE,"db connection problem when retrieving lease for dragon.", ex);
            throw new ServiceFailureException("Error when retrieving lease for dragon", ex);
        }
    }

    private boolean checkDragonID(Long leaseID, Dragon dragon){
        if(dragon == null){
            log.log(Level.SEVERE, "Check dragon ID illegal argument exception: dragon is null.");
            throw new IllegalArgumentException("dragon is null");
        }

        if(dragon.getId() == null){
            log.log(Level.SEVERE, "Check dragon ID illegal argument exception: dragon id is null.");
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
            log.log(Level.SEVERE,"db connection problem when retrieving lease for dragon", ex);
            throw new ServiceFailureException("Error when lease for dragon", ex);
        }
    }

    @Override
    public void updateLease(Lease lease) {
        log.log(Level.INFO, "Update lease: "+lease);
        checkDataSource();

        checkLease(lease);
        if (lease.getId() == null) {
            log.log(Level.SEVERE, "Update lease illegal argument exception: lease id is null.");
            throw new IllegalArgumentException("lease id is null");
        }

        if(lease.getStartDate() == null){
            log.log(Level.SEVERE, "Update lease illegal argument exception: startDate is null.");
            throw new IllegalArgumentException("startDate is null");
        }

        if(lease.getReturnDate() != null && lease.getReturnDate().before(lease.getStartDate())){
            log.log(Level.SEVERE, "Update lease illegal argument exception: startDate is after return date.");
            throw new IllegalArgumentException("startDate is after return date.");
        }

        if(lease.getEndDate().before(lease.getStartDate())){
            log.log(Level.SEVERE, "Update lease illegal argument exception: end date is after return date.");
            throw new IllegalArgumentException("end date is after return date.");
        }

        if(lease.getReturnDate() == null){
            if(checkDragonID(lease.getId(), lease.getDragon())){
                log.log(Level.SEVERE, "Update lease illegal argument exception: Dragon is borrowed in another lease.");
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
                log.log(Level.SEVERE, "Update lease Service failure exception: bad BigDecimal value.");
                throw new ServiceFailureException("bad BigDecimal value");
            }

            st.setLong(7, lease.getId());
            if(st.executeUpdate() != 1) {
                log.log(Level.SEVERE, "Update lease illegal argument exception:" +
                        "lease with id=" + lease.getId() + " do not exist.");
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
            log.log(Level.INFO, "Update lease: "+lease+" is OK.");
        } catch(SQLException ex) {
            log.log(Level.SEVERE,"db connection problem when updating lease.", ex);
            throw new ServiceFailureException("Error when updating lease", ex);
        }
    }
    
    @Override
    public void deleteLease(Lease lease) {
        log.log(Level.INFO, "Delete lease: "+lease);

        checkDataSource();

        if(lease == null){
            log.log(Level.SEVERE, "Delete lease illegal argument exception: lease is null.");
            throw new IllegalArgumentException("lease is null");
        }

        if(lease.getId() == null){
            log.log(Level.SEVERE, "Delete lease illegal argument exception: lease id is null.");
            throw new IllegalArgumentException("lease id is null");
        }

        /*if(lease.getReturnDate() == null){
            log.log(Level.SEVERE, "Delete lease illegal argument exception: dragon is not returned yet.");
            throw new IllegalArgumentException("dragon is not returned yet");
        }*/

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("DELETE FROM LEASES WHERE id=?")) {
            st.setLong(1, lease.getId());
            if(st.executeUpdate() != 1) {
                log.log(Level.SEVERE, "Delete lease illegal argument exception: " +
                        "lease with id=" + lease.getId() + " do not exist");
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
            log.log(Level.INFO, "Delete lease: "+lease+" is OK.");
        } catch(SQLException ex) {
            log.log(Level.SEVERE,"db connection problem while deleting lease", ex);
            throw new ServiceFailureException("Error when deleting lease", ex);
        }
    }
}
