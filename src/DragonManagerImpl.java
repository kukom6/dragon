import java.util.Collection;

/**
 * Created by Matej on 23. 2. 2015.
 */
public class DragonManagerImpl implements DragonManager {
    @Override
    public void createDragon(Dragon dragon) {

    }

    @Override
    public Dragon getDragonById(Long ID) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Collection<Dragon> getAllDragons() {
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
    public void deleteDragon(Dragon dragon) {
        throw new UnsupportedOperationException("not implemented");
    }
}
