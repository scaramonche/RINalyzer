package de.mpg.mpi_inf.bioinf.rinalyzer2.internal.centrality;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import de.mpg.mpi_inf.bioinf.rinalyzer2.internal.utilities.Messages;

/**
 * File filter for results files, i.e. files with an extension ".centstats".
 * 
 * @author Nadezhda Doncheva
 */
public class ResultsFileFilter extends FileFilter {

	public ResultsFileFilter() {
		super();
	}

	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith(Messages.EXT_RINSTATS);
	}

	@Override
	public String getDescription() {
		return Messages.EXT_RINSTATSNAME;
	}
}
