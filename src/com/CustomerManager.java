package com;

import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface CustomerManager {

    public void createCustomer(Customer customer);

    public Customer getCustomerByID(Long ID);


    public Collection<Customer> getAllCustomers();

    public Collection<Customer> getAllCustomersByName(String name);

    public void updateCustomer(Customer customer);


    public void deleteCustomer(Customer customer);

}
