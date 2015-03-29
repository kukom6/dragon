import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Collection;
import java.util.Date;
import java.util.IllegalFormatException;

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
        if(lease.getId() != null){
            throw new IllegalArgumentException("id is not null");
        }

        checkLease(lease);

        if(lease.getEndDate().before(timeService.getCurrentDate())){
            throw new IllegalArgumentException("end lease is not in future.");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("INSERT INTO LEASES (IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, PRICE) VALUES (?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {

            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getDragon().getId());
            st.setTimestamp(3, new Timestamp(lease.getStartDate().getTime()));
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
        if (lease == null) {
            throw new IllegalArgumentException("lease is null");
        }

        if(lease.getPrice() == null){
            throw new IllegalArgumentException("lease price is null");
        }

        if(lease.getPrice().compareTo(new BigDecimal(0)) < 0){
            throw new IllegalArgumentException("lease price is negative");
        }

        if(lease.getStartDate() == null){
            throw new IllegalArgumentException("start date is null");
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

        if(lease.getCustomer().getId() == null || lease.getCustomer().getId() < 0){
            throw new IllegalArgumentException("customer id is null or negative");
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

    private Lease resultSetToLease(ResultSet rs) throws SQLException{
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

    @Override
    public Collection<Lease> getAllLeases() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Lease> getAllLeasesByEndDate(Date endLease) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Lease> findLeasesForCustomer(Customer customer) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Lease> findLeasesForDragon(Dragon dragon) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void updateLease(Lease lease) {
        throw new UnsupportedOperationException("not implemented");
    }
    
    @Override
    public void deleteLease(Lease lease) {
        throw new UnsupportedOperationException("not implemented");
    }
}
