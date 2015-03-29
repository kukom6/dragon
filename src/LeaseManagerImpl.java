import javax.sql.DataSource;
import java.util.Collection;
import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class LeaseManagerImpl implements LeaseManager {


    private final DataSource dataSource;

    public LeaseManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createLease(Lease lease) {
        throw new UnsupportedOperationException("not implemented");
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
    public Collection<Lease> getAllLeasesByStartDate(Date startLease) {
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
