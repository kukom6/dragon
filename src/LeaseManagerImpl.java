import java.util.Collection;
import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class LeaseManagerImpl implements LeaseManager {
    @Override
    public void createLease(Lease lease) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Lease getLeaseByID(Long ID) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Customer> getAllLeases() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Customer> getAllCustomersByEndLease(Date endLease) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Customer> getAllCustomersByStartLease(Date startLease) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Customer> findLeasesForCustomer(Customer customer) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Customer> findLeasesForDragon(Dragon dragon) {
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
