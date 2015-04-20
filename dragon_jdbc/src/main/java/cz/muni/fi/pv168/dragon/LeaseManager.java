package cz.muni.fi.pv168.dragon;

import java.util.Collection;
import java.util.Date;

public interface LeaseManager {

    public void createLease(Lease lease);

    public Lease getLeaseByID(Long ID);

    public Collection<Lease> getAllLeases();

    public Collection<Lease> getAllLeasesByEndDate(Date endDate);

    public Collection<Lease> findLeasesForCustomer(Customer customer);

    public Collection<Lease> findLeasesForDragon(Dragon dragon);

    public void updateLease(Lease lease);

    public void deleteLease(Lease lease);




}
