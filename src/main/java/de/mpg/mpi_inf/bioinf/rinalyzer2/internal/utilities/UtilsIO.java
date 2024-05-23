package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import java.awt.Component;
import java.io.Closeable;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Utility class providing helper methods for file manipulation.
 * 
 * @author Nadezhda Doncheva
 * 
 */
public class UtilsIO {

	/**
	 * Checks the input directory, i.e. if aFile exists, is a file, is not empty and is readable.
	 * 
	 * @param aFile
	 *            File to be checked.
	 * @return <code>true</code> if the input directory fulfills the criterion, and
	 *         <code>false</code> otherwise.
	 */
	public static boolean checkInputFile(Component parent, File aFile) {
		try {
			if (aFile.exists() && aFile.isFile() && aFile.canRead()) {
				return true;
			}
		} catch (SecurityException ex) {
			JOptionPane.showMessageDialog(parent, Messages.SM_IOERROROPEN, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	/**
	 * Verifies the given file does not exist and can therefore be created.
	 * <p>
	 * If the file exists, it will be overwritten. In such a case a confirmation dialog is displayed
	 * to the user.
	 * </p>
	 * 
	 * @param aFile
	 *            File to be checked.
	 * @param aParent
	 *            Parent component of the confirmation dialog. This parameter is used only if a
	 *            confirmation dialog is displayed.
	 * @return <code>true</code> if the specified file does not exist, or if it exists and the user
	 *         has confirmed it can be overwritten; <code>false</code> otherwise.
	 */
	public static boolean canSave(File aFile, Component aParent) {
		try {
			if (aFile.exists()) {
				return JOptionPane.showConfirmDialog(aParent, Messages.SM_FILEEXISTS,
						Messages.DI_FILEEXISTS, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			}
		} catch (SecurityException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Get a file to save data in it. This method also assures that the file can be created.
	 * 
	 * @param desktop
	 *            The Frame from which the save dialog is called.
	 * @param fileChooser
	 *            A file chooser.
	 * @param aExtension
	 *            File extension that should be added to the file names. Can be <code>null</code>.
	 * @return The selected file or null if this file can not be created or the user cancels the
	 *         dialog.
	 */
	public static File getFileToSave(Component parent, JFileChooser fileChooser,
			String aExtension) {
		final int saveIt = fileChooser.showSaveDialog(null);
		if (saveIt == JFileChooser.APPROVE_OPTION) {
			String fileName = fileChooser.getSelectedFile().getAbsolutePath();
			if (aExtension != null && !fileName.toLowerCase().endsWith(aExtension)) {
				fileName += aExtension;
			}
			try {
				final File file = new File(fileName);
				if (UtilsIO.canSave(file, null)) {
					return file;
				}
			} catch (NullPointerException ex) {
				JOptionPane.showMessageDialog(parent, Messages.SM_IOERRORSAVE, Messages.DT_ERROR,
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (saveIt == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(parent, Messages.SM_GUIERROR, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	/**
	 * Get a file to open.
	 * 
	 * @param desktop
	 *            The Frame from which the save dialog is called.
	 * @param fileChooser
	 *            A file chooser.
	 * @return The selected file or null if this file can not be created or the user cancels the
	 *         dialog.
	 */
	public static File getFileToOpen(Component parent, JFileChooser fileChooser) {
		int saveIt = fileChooser.showOpenDialog(null);
		if (saveIt == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
				if (checkInputFile(parent, file)) {
					return file;
				}
		} else if (saveIt == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(parent, Messages.SM_GUIERROR, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	/**
	 * Get a file to open.
	 * 
	 * @param desktop
	 *            The Frame from which the save dialog is called.
	 * @param fileChooser
	 *            A file chooser.
	 * @return Name of the file including the path or null if this file can not be created or the
	 *         user cancels the dialog.
	 */
	public static Set<File> getFilesToOpen(Component parent, JFileChooser fileChooser) {
		int saveIt = fileChooser.showOpenDialog(null);
		if (saveIt == JFileChooser.APPROVE_OPTION) {
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			final Set<File> filesToOpen = new HashSet<File>();
			for (final File file : selectedFiles) {
				if (checkInputFile(parent, file)) {
					filesToOpen.add(file);
				} else {
					return null;
				}
			}
			return filesToOpen;
		} else if (saveIt == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(parent, Messages.SM_GUIERROR, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}

	/**
	 * Silently closes the given stream.
	 * <p>
	 * This method does not throw an exception in any circumstances.
	 * </p>
	 * 
	 * @param aStream
	 *            Stream to be closed.
	 */
	public static void closeStream(Closeable aStream) {
		try {
			aStream.close();
		} catch (Exception ex) {
			// Unsuccessful attempt to close the stream; ignore
		}
	}
	
}
