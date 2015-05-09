package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.Lease;
import cz.muni.fi.pv168.dragon.LeaseManager;
import cz.muni.fi.pv168.dragon.ServiceFailureException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

/**
 * Created by Michal on 4.5.2015.
 */
public class LeaseTableModel extends AbstractTableModel{
    private LeaseManager leaseManager;
    private List<Lease> allLeases;
    private static final Object LOCK = new Object();

    private RefreshLeasesSwingWorker refreshLeasesSwingWorker;

    private class RefreshLeasesSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            synchronized (LOCK){
                allLeases = new ArrayList<>(leaseManager.getAllLeases());
            }
            return 1;
        }
    }

    private UpdateLeaseSwingWorker updateLeaseSwingWorker;

    private class UpdateLeaseSwingWorker extends SwingWorker<Integer,Void> {

        private Lease leaseToUpdate;
        public UpdateLeaseSwingWorker(Lease leaseToUpdate) {
            this.leaseToUpdate = leaseToUpdate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            leaseManager.updateLease(leaseToUpdate);
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

    public LeaseTableModel(LeaseManager leaseManager){
        this.leaseManager = leaseManager;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        synchronized (LOCK) {
            return allLeases.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Lease lease = allLeases.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return lease.getId();
            case 1:
                return lease.getPrice();
            case 2:
                return lease.getStartDate();
            case 3:
                return lease.getEndDate();
            case 4:
                return lease.getDragon().getName();
            case 5:
                return lease.getCustomer().getName();
            case 6:
                return lease.getReturnDate();
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
                return lang.getString("mainwindow_price");
            case 2:
                return lang.getString("mainwindow_startDate");
            case 3:
                return lang.getString("mainwindow_endDate");
            case 4:
                return lang.getString("mainwindow_dragonName");
            case 5:
                return lang.getString("mainwindow_customerName");
            case 6:
                return lang.getString("mainwindow_returnDate");
            default:
                throw new IllegalArgumentException("column");
        }
    }

    @Override
    public void fireTableDataChanged() {
        refreshLeasesSwingWorker = new RefreshLeasesSwingWorker();
        refreshLeasesSwingWorker.execute();
        super.fireTableDataChanged();
    }

    public Lease getLeaseAt(int row){
        synchronized (LOCK) {
            return allLeases.get(row);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Lease lease = getLeaseAt(rowIndex);
        switch(columnIndex){
            case 1:
                lease.setPrice((BigDecimal) aValue);

        }
        updateLeaseSwingWorker = new UpdateLeaseSwingWorker(lease);
        updateLeaseSwingWorker.execute();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex){
            case 1:
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }
}
