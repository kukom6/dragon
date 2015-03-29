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


    public LeaseManagerImpl(DataSource dataSource, TimeService timeService) {
        this.dataSource = dataSource;
        this.timeService = timeService;
    }

    @Override
    public void createLease(Lease lease) {
        if(lease.getId() == null){
            throw new IllegalArgumentException("id is null");
        }

        checkLease(lease);

        if(lease.getReturnDate().after(timeService.getCurrentDate())){
            throw new IllegalArgumentException("return date is in future.");
        }

        try (Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("INSERT INTO LEASES (IDCUSTOMER, IDDRAGON, STARTDATE, ENDDATE, PRICE) VALUES (?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {

            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getDragon().getId());
            st.setTimestamp(3, new Timestamp(lease.getStartDate().getTime()));
            st.setTimestamp(4, new Timestamp(lease.getEndDate().getTime()));
            st.setBigDecimal(5, lease.getPrice());

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

        if(lease.getPrice().compareTo(new BigDecimal(0)) > 0){
            throw new IllegalArgumentException("lease price is negative");
        }

        if(lease.getStartDate() == null){
            throw new IllegalArgumentException("start date is null");
        }

        if(lease.getEndDate().before(timeService.getCurrentDate())){
            throw new IllegalArgumentException("end lease is not in future.");
        }

        if(lease.getEndDate() == null){
            throw new IllegalArgumentException("end date is null");
        }

        if(lease.getDragon() == null){
            throw new IllegalArgumentException("dragon is null");
        }

        if(lease.getDragon().getId() == null || lease.getDragon().getId() < 0){
            throw new IllegalArgumentException("dragon id is null or negative");
        }

        if(lease.getCustomer() == null){
            throw new IllegalArgumentException("customer is null");
        }

        if(lease.getCustomer().getId() == null || lease.getCustomer().getId() < 0){
            throw new IllegalArgumentException("customer id is null or negative");
        }
    }

    @Override
    public Lease getLeaseByID(Long ID) {
        throw new UnsupportedOperationException("not implemented");
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
