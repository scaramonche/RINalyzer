Requirements and Installation
-----------------------------

1.  Requirements:
	1.  Python: Ensure that Python 2.6.x or 2.7.x is installed. If not, download and install it from [here](https://www.python.org/downloads/).
	2.  Biopython: Ensure that Biopython 1.5 or above is installed. If not, download and install from [here](http://biopython.org/wiki/Download).
	3.  Reduce: Download the latest version from [here](http://kinemage.biochem.duke.edu/software/reduce.php).
	4.  Probe: Download the latest version from [here](http://kinemage.biochem.duke.edu/software/probe.php).
 
2.  Install Reduce and Probe:
	1.  Download the latest version of Reduce from [here](http://kinemage.biochem.duke.edu/software/reduce.php) and of Probe from [here](http://kinemage.biochem.duke.edu/software/probe.php).
	2.  Create symbolic links to the Reduce and Probe executables, e.g., in your user's `$USER_HOME/bin` directory, using the following shell commands:    

		    	cd $USER_HOME/bin
		    ln -s PROGRAMS_DIR/reduce.3.23.130521.linuxi386 reduce
		    	ln -s PROGRAMS_DIR/probe.2.16.130520.linuxi386 probe
    	
3.  Install RINerator:
	1.  Download the latest RINerator package from [here](../rinerator/RINerator_V0.5.1.tar.gz).
	2.  Set a directory for installation (`INST_DIR`).
	3.  Move the package RINerator_V0.5.X.tar.gz to INST_DIR and extract its content, e.g. on a linux machine, type shell command:	    
		`tar xvzf RINerator_V0.5.X.tar.gz`   
	4.  This creates RINerator_V0.5.X directory with:
		*   `README.TXT`: This instruction file
		*   `Source`: Python scripts
		*   `Test Example`: job files and results for testing installation
 
4.  Test installation:
	1.  Go to the test directory: INST_DIR/RINerator_V0.5.X/Test
	2.  Execute the shell commands:	    

		../Source/get_chains.py PDB/pdb1hiv.ent Results/ INPUT/chains_1hiv_A.txt
		../Source/get_conservation.py - name+score INPUT/pdb1hiv_ConsrufDB_align.fasta Results/pdb1hiv_h_cons.txt Results/pdb1hiv_h_cons.log Results/pdb1hiv_h_res.txt
		../Source/get_data.py Results/pdb1hiv_h_res.txt Results/pdb1hiv_h_data.na 1hiv

	3.  The following files should be generated in the Results directory:

		    	pdb1hiv_h.ent            PDB with hydrogen atoms
		    	pdb1hiv_h.probe          probe result file 
		    	pdb1hiv_h.sif            network file of residue interactions, can be loaded into Cytoscape
		    	pdb1hiv_h_cons.log       log file of conservation computation
		    	pdb1hiv_h_cons.txt       node attribute file with conservation scores
		    	pdb1hiv_h_data.na        node attribute file with external data retrieved from AA index
		    	pdb1hiv_h_nrint.ea       edge attribute file with number of interactions between residues
		    	pdb1hiv_h_intsc.ea       edge attribute file with score of interaction between residues
		    	pdb1hiv_h_res.txt        list of all residues

	4.  These files should be identical to precomputed files that are included in directory `INST_DIR/RINerator_V0.5.X/Test/OUTPUT/` (you can compare them with diff).
  

* * *

Examples
--------

### 1. Example  
To create a RIN from all chains (and ligands) of a protein in a PDB file, execute the shell command:  

	INST_DIR/RINerator_V0.5.X/Source/get_chains.py path_pdb path_output path_chains [path_ligands]

Parameters:  
*   `path_pdb`: PDB file or directory containing PDB files of the same protein structure
*   `path_output`: directory to save generated files
*   `path_chains`: file with chain identifiers separated by commas
    *   chain identifier might be any letter or an empty string
    *   examples: "A,B,I" or "" or "A,"
*   `path_ligands`: [optional] file with ligand identifiers separated by new lines
    *   each ligand is listed with a name, a chain identifier and a residue number separated by commas
    *   examples: "NOA,I,1" or "NOA,,1"
    
Test command from within the `RINerator_V0.5.X` directory:  

    	Source/get_chains.py Test/PDB/pdb1hiv.ent Test/Results/ Test/INPUT/chains_1hiv_all.txt Test/INPUT/ligands_1hiv_all.txt 

### 2.  Example  
To create a RIN from a specific selection of residues in a PDB file, execute the shell command:  
    
	INST_DIR/RINerator_V0.5.X/Source/get_segments.py path_pdb path_output path_segments [path_ligands]
    
Parameters:  
*   `path_pdb`: PDB file or directory containing PDB files of the same protein structure
*   `path_output`: directory to save generated files
*   `path_segments`: file with segment identifiers separated by new lines
    *   each segment consists of a model number, a chain identifier, a starting and ending residue number separated by commas
    *   starting and ending residue numbers should be one of these:
        *  number: residue number
        *  _: should be set both as starting and ending to consider all residues in the chain
        *  None: could be set as ending residue if there is only a starting residue available
    *   examples: "0,A,_,_" or "0,B,25,None" or "0,B,50,70"
*   `path_ligands`: [optional] file with ligand identifiers separated by new lines
    *   each ligand is listed with a name, a chain identifier and a residue number separated by commas
    *   examples: "NOA,I,1" or "NOA,,1"
    
      
Test command from within the RINerator_V0.5.X directory:  

    	Source/get_segments.py Test/PDB/pdb1hiv.ent Test/Results/ Test/INPUT/segments_1hiv.txt Test/INPUT/ligands_1hiv_all.txt

### 3.  Example  
To calculate conservation scores from a user-specified MSA file, execute the shell command:  

	INST_DIR/RINerator_V0.5.X/Source/get_conservation.py gap_format output_format path_alignment path_out path_log [path_id]

Parameters:  
*   `gap_format`: any symbol will be considered as a gap
*   `output_format`:
	*   `name+score`: identifier conservation_score
	*   `resid+score`: index conservation_score
	*   `score`: conservation_score
*   `path_alignment`: file with multiple sequence alignment in FASTA format
*   `path_out`: file with 1 or 2 columns (according to output_format) separated by space
*   `path_log`: log file
*   `path_id`: [optional] file with nodes identifiers (according to the NONgaps positions in the first sequence in alignment) in TXT format (should not contain empty lines)

Test command from within the `RINerator_V0.5.X` directory:  

    	Source/get_conservation.py - name+score Test/INPUT/pdb1hiv_ConsrufDB_align.fasta Test/Results/pdb1hiv_h_cons.txt Test/Results/pdb1hiv_h_cons.log Test/OUTPUT/pdb1hiv_h_res.txt

Note that we use the `Test/OUTPUT/pdb1hiv_h_res.txt` identifier file since it contains *only* the residue nodes from chain A in contrast to `Test/Results/pdb1hiv_h_res.txt`, which currently contains all residue nodes assuming the previous test commands were executed.
  
### 4.  Example  
To retrieve data from external resources, such as AAindex and ConSurfDB, execute the shell command:  

	INST_DIR/RINerator_V0.5.X/Source/get_data.py path_id path_output pdb_id [path_input]

Parameters:  
*   `path_id`: file with nodes identifiers (according to the RIN specifications) in TXT format
*   `path_output`: file to save retrieved data
*   `pdb_id`: PDB identifier  
    The script will try to retrieve the conservation scores from the ConSurfDB website, if no consurf.grades files are found in path_input.
*   `path_input`: [optional] path with additional input data, any of the following files will be considered:
	*   `consurf.grades`: conservation scores from ConSurfDB  
	    If more than one file, e.g., for each chain, the chain identifier should be included in the file name: consurf_A.grades
	*   `*.sif`: network file generated by RINerator  
	    This file will be used to calculate the number of unweighted residue interactions of each residue node.
	*   `*_nrint.ea`: edge attributes generated by RINerator  
	    This file will be used to calculate the number of atomic interactions of each residue node.
	*   `*_intsc.ea`: edge attributes generated by RINerator  
	    This file will be used to calculate the sum of atomic interaction scores for each residue node.

Test command from within the `RINerator_V0.5.X` directory:  
    
    	Source/get_data.py Test/Results/pdb1hiv_h_res.txt Test/Results/pdb1hiv_h_data.na 1hiv
    
So far, the following data are retrieved:
*   `ConSurfDB`: conservation scores from the ConSurfDB
*   `FAUJ880111`: AA positive charge
*   `FAUJ880112`: AA negative charge
*   `GRAR740102`: AA polarity
*   `GRAR740103`: AA volume
*   `ISOY800101`: AA normalized relative frequency of alpha-helix
*   `JANJ780101`: AA average accessible surface area
*   `JOND920101`: AA relative frequency of occurrence
*   `JOND920102`: AA relative mutability
*   `JURD980101`: AA modified Kyte-Doolittle hydrophobicity scale
*   `KLEP840101`: AA net charge
*   `ZIMJ680104`: AA isoelectric point
*   `cnt intsc`: sum of 'contact' interaction scores
*   `cnt nrint`: number of 'contact' edges (atomic interactions)
*   `cnt unweighted`: number of 'contact' edges (residue interactions)
*   `combi intsc`: sum of 'combi' interaction scores
*   `combi nrint`: number of 'combi' edges (atomic interactions)
*   `combi unweighted`: number of 'combi' edges (residue interactions)
*   `hbond intsc`: sum of 'hbond' interaction scores
*   `hbond nrint`: number of 'hbond' edges (atomic interactions)
*   `hbond unweighted`: number of 'hbond' edges (residue interactions)
*   `ovl intsc`: sum of 'overlap' interaction scores
*   `ovl nrint`: number of 'overlap' edges (atomic interactions)
*   `ovl unweighted`: number of 'overlap' edges (residue interactions)
  
  
### 5.  Example  
To create a RIN using a job file (as provided in the TEST directory or created for versions of RINalyzer below 0.5), execute the command:

	INST_DIR/RINerator_V0.5.X/Source/get_ncint.py Test/JOBS/test_job_*.py

Description of the job files and further instructions can be found [here](../rinerator/README_V0.5.1.TXT).

