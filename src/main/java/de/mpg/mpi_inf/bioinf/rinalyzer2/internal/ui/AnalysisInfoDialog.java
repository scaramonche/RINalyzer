package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Dialog displaying the analysis settings defined by the user for the current computation.
 * 
 * @author Nadezhda Doncheva
 */
public class AnalysisInfoDialog extends JDialog {

	/**
	 * Initialize a new instance of <code>AnalysisInfoDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 * @param aData
	 *            Array with all analysis settings values as Strings.
	 */
	public AnalysisInfoDialog(Frame aOwner, String[] aData) {
		super(aOwner, Messages.DT_SETTINGSOVERVIEW, false);
		data = aData;
		init();
		setLocationRelativeTo(aOwner);
		setResizable(false);

	}

	/**
	 * Initializes the <code>AnalysisInfoDialog</code>.
	 */
	private void init() {
		final int BS = UtilsUI.BORDER_SIZE;
		final JPanel contentPane = new JPanel(new BorderLayout(BS, BS));
		contentPane.setBorder(BorderFactory.createEmptyBorder(BS, BS, BS, BS));

		final JEditorPane helpPane = new JEditorPane("text/html", getFormattedData());
		helpPane.setEditable(false);
		helpPane.setCaretPosition(0);
		final JScrollPane scrollPaneHelp = new JScrollPane(helpPane);
		scrollPaneHelp.setPreferredSize(new Dimension(390, 350));
		contentPane.add(scrollPaneHelp, BorderLayout.CENTER);

		setContentPane(contentPane);
		pack();
	}

	/**
	 * Creates an html representation of all analysis settings.
	 * 
	 * @return One string containing all analysis settings in a proper format for display.
	 */
	private String getFormattedData() {
		final StringBuilder sb = new StringBuilder(256);
		sb.append("<html><b>" + Messages.DI_CENTMEASURES + "</b><hr><table>");
		// centrality measures data
		sb.append("<tr><td width=260>" + Messages.DI_ANALYSIS_NETWORK + "</td>");
		sb.append("<td>" + data[0] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_COMPSPCENT + "</td>");
		sb.append("<td>" + data[1] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_COMPCFCENT + "</td>");
		sb.append("<td>" + data[2] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_COMPRWCENT + "</td>");
		sb.append("<td>" + data[3] + "</td></tr>");
		sb.append("</table><br>");
		// edge weights data
		sb.append("<b>" + Messages.DI_CENTANASET + "</b><hr><table>");
		sb.append("<tr><td width=260>" + Messages.DI_CHOOSEATTRIBUTE + "</td>");
		sb.append("<td>" + data[4] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_MULTIPLEEDGES + "</td>");
		sb.append("<td>" + data[5] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_REMOVE_NEGWEIGHT + "</td>");
		sb.append("<td>" + data[6] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_CONVERTWEIGHT + "</td>");
		sb.append("<td>" + data[7] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_DEFWEIGHTVALUE + "</td>");
		sb.append("<td>" + data[8] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_DEGREECUTOFF + "</td>");
		sb.append("<td>" + data[9] + "</td></tr>");
		sb.append("<tr><td width=260>" + Messages.DI_USECONNCOMP2 + "</td>");
		sb.append("<td>" + data[10] + "</td></tr>");
		sb.append("</table></html>");
		return sb.toString();
	}

	/**
	 * [Network title, compute sp, compute cf, compute rw, Weight attribute, Multiple edges, Negative weight,
	 * SimToDist, default weight, degree cutoff, exclude paths]
	 */
	private String[] data;

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = -3943792777880931598L;

}
