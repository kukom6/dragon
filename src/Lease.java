import java.math.BigDecimal;
import java.util.Date;

public class Lease {

    private Long id;
    private BigDecimal price;
    private Date startDate;
    private Date endDate;
    private Dragon dragon;
    private Customer customer;
    private Date returnDate;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Lease{" +
                "id=" + id +
                ", price=" + price +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", dragon=" + dragon +
                ", customer=" + customer +
                ", returnDate=" + returnDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lease lease = (Lease) o;

        if (customer != null ? !customer.equals(lease.customer) : lease.customer != null) return false;
        if (dragon != null ? !dragon.equals(lease.dragon) : lease.dragon != null) return false;
        if (endDate != null ? !endDate.equals(lease.endDate) : lease.endDate != null) return false;
        if (id != null ? !id.equals(lease.id) : lease.id != null) return false;
        if (price != null ? !price.equals(lease.price) : lease.price != null) return false;
        if (startDate != null ? !startDate.equals(lease.startDate) : lease.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (dragon != null ? dragon.hashCode() : 0);
        result = 31 * result + (customer != null ? customer.hashCode() : 0);
        return result;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
