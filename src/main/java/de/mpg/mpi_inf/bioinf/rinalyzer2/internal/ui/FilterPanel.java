package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.ui;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.CyUtils;

/**
 * Class adapted from the Cytoscape's class {@link FilterSettingPanel}. Each
 * centrality measure has its own instance of this panel.
 * 
 * @author Nadezhda Doncheva
 */
public class FilterPanel extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 7004059545246642712L;
	private JSlider slider;
	private JLabel label;
	// private DoubleRangeModel sliderModel;
	private CyNetwork network;
	private Map<CyNode, Double> centData;
	private Double[] minMeanMax;

	private CyServiceRegistrar context;

	/**
	 * Initializes a new instance of <code>FilterPanel</code>.
	 * 
	 * @param aFilter
	 *            A composite filter with a numeric filter as a child defined
	 *            for the considered centrality measure.
	 */
	public FilterPanel(CyServiceRegistrar bc, Map<CyNode, Double> aCentralityData, Double[] aMinMeanMax,
			CyNetwork aNetwork) {
		super();
		context = bc;
		centData = aCentralityData;
		minMeanMax = aMinMeanMax;
		network = aNetwork;
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form,
	 * the panel's slider and the &quot;Not&quot; check box.
	 */
	private void initComponents() {
		final int BS = UtilsUI.BORDER_SIZE;
		setLayout(new BorderLayout(BS, BS));
		label = new JLabel(UtilsUI.getThreeDigits(minMeanMax[1]));
		// System.out.println((int) (minMeanMax[0].doubleValue() * 1000) + "\t"
		// + (int) (minMeanMax[1].doubleValue() * 1000) + "\t"
		// + (int) (minMeanMax[2].doubleValue() * 1000));
		slider = new JSlider(JSlider.HORIZONTAL, (int) (minMeanMax[0].doubleValue() * 1000),
				(int) (minMeanMax[2].doubleValue() * 1000),
				(int) (minMeanMax[1].doubleValue() * 1000));
		slider.addChangeListener(this);
		this.add(label, BorderLayout.NORTH);
		this.add(slider, BorderLayout.CENTER);
		final JPanel borderLabels = new JPanel(new BorderLayout(BS, BS));
		borderLabels.add(new JLabel(UtilsUI.getThreeDigits(minMeanMax[0])), BorderLayout.WEST);
		borderLabels.add(new JLabel(UtilsUI.getThreeDigits(minMeanMax[2])), BorderLayout.EAST);
		this.add(borderLabels, BorderLayout.SOUTH);
		// spinner.setModel(new SpinnerNumberModel(minMeanMax[1].doubleValue(),
		// minMeanMax[0]
		// .doubleValue(), minMeanMax[2].doubleValue(), 1));
		// spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.000"));
		// spinner.addChangeListener(new ChangeListener() {
		// public void stateChanged(ChangeEvent e) {
		// JSpinner s = (JSpinner) e.getSource();
		// System.out.println("Spinner: " + s.getValue());
		// // slider.setValue((Integer) s.getValue());
		// }
		// });
		// this.add(spinner);

		// sliderModel = new DoubleRangeModel((int)Math.floor(minMeanMax[0]),
		// (int)Math.ceil(minMeanMax[2]),
		// minMeanMax[1]);
		// slider = new JSlider(sliderModel);
		// //slider.setMajorTickSpacing(10);
		// //slider.setMinorTickSpacing(1);
		// //slider.setPaintTicks(true);
		// slider.setPaintLabels(true);
		// slider.setBorder(BorderFactory.createEmptyBorder(UtilsUI.BORDER_SIZE,
		// UtilsUI.BORDER_SIZE,
		// UtilsUI.BORDER_SIZE, UtilsUI.BORDER_SIZE));
		// sliderModel.addChangeListener(this);
		// add(slider);
		// this.validate();
		// this.repaint();
	}

	public void stateChanged(ChangeEvent e) {
		double sValue = (double) ((JSlider) e.getSource()).getValue() / 1000;
		label.setText(UtilsUI.getThreeDigits(sValue));
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : centData.keySet()) {
			if (centData.get(node).doubleValue() >= sValue) {
				nodes.add(node);
			}
		}
		CyUtils.selectNodes(context, network, nodes);
	}

}
