package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * RINalyzer's about information.
 * 
 * @author Nadezhda Doncheva
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = -3247583150980630650L;

	/**
	 * Initializes a new instance of <code>AboutDialog</code>.
	 * 
	 * @param aOwner
	 *            The <code>Frame</code> from which this dialog is displayed.
	 */
	public AboutDialog(Frame aOwner) {
		super(aOwner, Messages.DT_ABOUT, true);
		init();
		setResizable(false);
		if (aOwner != null) {
			setLocationRelativeTo(aOwner);
		}
	}

	private void init() {
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(Color.WHITE);
		JLabel labContents = new JLabel(getImage());
		contentPane.add(labContents, BorderLayout.CENTER);
		setContentPane(contentPane);
		pack();
	}

	/**
	 * Loads an image stored in the given file.
	 * 
	 * @param aFileName
	 *            Name of file storing the image.
	 * @return The <code>ImageIcon</code> instance representing the loaded image, or an empty image
	 *         if the file specified does not exist.
	 */
	private ImageIcon getImage() {
		URL imageURL = AboutDialog.class.getResource("/aboutbox.png");
		return new ImageIcon(imageURL);
	}

}
