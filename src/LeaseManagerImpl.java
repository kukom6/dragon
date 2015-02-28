import java.util.Collection;
import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class LeaseManagerImpl implements LeaseManager {
    @Override
    public void createLease(Lease lease) {

    }

    @Override
    public Lease getLeaseByID(Long ID) {
        return null;
    }

    @Override
    public Collection<Customer> getAllLeases() {
        return null;
    }

    @Override
    public Collection<Customer> getAllCustomersByEndLease(Date endLease) {
        return null;
    }

    @Override
    public Collection<Customer> getAllCustomersByStartLease(Date startLease) {
        return null;
    }

    @Override
    public Collection<Customer> findLeasesForCustomer(Customer customer) {
        return null;
    }

    @Override
    public Collection<Customer> findLeasesForDragon(Dragon dragon) {
        return null;
    }

    @Override
    public void updateLease(Lease lease) {

    }
    
    @Override
    public void deleteLease(Lease lease) {

    }
}
