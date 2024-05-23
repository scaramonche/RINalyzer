package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.layout;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class RINLayoutContext implements TunableValidator {
	
	// Defines how strong the z distance weakens the vertex repulsion(1.0 max=nearly neutralizes it)
	@Tunable(description = "Influence of z coordinate distance on repulsion (Default 0.5)")
	public double zinfluence = 0.5;

	// Used to allow user scaling of optimal distance value
	@Tunable(description = "Optimal Distance Factor (Default 1.0)")
	public double optDistFactor = 1.0;

	// Default factor for attractive forces in same SSC
	@Tunable(description = "Strength factor for attraction in secondary structures (Default 3.0)")
	public double ssinAtt = 3.0;

	// Two factors that influence how strong vertices repel each other if they belong to the same
	// SSC or not, changeable via Tunables by user
	public double ssoutfac = 1.0; // strong repulsion

	@Tunable(description = "Inner SS Structures Repulsion Factor (Default 0.5)")
	public double ssinfac = 0.5; // weaker repulsion

	// distance factor camera to plane (* graph z range)
	@Tunable(description = "Camera distance factor")
	public double camDist = 2.0;

	// Glue subsequent vertices in same SSC as if connected by edge
	@Tunable(description = "Glue vertices in SSC even if not connected by edge")
	public boolean gluess = true;

	// Skip computation
	@Tunable(description = "Skip Computation (show chimera coordinates)")
	public boolean debugSwitch = false;

	// Are vertices drawn to the original position
	@Tunable(description = "Anchor vertices to chimera coordinates (Default true)")
	public boolean anchor = true;

	// Are CCs packed or just stacked depending on 3D layout
	@Tunable(description = "Pack connected components (avoid overlap)")
	public boolean pack = false;

	// Determines number of options in option dialog
	// @Tunable(description = "Expert mode")
	static boolean expertMode = false;
	
	public ValidationState getValidationState(Appendable errMsg) {
		return ValidationState.OK;
	}

}
