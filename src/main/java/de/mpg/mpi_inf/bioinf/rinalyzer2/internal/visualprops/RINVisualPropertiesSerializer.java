package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.visualprops;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.cytoscape.service.util.CyServiceRegistrar;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;
import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * Controller class providing static methods for loading and saving visual properties.
 * 
 * @author Nadezhda Doncheva
 */
public class RINVisualPropertiesSerializer {

	/**
	 * Get the default color mapping as stored in rinalyzer.props or the map in
	 * {@link Messages#colors} if the props file has not been loaded properly.
	 * 
	 * @return New instance of the map with color mappings.
	 */
	public static Map<String, Color> getColorMap() {
		if (colorMap != null) {
			return new HashMap<String, Color>(colorMap);
		}
		return new HashMap<String, Color>(Messages.colors);
	}

	/**
	 * Get the default edge constants as stored in rinalyzer.props or the constants in
	 * {@link Messages#sizeConst} if the props file has not been loaded properly.
	 * 
	 * @return New instance of the array with size constants ([bb edge width, edge width, edge
	 *         space, label size, node size]).
	 */
	public static int[] getSizeConst() {
		if (sizeConst != null) {
			return sizeConst.clone();
		}
		return Messages.sizeConst.clone();
	}

	/**
	 * Store current visual properties as default in the rinalyzer.props file in the user's
	 * cytoscape directory.
	 * 
	 * @param aColorMap
	 *            Current color mapping.
	 * @param aSizeConstArray
	 *            Array with all sizes constants [bb edge width, edge width, edge space, node label,
	 *            node size].
	 */
	public static void storeDefaultVisualProps(CyServiceRegistrar context, Component parent,
			Map<String, Color> aColorMap, int[] aSizeConstArray) {
		colorMap = aColorMap;
		sizeConst = aSizeConstArray;
		final Properties newProps = new Properties();
		for (final String key : aColorMap.keySet()) {
			final String newKey = "color." + key;
			final Color color = aColorMap.get(key);
			final String value = String.valueOf(color.getRed()) + ","
					+ String.valueOf(color.getGreen()) + "," + String.valueOf(color.getBlue());
			newProps.put(newKey, value);
		}
		newProps.put("bbedgewidth", String.valueOf(aSizeConstArray[0]));
		newProps.put("edgewidth", String.valueOf(aSizeConstArray[1]));
		newProps.put("edgespace", String.valueOf(aSizeConstArray[2]));
		newProps.put("labelsize", String.valueOf(aSizeConstArray[3]));
		newProps.put("nodesize", String.valueOf(aSizeConstArray[4]));
		newProps.put("edgedist", String.valueOf(aSizeConstArray[5]));
		try {
			newProps.store(new FileOutputStream(CyUtils.getUserPropsFileName(context)),
					Messages.HEADER_PROPS);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(parent, Messages.SM_PROPSFAIL, Messages.DT_ERROR,
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Load the visual properties. First try to load the rinalyzer.props file from the user's
	 * cytoscape directory and if it's not successful, load the props file from the jar file.
	 */
	public static void loadVisProps(CyServiceRegistrar context) {
		final Properties props = new Properties();
		try {
			String filename = CyUtils.getUserPropsFileName(context);
			if (filename != null) {
				final File propsFile = new File(filename);
				if (propsFile.exists() && propsFile.isFile()) {
					props.load(new FileInputStream(propsFile));
					extractProps(props);
					return;
				}
			}
		} catch (IOException ex) {
			// ignore, try to load props from jar file.
		}
		try {
			props.load(RINVisualPropertiesSerializer.class.getResourceAsStream("/rinalyzer.props"));
			extractProps(props);
		} catch (IOException e) {
			// ignore, load props from Messages
		}
	}

	/**
	 * Extract the color mappings and the edge constants from the <code>defaultProps</code>
	 * instance.
	 * 
	 * @param defaultProps
	 *            Properties instance as loaded from the props file.
	 */
	private static void extractProps(Properties defaultProps) {
		colorMap = new HashMap<String, Color>();
		sizeConst = new int[6];
		for (final Object key : defaultProps.keySet()) {
			final String prop = (String) key;
			final String value = defaultProps.getProperty(prop);
			if (prop.startsWith("color")) {
				final String[] props = prop.split("\\.");
				final String[] values = value.split("\\,");
				if (values.length == 3) {
					colorMap.put(props[1], new Color(Integer.valueOf(values[0]).intValue(), Integer
							.valueOf(values[1]).intValue(), Integer.valueOf(values[2]).intValue()));
				}
			} else if (prop.equals("bbedgewidth")) {
				sizeConst[0] = Integer.valueOf(value).intValue();
			} else if (prop.equals("edgewidth")) {
				sizeConst[1] = Integer.valueOf(value).intValue();
			} else if (prop.equals("edgespace")) {
				sizeConst[2] = Integer.valueOf(value).intValue();
			} else if (prop.equals("labelsize")) {
				sizeConst[3] = Integer.valueOf(value).intValue();
			} else if (prop.equals("nodesize")) {
				sizeConst[4] = Integer.valueOf(value).intValue();
			} else if (prop.equals("edgedist")) {
				sizeConst[5] = Integer.valueOf(value).intValue();
			}
		}
	}

	/**
	 * Color mappings for nodes, edges and background.
	 */
	private static Map<String, Color> colorMap = null;

	/**
	 * Edge constants, [backbone edge width, edge width, edge space, label size, node size].
	 */
	private static int[] sizeConst = null;

}
