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
        switch (action) {
            case "/add":
                //načtení POST parametrů z formuláře
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String name = request.getParameter("name");
                Date born;
                try {
                    born = sdf.parse(request.getParameter("born").replace("T", " "));
                }catch(ParseException ex){
                    throw new ServiceFailureException("Bad date form", ex);
                }
                String race = request.getParameter("race");
                int heads = Integer.parseInt(request.getParameter("heads"));
                int weight = Integer.parseInt(request.getParameter("weight"));

                //kontrola vyplnění hodnot
                if (name == null
                        || name.length() == 0
                        || race == null
                        || race.length() == 0
                        || born == null
                        || heads < 0
                        || weight < 0) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty spravne!");
                    showDragonsList(request, response);
                    return;
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    Dragon dragon = new Dragon();
                    dragon.setName(name);
                    dragon.setBorn(born);
                    dragon.setRace(race);
                    dragon.setNumberOfHeads(heads);
                    dragon.setWeight(weight);
                    getDragonManager().createDragon(dragon);
                    log.debug("created {}",dragon);
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
                SimpleDateFormat usdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String uname = request.getParameter("name");
                Date uborn;

                try {
                    uborn = usdf.parse(request.getParameter("born").replace("T", " "));
                }catch(ParseException ex){
                    throw new ServiceFailureException("Bad date form", ex);
                }
                String urace = request.getParameter("race");
                int uheads = Integer.parseInt(request.getParameter("heads"));
                int uweight = Integer.parseInt(request.getParameter("weight"));
                long id = Long.parseLong(request.getParameter("id"));


                //kontrola vyplnění hodnot
                if (uname == null
                        || uname.length() == 0
                        || urace == null
                        || urace.length() == 0
                        || uborn == null
                        || uheads < 0
                        || uweight < 0
                        || id <= 0) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty spravne!");
                    showDragonsList(request, response);
                    return;
                }
                //zpracování dat - vytvoření záznamu v databázi
                try {
                    Dragon dragon = new Dragon();
                    dragon.setId(id);
                    dragon.setName(uname);
                    dragon.setBorn(uborn);
                    dragon.setRace(urace);
                    dragon.setNumberOfHeads(uheads);
                    dragon.setWeight(uweight);
                    getDragonManager().updateDragon(dragon);
                    log.debug("created {}",dragon);
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