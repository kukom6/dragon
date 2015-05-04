package cz.muni.fi.pv168.web;

import cz.muni.fi.pv168.dragon.Dragon;
import cz.muni.fi.pv168.dragon.DragonManager;
import cz.muni.fi.pv168.dragon.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servlet for managing books.
 */
@WebServlet(DragonServlet.URL_MAPPING + "/*")
public class DragonServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/dragons";

    private final static Logger log = LoggerFactory.getLogger(DragonServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        showDragonsList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //aby fungovala čestina z formuláře
        request.setCharacterEncoding("utf-8");
        //akce podle přípony v URL
        String action = request.getPathInfo();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        switch (action) {
            case "/add":
                //načtení POST parametrů z formuláře

                Dragon dragonForCreate;
                try {
                    dragonForCreate = newDragon(
                            request.getParameter("name")
                            ,sdf.parse(request.getParameter("born").replace("T", " "))
                            ,request.getParameter("race")
                            ,Integer.parseInt(request.getParameter("heads"))
                            ,Integer.parseInt(request.getParameter("weight"))
                    );
                }catch(ParseException | NumberFormatException ex){
                    request.setAttribute("chyba", "Udaje su v zlom formate!");
                    showDragonsList(request, response);
                    return;
                }

                //kontrola vyplnění hodnot
                if (checkDragon(dragonForCreate)) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty spravne!");
                    showDragonsList(request, response);
                    return;
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    getDragonManager().createDragon(dragonForCreate);
                    log.debug("created {}",dragonForCreate);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot add book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getDragonManager().deleteDragon(getDragonManager().getDragonById(id));
                    log.debug("deleted book {}",id);
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot delete book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                //načtení POST parametrů z formuláře
                Dragon dragonForUpdate;
                try {
                    dragonForUpdate = newDragon(
                            request.getParameter("name")
                            ,sdf.parse(request.getParameter("born").replace("T", " "))
                            ,request.getParameter("race")
                            ,Integer.parseInt(request.getParameter("heads"))
                            ,Integer.parseInt(request.getParameter("weight"))
                    );
                }catch(ParseException | NumberFormatException ex){
                    request.setAttribute("chyba", "Udaju su v zlom formate!");
                    showDragonsList(request, response);
                    return;
                }
                Long id = Long.parseLong(request.getParameter("id"));
                //kontrola vyplnění hodnot
                if (checkDragon(dragonForUpdate) || id <= 0) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty spravne!");
                    showDragonsList(request, response);
                    return;
                }

                dragonForUpdate.setId(id);
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    getDragonManager().updateDragon(dragonForUpdate);
                    log.debug("created {}",dragonForUpdate);
                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    log.error("Cannot add book", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    private boolean checkDragon(Dragon dragon){
        return dragon.getName() == null
                || dragon.getName().length() == 0
                || dragon.getRace() == null
                || dragon.getRace().length() == 0
                || dragon.getBorn() == null
                || dragon.getNumberOfHeads() < 0
                || dragon.getWeight() < 0;
    }
    private Dragon newDragon(String name, Date bornDate, String race, int numOfHeads, int weight){
        Dragon dragon = new Dragon();
        dragon.setName(name);
        dragon.setBorn(bornDate);
        dragon.setRace(race);
        dragon.setNumberOfHeads(numOfHeads);
        dragon.setWeight(weight);
        return dragon;
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private DragonManager getDragonManager() {
        return (DragonManager) getServletContext().getAttribute("dragonManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showDragonsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("dragons", getDragonManager().getAllDragons());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show dragons", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}