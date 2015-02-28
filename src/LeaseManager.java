import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface LeaseManager {

    public void createLease(Lease lease);

    public Lease getLeaseByID(Long ID);

    public Collection<Customer> getAllLeases();

    public Collection<Customer> getAllCustomersByEndLease(java.util.Date endLease);

    public Collection<Customer> getAllCustomersByStartLease(java.util.Date startLease);

    public Collection<Customer> findLeasesForCustomer(Customer customer);

    public Collection<Customer> findLeasesForDragon(Dragon dragon);

    public void updateLease(Lease lease);

    public void deleteLease(Lease lease);




}
