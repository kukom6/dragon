import java.util.Collection;
import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface LeaseManager {

    public void createLease(Lease lease);

    public Lease getLeaseByID(Long ID);

    public Collection<Lease> getAllLeases();

    public Collection<Lease> getAllLeasesByStartDate(Date endLease);

    public Collection<Lease> getAllLeasesByEndDate(Date startLease);

    public Collection<Lease> findLeasesForCustomer(Customer customer);

    public Collection<Lease> findLeasesForDragon(Dragon dragon);

    public void updateLease(Lease lease);

    public void deleteLease(Lease lease);




}
