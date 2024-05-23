package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChimUtils {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ChimUtils.class);

	public static boolean sendCommand(CyServiceRegistrar context, TaskObserver observer,
			String command) {
		TaskFactory sendCommandFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_SENDCOMMANDTASK);
		if (sendCommandFactory != null && sendCommandFactory.isReady()) {
			TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
					TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			tunables.put(Messages.SV_COMMANDTUNABLE, command);
			TaskManager<?, ?> tm = (TaskManager<?, ?>) CyUtils.getService(context,
					TaskManager.class);
			tm.execute(tunableSetter.createTaskIterator(sendCommandFactory.createTaskIterator(),
					tunables, observer), observer);
			return true;
		}
		return false;
	}

	// public static List<String> getSortedOpenModelChains(BundleContext context) {
	// List<String> models = new ArrayList<String>();
	// List<String> reply = (List<String>) sendChimeraCommand(context, Messages.CC_LISTMMOL);
	// if (reply != null) {
	// for (String line : reply) {
	// String modelName = line.substring(line.indexOf("name") + 5).trim();
	// String modelID = line.substring(line.indexOf("#"), line.indexOf("type")).trim();
	// if (modelName != null && modelID != null) {
	// models.add(modelID + " " + modelName);
	// List<String> reply2 = (List<String>) sendChimeraCommand(context,
	// Messages.CC_LISTMODELCHAINS + modelID);
	// if (reply2 != null && reply2.size() > 1) {
	// for (String line2 : reply2) {
	// String chainID = line2.substring(line2.lastIndexOf("chain")).trim();
	// if (chainID != null) {
	// models.add(modelID + " " + modelName + " " + chainID);
	// }
	// }
	// }
	// }
	// }
	// }
	// return models;
	// }

	public static String defineChimeraColor(CyServiceRegistrar context, Color aColor, String aName) {
		String colorName = aName;
		String colorDef = "";
		try {
			float[] rgbColorCodes = aColor.getRGBColorComponents(null);
			for (int i = 0; i < rgbColorCodes.length; i++) {
				colorDef += " " + rgbColorCodes[i];
				colorName += rgbColorCodes[i];
			}
		} catch (Exception e) {
			logger.warn("Could not define new color");
			return null;
		}
		if (colorDef.equals("")) {
			return null;
		}
		sendChimeraCommand(context, Messages.CC_COLORDEF + " " + colorName + " " + colorDef);
		return colorName;
	}

	// public static String getModelNumber(BundleContext context, String name) {
	// List<String> reply = (List<String>) sendChimeraCommand(context, Messages.CC_LISTMMOL);
	// if (reply != null) {
	// for (String line : reply) {
	// String modelName = line.substring(line.indexOf("name") + 5).trim();
	// String modelID = line.substring(line.indexOf("#"), line.indexOf("type")).trim();
	// if (modelName != null && name.contains(modelName)) {
	// return modelID;
	// }
	// }
	// }
	// return null;
	// }

	// TODO: [Improve][Chimera] What to do in case of multiple models associated with the same
	// network?
	public static boolean getCoordinates(CyServiceRegistrar context, TaskObserver taskObserver,
			CyNetwork network) {
		NetworkTaskFactory annotateFactory = (NetworkTaskFactory) CyUtils.getService(context,
				NetworkTaskFactory.class, Messages.SV_ANNOTATECOMMANDTASK);
		if (annotateFactory != null && annotateFactory.isReady(network)) {
			TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
					TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			ListMultipleSelection<String> resAttrTun = new ListMultipleSelection<String>(
					Messages.SV_RESCOORDINATES);
			if (network.getDefaultNodeTable().getColumn(Messages.SS_ATTR_NAME) == null) {
				resAttrTun = new ListMultipleSelection<String>(Messages.SV_RESCOORDINATES,
						Messages.SV_RESSS);
			}
			resAttrTun.setSelectedValues(resAttrTun.getPossibleValues());
			tunables.put(Messages.SV_TUNABLEANNOTATE, resAttrTun);
			SynchronousTaskManager taskManager = (SynchronousTaskManager) CyUtils.getService(
					context, SynchronousTaskManager.class);
			taskManager.execute(tunableSetter.createTaskIterator(
					annotateFactory.createTaskIterator(network), tunables, taskObserver),
					taskObserver);
			return true;
		} else if (network.getDefaultNodeTable().getColumn("resCoord.x") != null
				&& network.getDefaultNodeTable().getColumn("resCoord.y") != null
				&& network.getDefaultNodeTable().getColumn("resCoord.z") != null) {
			logger.warn(Messages.LOG_RINLAYOUTOLDCOORD);
			return true;
		}
		return false;
	}

	/*
	 * public static void getCoordinatesOld(BundleContext context, CyNetwork network) { // Check for
	 * attribute containing the residue ID CyTable nodeTable = network.getDefaultNodeTable();
	 * HashMap<CyNode, Set<String>> node2res = new HashMap<CyNode, Set<String>>(); Set<String>
	 * structKeys = new HashSet<String>(); for (String structKey : Messages.defaultStructureKeys) {
	 * if (nodeTable.getColumn(structKey) != null) { structKeys.add(structKey); } } if
	 * (structKeys.size() == 0) { logger.warn(Messages.LOG_CHIMERATTR); return;
	 * 
	 * } // get all PDB ids associated with the network (simple version) Set<String> pdbIDs = new
	 * HashSet<String>(); for (CyNode node : network.getNodeList()) { Set<String> resIDs = new
	 * HashSet<String>(); for (String structKey : structKeys) { if
	 * (!network.getRow(node).isSet(structKey)) { continue; } Class<?> colType =
	 * nodeTable.getColumn(structKey).getType(); if (colType == String.class) {
	 * resIDs.add(network.getRow(node).get(structKey, String.class)); } else if (colType ==
	 * List.class) { resIDs.addAll(network.getRow(node).getList(structKey, String.class)); } }
	 * node2res.put(node, resIDs); for (String resID : resIDs) { if (resID.contains("#")) {
	 * pdbIDs.add(resID.substring(0, resID.indexOf("#"))); } } } // create columns final String
	 * resAttr = "resCoord"; if (pdbIDs.size() > 0) { if (nodeTable.getColumn(resAttr + ".x") ==
	 * null) { nodeTable.createColumn(resAttr + ".x", Double.class, false); } if
	 * (nodeTable.getColumn(resAttr + ".y") == null) { nodeTable.createColumn(resAttr + ".y",
	 * Double.class, false); } if (nodeTable.getColumn(resAttr + ".z") == null) {
	 * nodeTable.createColumn(resAttr + ".z", Double.class, false); } }
	 * 
	 * // for each pdb ID, get the coordinates for (String pdbID : pdbIDs) { //
	 * System.out.println("Retrieve coordinates for model " + pdbID + "."); // get model number
	 * final String modelNumber = getModelNumber(context, pdbID); if (modelNumber == null) {
	 * System.out .println("Model " + pdbID +
	 * " does not seem to be open in Chimera. Needed for coordinates retrieval."); continue; } final
	 * String commandText = Messages.CC_GETCOORD + modelNumber + ";"; Map<String, Double[]>
	 * resCoords = new HashMap<String, Double[]>(); // get coordinates from Chimera List<String>
	 * reply = (List<String>) sendChimeraCommand(context, commandText); if (reply != null) { //
	 * parse coordinates from Chimera for (String inputLine : reply) { String[] lineParts =
	 * inputLine.split("\\s+"); if (lineParts.length != 5) { continue; }
	 * 
	 * String[] residueChimera = lineParts[1].split(":|@"); String residue = lineParts[1]; if
	 * (residueChimera.length == 3) { residue = residueChimera[1]; } final String atom =
	 * lineParts[1].split("@")[1]; Double[] coord = null; try { coord = new Double[3]; for (int i =
	 * 0; i < 3; i++) { coord[i] = new Double(lineParts[i + 2]); } if
	 * (!resCoords.containsKey(residue) || atom.equals("CA")) { resCoords.put(residue, coord); } }
	 * catch (NumberFormatException ex) { // no coordinates for this node, ignore //
	 * ex.printStackTrace(); } } } if (resCoords.size() > 0) { // save residue coordinates for
	 * (CyNode node : network.getNodeList()) { Set<String> chimeraIDs = node2res.get(node); for
	 * (String chimeraID : chimeraIDs) { if (!chimeraID.contains(pdbID)) { continue; } if
	 * (chimeraID.contains("#")) { chimeraID = chimeraID.split("#")[1]; } if
	 * (resCoords.containsKey(chimeraID)) { final Double[] coord = resCoords.get(chimeraID);
	 * network.getRow(node).set(resAttr + ".x", coord[0]); network.getRow(node).set(resAttr + ".y",
	 * coord[1]); network.getRow(node).set(resAttr + ".z", coord[2]); //
	 * System.out.println("saved coord for " + chimeraID); } }
	 * 
	 * } } } }
	 */

	public static void sendChimeraCommand(CyServiceRegistrar context, String command) {
		TaskFactory sendCommandFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_SENDCOMMANDTASK);
		if (sendCommandFactory != null && sendCommandFactory.isReady()) {
			TunableSetter tunableSetter = (TunableSetter) CyUtils.getService(context,
					TunableSetter.class);
			Map<String, Object> tunables = new HashMap<String, Object>();
			tunables.put(Messages.SV_COMMANDTUNABLE, command);
			TaskManager<?, ?> tm = (TaskManager<?, ?>) CyUtils.getService(context,
					TaskManager.class);
			tm.execute(tunableSetter.createTaskIterator(sendCommandFactory.createTaskIterator(),
					tunables));
		}
		//
		//
		// SendCommandThread commandThread = new SendCommandThread(context, command);
		// Object result = commandThread.getResults(List.class);
		// return result;
		// return null;
		// return new SendCommandThread().sendChimeraCommand(context, command);
	}

	public static boolean isChimeraReady(CyServiceRegistrar context) {
		TaskFactory createStrNetTaskFactory = (TaskFactory) CyUtils.getService(context,
				TaskFactory.class, Messages.SV_CREATERINCOMMANDTASK);
		if (createStrNetTaskFactory != null) {
			return createStrNetTaskFactory.isReady();
		}
		return false;

	}
}
