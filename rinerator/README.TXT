RINERATOR VERSION 0.3.4
22 April 2010




1.REQUIREMENTS

You need to have python and Biopython installed.
Both should be included in a standard linux distribution like Debian.
You also need Reduce in order to get the hydrogen atoms in the PDB coordinate 
files, and Probe to identify the non covalent interactions.
They are very easy to install and you can get them from the Richardson Lab Web 
Site (http://kinemage.biochem.duke.edu/subindex.php).

python
http://www.python.org/
tested on version 2.5.2

Biopython
http://biopython.org
tested on version 1.52 and 1.53

Reduce
http://kinemage.biochem.duke.edu/software/reduce.php
tested on version 3.13 and 3.14

Probe
http://kinemage.biochem.duke.edu/software/probe.php
tested on version 2.12




2. INSTALLATION

2.1.
Set a directory for installation (INST_DIR).

2.2.
Move the package RINerator_V0.3.4.tar.gz to INST_DIR and extract its content,
e.g. on a linux machine, type shell command:
tar xvzf RINerator_V0.3.4.tar.gz
This creates RINerator_V0.3.4 directory with:
README.TXT  This instruction file
Source      Python scripts
Test        Example job files and results for testing installation




3. TEST INSTALLATION

Go to the test directory:
INST_DIR/RINerator_V0.3.4/Test
There you find:
test_job_chains.py        test script to generate network file of residue 
                          interactions (.sif file)

Edit the file test_job_chains.py with text editor and set the paths of the reduce
and probe programs, e.g.:
reduce_cmd = 'REDUCE_INSTALLATION_DIRECTORY/reduce.3.14.080821.src/reduce_src/reduce'
probe_cmd = 'PROBE_INSTALLATION_DIRECTORY/probe.2.12.071128.scr/probe'

Run the job file to compute the network .sif files by typing the command:
INST_DIR/RINerator_V0.3.4/Source/get_ncint.py test_job_chains.py

The following files should be generated:

PDB/pdb1hiv_h.ent        PDB with hydrogen atoms
pdb1hiv_h.probe          probe result file 
pdb1hiv_h.sif            network file of residue interactions, can be loaded into Cytoscape
pdb1hiv_h_nrint.ea       attribute file with number of interactions between residues
pdb1hiv_h_intsc.ea       attribute file with score of interaction between residues

These files should be identical to precomputed files that are included in directory
INST_DIR/RINerator_V0.3.4/Test/Results/ (you can compare them with diff).




4. RUNNING RINERATOR

For generating RINs for single chains in a PDB file without models, go to 4.1; for defining
residue segments or specifying the model number, go to 4.2; for full selection control go to
4.3.

4.1 SELECTING CHAINS

In order to compute a network for all or for a single chain in a given PDB structure,
say xxx.pdb, you need to copy the test script test_job_chains.py and edit the lines:

reduce_cmd:       reduce command
probe_cmd:        probe command
pdb_path:         path of the input pdb
pdb_h_path:       path for the output pdb file with hydrogen atoms
probe_path:       path for the probe output file
pdb_filename:     file name of the input pdb
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
chains:           identifiers of the chains to be included in the network

If you want to select only chain A, write:
chains = ['A']

For chain A and B, write:
chains = ['A', 'B']

Run the modified script test_job_chains.py with the command:
INST_DIR/RINerator_V0.3.4/Source/get_ncint.py test_job_chains.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.



4.2. SELECTING RESIDUE SEGMENTS AND SPECIFYING MODEL NUMBER

In order to compute a network for a residue segment or specify a model for a given PDB structure,
say xxx.pdb, you need to copy the test script test_job_segments.py and edit it the lines:
reduce_cmd:       reduce command
probe_cmd:        probe command
pdb_path:         path of the input pdb
pdb_h_path:       path for the output pdb file with hydrogen atoms
probe_path:       path for the probe output file
pdb_filename:     file name of the input pdb
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
segment*:         a single segment selection
all_segments:     a list of segments to be included in the network

For example:
segment1 = [0, 'A', 20, 30]
segments = [segment1]
will select residues 20-30 in chain A.

The syntax for a segment is:
segment = [1, 2, 3, 4]

1: model number, always 0 for first model (in NMR PDB files) or when there are no models
   in the PDB file
2: string chain identifier
3: start of segment (could be integer, string or None)
4: end of segment

The start and end of segments are strings and could be any of these four values:
'_'                                  if the whole chain is selected
None                                 if the residue is not specified
residue_number                       the residue number, e.g. 20
'residue_number and insertion_code'  the residue number and an insertion code, e.g. '20A'

Select all residues in chain A:
segment1 = [0, 'A', '_', '_']

Select residue 25 in chain B:
segment2 = [0, 'B', 25, None]

Select residues 50-70 in chain B:
segment3 = [0, 'B', 50, 70]

Include all three segments in the network:
all_segments = [segment1, segment2, segment3]

After defining the selection, run the modified script test_job_segments.py with the command:
INST_DIR/RINerator_V0.3.4/Source/get_ncint.py test_job_segments.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.



4.3. ADVANCED SELECTION AND MULTIPLE RUNS

For advanced selection and multiple runs, you need to use these two files:
run_reduce_probe_job.py   script to generate pdb file with hydrogen bonds and probe file 
                          with interactions
test_job_all.py           script to generate network file of residue interactions (.sif file)


4.3.1. RUN REDUCE AND PROBE

In run_reduce_probe_job.py you need to set the names of several files and directories:
reduce_cmd:      the reduce command
probe_cmd:       the probe command
pdb_filename:    the pdb file name in
pdb_path:        the path of the input pdb
pdb_h_path:      the path for the output pdb file with hydrogen atoms
probe_path:      the path for the probe output file

You can run the script with the command:
INST_DIR/RINerator_V0.3.4/Source/get_ncint.py run_reduce_probe_job.py

Then, you get new PDB file xxx_h.ent stored in path defined in pdb_h_path,
and a probe result file xxx_h.probe stored in path defined in probe_path


4.3.2. GENERATE NETWORK SIF FILE

In test_job_all.py, edit the lines:
pdb_path:         path where to find the pdb file with hydrogen atom coordinates
pdb_filename:     name of pdb file with hydrogen atoms
probe_path:       the path for the probe output file
probe_filename:   name of probe file
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
component:        residue selection

You need to select the residues that are going to be included in the network of interactions.
For example:
component = ['1hiv', 'protein', [[0,'A',[' ',2,' '],[' ',57,' ']]]]
selects residues 2-57 in chain A

The syntax is:
component = [1, 2, [[3,4,[5,6,7],[8,9,10]], ...]]
1: string label
2: should be 'protein'
3: model number, always 0 for first model (in NMR PDB files) or when there are 
   no models in the PDB file
4: string with chain identifier
5,6,7: definition of start of a segment
5: if start residue is a standard amino acid residue then is space string(' ')
   This is the hetero-flag defined in Biopython, only if residue is hetero atom
   (HETATM in PDB), then this is 'H_' plus the name of the hetero-residue see
   Bio.PDB FAQ documentation in Biopython web site
6: residue number of start residue in segment
7: insertion code, in most case there is no insertion code and you should use
   space string (' ')
8,9,10: definition of end of segment, same syntax as start of a segment

If the start and end of the segment are the first and last residue in the chain
than use '_' as residue number. For example, select all residues in chain A:
component = ['1hiv', 'protein', [[0,'A',[' ','_',' '],[' ','_',' ']]]]

Additional segments can be defined, for segments 2-57 and 65-90 in chain A:
component = ['1hiv', 'protein', [[0,'A',[' ',2,' '],[' ',57,' ']],
[0,'A',[' ',65,' '],[' ',90,' ']]]]

If the segment is only one residue, then the end of segment is defined with
None. For example, residues 25 and 27 in chain B:
component = ['1hiv', 'protein', [[0,'B',[' ',25,' '],[None,None,None]],
[0,'B',[' ',27,' '],[None,None,None]]]]

Different chains can be identified, for example, to define segments 2-57 and
65-90 in chain A and 65-90 in chain B:
component = ['1hiv', 'protein', [[0,'A',[' ',2,' '],[' ', 57,' ']],
[0,'A',[' ',65,' '],[' ',90,' ']], [0,'B',[' ',65,' '],[' ',90,' ']]]]

Another example, residues 20-30 in chain A, plus 25,26,65-68 in chain B:
component = ['1hiv', 'protein', [[0,'A',[' ',20,' '],[' ',30,' ']],
[0,'B',[' ',25,' '],[None,None,None]], [0,'B',[' ',26,' '],[None,None,None]], 
[0,'B',[' ',65,' '],[' ',68,' ']]]]




Send comments and questions to:
Francisco Silva Domingues
Max-Planck-Institut Informatik
Campus E1 4
66123 Saarbruecken, Germany
email: doming _AT_ mpi-sb.mpg.de

