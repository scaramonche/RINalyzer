package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

public class RINStressLayout extends AbstractLayoutAlgorithm {

	private UndoSupport undo;
	private String name;
	private CyServiceRegistrar context;
	// private BundleContext context;

	public RINStressLayout(CyServiceRegistrar bc, String computerName, String humanName, UndoSupport undoSupport) {
		super(computerName, humanName, undoSupport);
		name = humanName;
		undo = undoSupport;
		context = bc;
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, Object layoutContext,
			Set<View<CyNode>> nodesToLayOut, String layoutAttribute) {
		return new TaskIterator(new RINStressLayoutTask(context, name, networkView,
				(RINStressLayoutContext) layoutContext, nodesToLayOut, layoutAttribute, undo));
	}

	public RINStressLayoutContext createLayoutContext() {
		return new RINStressLayoutContext();
	}

}
