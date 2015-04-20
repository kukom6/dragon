package cz.muni.fi.pv168.dragon;

import java.util.Collection;

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
