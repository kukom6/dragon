import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public interface DragonManager {
    void createDragon(Dragon dragon);

    Dragon getDragonById(Long id);

    Collection<Dragon> getAllDragons();

    Collection<Dragon> getDragonsByName(String name);

    Collection<Dragon> getDragonsByRace(String race);

    Collection<Dragon> getDragonsByNumberOfHeads(int number);

    void updateDragon(Dragon dragon);

    void deleteDragon(Dragon dragon);
}
