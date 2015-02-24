package com;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class Lease {
    private BigDecimal price;
    private Date endLease;
    private Date startLease;
    private Dragon dragon;
    private Customer customer;
    private Date returnDate;

    public Lease(){

    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getEndLease() {
        return endLease;
    }

    public void setEndLease(Date endLease) {
        this.endLease = endLease;
    }

    public Date getStartLease() {
        return startLease;
    }

    public void setStartLease(Date startLease) {
        this.startLease = startLease;
    }

    public Dragon getDragon() {
        return dragon;
    }

    public void setDragon(Dragon dragon) {
        this.dragon = dragon;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
