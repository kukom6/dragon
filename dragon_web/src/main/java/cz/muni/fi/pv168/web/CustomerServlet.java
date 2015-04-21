package cz.muni.fi.pv168.web;
import cz.muni.fi.pv168.dragon.Customer;
import cz.muni.fi.pv168.dragon.CustomerManager;
import cz.muni.fi.pv168.dragon.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for managing books.
 */
@WebServlet(CustomerServlet.URL_MAPPING + "/*")
public class CustomerServlet extends HttpServlet {

    private static final String LIST_JSP = "/listCustomer.jsp";
    public static final String URL_MAPPING = "/customers";

    private final static Logger log = LoggerFactory.getLogger(CustomerServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        showCustomerList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getPathInfo();
        switch (action) {
            case "/add":
                String name = request.getParameter("name");
                String surname = request.getParameter("surname");
                String address = request.getParameter("address");
                String identityCard = request.getParameter("identityCard");
                String phoneNumber = request.getParameter("phoneNumber");
                if ( ( (address==null || address.length()==0) && (phoneNumber==null || phoneNumber.length()==0) )
                        || name == null || name.length() == 0
                        || surname == null || surname.length() == 0
                        || identityCard == null || identityCard.length() == 0) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty alebo aspon jeden kontaktny !");
                    showCustomerList(request, response);
                    break;
                }
                try {
                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setSurname(surname);
                    customer.setAddress(address);
                    customer.setIdentityCard(identityCard);
                    customer.setPhoneNumber(phoneNumber);

                    getCustomerManager().createCustomer(customer);
                    log.debug("created {}",customer);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException ex) {
                    log.error("Cannot add customer", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getCustomerManager().deleteCustomer(getCustomerManager().getCustomerByID(id));
                    log.debug("deleted customer {}",id);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException ex) {
                    log.error("Cannot delete customer", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            case "/showUpdate":
                Long id = Long.valueOf(request.getParameter("id"));
                Customer customer=getCustomerManager().getCustomerByID(id);
                request.setAttribute("name", customer.getName());
                request.setAttribute("surname", customer.getSurname());
                request.setAttribute("address", customer.getAddress());
                request.setAttribute("identityCard", customer.getIdentityCard());
                request.setAttribute("phoneNumber", customer.getPhoneNumber());
                request.setAttribute("id",id);
                showCustomerList(request, response);
                return;
            case "/update":
                try {
                    id = Long.parseLong(request.getParameter("id"));
                    name = request.getParameter("name");
                    surname = request.getParameter("surname");
                    address = request.getParameter("address");
                    identityCard = request.getParameter("identityCard");
                    phoneNumber = request.getParameter("phoneNumber");
                    if ( ( (address==null || address.length()==0) && (phoneNumber==null || phoneNumber.length()==0) )
                            || name == null || name.length() == 0
                            || surname == null || surname.length() == 0
                            || identityCard == null || identityCard.length() == 0) {
                        request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty alebo aspon jeden kontaktny !");
                        showCustomerList(request, response);
                        return;
                    }
                    try {
                        customer = getCustomerManager().getCustomerByID(id);
                        customer.setName(name);
                        customer.setSurname(surname);
                        customer.setAddress(address);
                        customer.setIdentityCard(identityCard);
                        customer.setPhoneNumber(phoneNumber);
                        getCustomerManager().updateCustomer(customer);
                        log.debug("updated {}",customer);
                        response.sendRedirect(request.getContextPath()+URL_MAPPING);
                        return;
                    } catch (ServiceFailureException ex) {
                        log.error("Cannot add customer", ex);
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                        return;
                    }
                } catch (ServiceFailureException ex) {
                    log.error("Cannot update customer", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private CustomerManager getCustomerManager() {
        return (CustomerManager) getServletContext().getAttribute("customerManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showCustomerList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("customers", getCustomerManager().getAllCustomers());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show customer", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
