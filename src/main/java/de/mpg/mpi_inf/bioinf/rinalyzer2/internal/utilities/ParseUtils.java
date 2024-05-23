package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class ParseUtils {

	private static Logger logger = LoggerFactory
			.getLogger(de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.ParseUtils.class);

	/**
	 * Try to parse an XML file from the PDB.
	 * 
	 * @param file
	 *            File with mapping information.
	 * @return Map of successfully matched entries.
	 */
	public static Map<String, String> parseXMLFile(File mappingFile) {
		Map<String, String> resMapping = new HashMap<String, String>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(mappingFile);
			final NodeList elements = doc.getElementsByTagName("eqr");
			for (int i = 0; i < elements.getLength(); i++) {
				Element el = (Element) elements.item(i);
				final String res1 = el.getAttribute("chain1") + ":" + el.getAttribute("pdbres1");
				final String res2 = el.getAttribute("chain2") + ":" + el.getAttribute("pdbres2");
				resMapping.put(res1, res2);
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.warn(Messages.LOG_PARSINGFAILED + mappingFile.getAbsolutePath());
		}
		return resMapping;

	}

	// TODO: [Improve][Comparison] Refine parsing of fasta files
	/**
	 * Try to parse an afasta file with the format:
	 * 
	 * >1PTA, chain A/36-362
	 * 
	 * ...................................RINTVRGPITISEAGFTLTHEHICG
	 * SSAGFLRAWPEFFGSRKALAEKAVRGLRRARAAGVRTIVDVSTFDIGRDVSLLAEVSRAA
	 * DVHIVAATGLWFDPPLSMRLRSVEELTQFFLREIQYGIEDTGIRAGIIKVATTGKATPFQ
	 * ELVLKAAARASLATGVPVTTHTAASQRDGEQQAAIFESEGLSPSRVCIGHSDDTDDLSYL
	 * TALAARGYLIGLDHIPHSAIGLEDNASASALLGIRSWQTRALLIKALIDQGYMKQILVSN
	 * DWLFGFSSYVTNIMDVMDRVNPDGMAFIPLRVIPFLREKGVPQETLAGITVTNPARFLSP TL...
	 * 
	 * >1PSC, chain A/1-365
	 * 
	 * MQTRRVVLKSAAAAGTLLGGLAGCASVAGSIGTGDRINTVRGPITISEAGFTLTHEHICG
	 * SSAGFLRAWPEFFGSRKALAEKAVRGLRRARAAGVRTIVDVSTFDIGRDVSLLAEVSRAA
	 * DVHIVAATGLWFDPPLSMRLRSVEELTQFFLREIQYGIEDTGIRAGIIKVATTGKATPFQ
	 * ELVLKAAARASLATGVPVTTHTAASQRDGEQQAAIFESEGLSPSRVCIGHSDDTDDLSYL
	 * TALAARGYLIGLDHIPHSAIGLEDNASASALLGIRSWQTRALLIKALIDQGYMKQILVSN
	 * DWLFGFSSYVTNIMDVMDRVNPDGMAFIPLRVIPFLREKGVPQETLAGITVTNPARFLSP TLRAS
	 * 
	 * 
	 * @param file
	 *            File with mapping information.
	 * @return Map of successfully matched entries.
	 */
	public static Map<String, String> parseFASTAFile(File mappingFile) {
		Map<String, String> resMapping = new HashMap<String, String>();
		BufferedReader br = null;
		//
		List<String> headers = new ArrayList<String>();
		List<String> sequences = new ArrayList<String>();
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			String line = null;
			int counter = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(">")) {
					headers.add(line.substring(1));
					counter += 1;
					continue;
				}
				if (sequences.size() < counter) {
					sequences.add(line);
				} else {
					sequences.set(counter - 1, sequences.get(counter - 1) + line);
				}
			}
			br.close();
			if (headers.size() == 2 && sequences.size() == 2
					&& sequences.get(0).length() == sequences.get(1).length()) {
				String chainID1 = null;
				String chainID2 = null;
				String[] headerParts1 = headers.get(0).split(",|/");
				String[] headerParts2 = headers.get(1).split(",|/");
				if (headerParts1.length == 3 && headerParts2.length == 3) {
					chainID1 = headerParts1[1].trim().split(" ")[1];
					chainID2 = headerParts2[1].trim().split(" ")[1];
					// int offset1 =
					// Integer.valueOf(headerParts1[2].trim().split("-")[0]) -
					// 1;
					// int offset2 =
					// Integer.valueOf(headerParts2[2].trim().split("-")[0]) -
					// 1;
				}
				headerParts1 = headers.get(0).split("-|:");
				headerParts2 = headers.get(1).split("-|:");
				if (headerParts1.length == 3 && headerParts2.length == 3) {
					chainID1 = headerParts1[1];
					chainID2 = headerParts2[1];
				}
				// alternative formats?
				if (chainID1 != null && chainID2 != null) {
					for (int i = 0; i < sequences.get(0).length(); i++) {
						String res1 = String.valueOf(sequences.get(0).charAt(i));
						String res2 = String.valueOf(sequences.get(1).charAt(i));
						if (res1.equals(".") || res1.equals("-") || res2.equals(".")
								|| res2.equals("-")) {
							// System.out.println("skipped residue " + i);
							continue;
						}
						resMapping.put(chainID1 + ":" + String.valueOf(i + 1), chainID2 + ":"
								+ String.valueOf(i + 1));
						// resMapping.put(chainID1 + ":" + String.valueOf(i + 1
						// - offset1),
						// chainID2 + ":" + String.valueOf(i + 1 - offset2));
					}
				}
			}
		} catch (Exception ex) {
			// ignore, parsing failed
			logger.warn(Messages.LOG_PARSINGFAILED + mappingFile.getAbsolutePath());
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
		return resMapping;
	}

	/**
	 * Try to parse a tab-separated one to one mapping:
	 * 
	 * 
	 * @param file
	 *            File with mapping information.
	 * @return Map of successfully matched entries.
	 */
	public static Map<String, String> parseTXTFile(File mappingFile) {
		Map<String, String> resMapping = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mappingFile));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] lineParts = line.split("\t");
				if (lineParts.length != 2) {
					continue;
				}
				resMapping.put(lineParts[0], lineParts[1]);
			}
			br.close();
		} catch (IOException ex) {
			// ignore
			logger.warn(Messages.LOG_PARSINGFAILED + mappingFile.getAbsolutePath());
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				// ignore
			}
		}
		return resMapping;
	}

}
