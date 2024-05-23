package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Renderer for the header of the results table, that shows up and down arrows depending on the sort order.
 * 
 * @author Nadezhda Doncheva
 */
public class ResultsTableHeaderRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {

		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		final JTableHeader header = table.getTableHeader();
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		setHorizontalAlignment(SwingConstants.CENTER);
		setHorizontalTextPosition(SwingConstants.LEFT);

		final ResultsTableModel model = (ResultsTableModel) table.getModel();
		if (model.getSortColumnDesc(column) == 0) {
			setText(model.getColumnName(column));
		} else if (model.getSortColumnDesc(column) == 1) {
			setText(model.getColumnName(column) + "  \u2193");
		} else {
			setText(model.getColumnName(column) + "  \u2191");
		}
		return this;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 6692106079141925497L;
}
