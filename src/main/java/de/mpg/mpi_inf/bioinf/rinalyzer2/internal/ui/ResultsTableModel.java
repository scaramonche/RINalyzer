package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Table model for centrality results display. Cells are not editable and centrality values are shown as
 * 5-digit long numbers. Columns can be sorted by the user.
 * 
 * @author Nadezhda Doncheva
 */
public class ResultsTableModel extends DefaultTableModel {

	/**
	 * Initialize a new instance of the <code>ResultsTableModel</code>.
	 */
	public ResultsTableModel(String[][] data, String[] columns) {
		super(data, columns);
		sortColumnStatus = new int[columns.length];
	}

	/**
	 * Each cell is not editable.
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * Get sort order at column <code>column</code>. If 0 not sorted, if 1 ascending and if 2 descending.
	 * 
	 * @param column
	 *            Column index.
	 * @return Sort order status.
	 */
	public int getSortColumnDesc(int column) {
		return sortColumnStatus[column];
	}

	/**
	 * Sort by column with index <code>col</code>.
	 * 
	 * @param col
	 *            Index of column to be sorted.
	 */
	@SuppressWarnings("unchecked")
	public void sortByColumn(final int col) {
		if (col != -1 && col < columnIdentifiers.size()) {
			for (int i = 0; i < sortColumnStatus.length; i++) {
				if (i != col) {
					sortColumnStatus[i] = 0;
				} else {
					if (sortColumnStatus[i] == 1) {
						sortColumnStatus[i] = 2;
					} else {
						sortColumnStatus[i] = 1;
					}
				}
			}
			//Collections.sort(dataVector, new Comparator<Vector<String>>() {
			//	public int compare(final Vector<String> v1, final Vector<String> v2) {
			//		int cmp = v1.get(col).compareTo(v2.get(col));
			//		if (sortColumnStatus[col] == 2) {
			//			cmp *= -1;
			//		}
			//		return cmp;
			//	}
			//});
			// sortColumnStatus[col] ^= true;
		}
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -7512166354443042480L;

	/**
	 * Array with integers for the sort order of each column, if <code>0</code> not sorted, if 1 sorted
	 * ascending, and 2 descending.
	 */
	protected int[] sortColumnStatus;
}
