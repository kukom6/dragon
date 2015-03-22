
import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface CustomerManager {

    public void createCustomer(Customer customer);

    public Customer getCustomerByID(Long id);

    public Customer getCustomerByIDCard(String idCard);

    public Collection<Customer> getAllCustomers();

    public Collection<Customer> getAllCustomersByName(String name,String surname);

    public void updateCustomer(Customer customer);

    public void deleteCustomer(Customer customer);
}
