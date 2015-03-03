package source;

import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class DragonManagerImpl implements DragonManager {
    @Override
    public void createDragon(Dragon dragon) {

    }

    @Override
    public Dragon getDragonByID(Long ID) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Dragon> getAllDragon() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Dragon> getDragonsByName(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Dragon> getDragonsByRace(String race) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Dragon> getDragonsByNumberOfHeads(int number) {
        throw new UnsupportedOperationException("not implemented");
    }

   @Override
    public void updateDragon(Dragon dragon) {
       throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void deleteCustomer(Dragon dragon) {
        throw new UnsupportedOperationException("not implemented");
    }
}
