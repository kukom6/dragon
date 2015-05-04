package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Created by Michal on 30.4.2015.
 */
public class DragonTableModel extends AbstractTableModel{

    private DragonManager dragonManager;
    private List<Dragon> allDragons;
    private static final Object LOCK = new Object();

    private UpdateDragonSwingWorker updateDragonSwingWorker;

    private class UpdateDragonSwingWorker extends SwingWorker<Integer,Void> {

        private Dragon dragonToUpdate;
        public UpdateDragonSwingWorker(Dragon dragonToUpdate) {
            this.dragonToUpdate = dragonToUpdate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            dragonManager.updateDragon(dragonToUpdate);
            return 1;
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    fireTableDataChanged();
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete dragon", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    private RefreshDragonsSwingWorker refreshDragonsSwingWorker;

    private class RefreshDragonsSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            synchronized (LOCK){
                allDragons = new ArrayList<>(dragonManager.getAllDragons());
            }
            return 1;
        }
    }

    public DragonTableModel(DragonManager dragonManager){
        this.dragonManager = dragonManager;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        synchronized (LOCK) {
            return allDragons.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Dragon dragon = allDragons.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return dragon.getId();
            case 1:
                return dragon.getName();
            case 2:
                return dragon.getBorn();
            case 3:
                return dragon.getRace();
            case 4:
                return dragon.getNumberOfHeads();
            case 5:
                return dragon.getWeight();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public String getColumnName(int column) {
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        switch (column) {
            case 0:
                return lang.getString("mainwindow_id");
            case 1:
                return lang.getString("mainwindow_name");
            case 2:
                return lang.getString("mainwindow_born");
            case 3:
                return lang.getString("mainwindow_race");
            case 4:
                return lang.getString("mainwindow_heads");
            case 5:
                return lang.getString("mainwindow_weight");
            default:
                throw new IllegalArgumentException("column");
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Dragon dragon = getDragonAt(rowIndex);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        switch (columnIndex) {
            case 1:
                dragon.setName(aValue.toString());
                break;
            case 2:
                try {
                    dragon.setBorn(sdf.parse(aValue.toString()));
                } catch (ParseException ex){
                    throw new ServiceFailureException("Cannot parse date while updating dragon from JTable");
                }
                break;
            case 3:
                dragon.setRace(aValue.toString());
                break;
            case 4:
                try {
                    dragon.setNumberOfHeads(Integer.parseInt(aValue.toString()));
                } catch (NumberFormatException ex){
                    throw new ServiceFailureException("Cannot parse number of heads while updating dragon from JTable");
                }
                break;
            case 5:
                try {
                    dragon.setWeight(Integer.parseInt(aValue.toString()));
                } catch (NumberFormatException ex){
                    throw new ServiceFailureException("Cannot parse weight while updating dragon from JTable");
                }
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }

        updateDragonSwingWorker = new UpdateDragonSwingWorker(getDragonAt(rowIndex));
        updateDragonSwingWorker.execute();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    @Override
    public void fireTableDataChanged() {
        refreshDragonsSwingWorker = new RefreshDragonsSwingWorker();
        refreshDragonsSwingWorker.execute();
        super.fireTableDataChanged();
    }

    public Dragon getDragonAt(int row){
        return allDragons.get(row);
    }
}
