RINERATOR VERSION 0.5.1
10 October 2014


UPDATES V0.5.1

- Computation of conservation scores
- Retrieval of external data
- Simpler format for creating RINs

UPDATES V0.4

- Ligand names are in the same format as residues.
- A function for calling Probe without Reduce is available.
- The chain identifier may be an empty string.
- Small overlaps detected by Probe are ignored.
- The format of edge attribute files is compatible with Cy 3.
- A list of residues is included in the output.
- Simple selection of ligands is enabled.
- An example job for ligands and for multiple pdbs is available.



1.REQUIREMENTS

You need to have python and Biopython installed.
Both should be included in a standard linux distribution like Debian.
You also need Reduce in order to get the hydrogen atoms in the PDB coordinate 
files, and Probe to identify the non covalent interactions.
They are very easy to install and you can get them from the Richardson Lab Web 
Site (http://kinemage.biochem.duke.edu/subindex.php).

python
http://www.python.org/
tested on version 2.6

Biopython
http://biopython.org
tested on version 1.54

Reduce
http://kinemage.biochem.duke.edu/software/reduce.php
tested on version 3.23

Probe
http://kinemage.biochem.duke.edu/software/probe.php
tested on version 2.16




2. INSTALLATION

2.1.
Set a directory for installation (INST_DIR).

2.2.
Move the package RINerator_V0.5.1.tar.gz to INST_DIR and extract its content,
e.g. on a linux machine, type shell command:
tar xvzf RINerator_V0.5.1.tar.gz
This creates RINerator_V0.5.1 directory with:
README.TXT  This instruction file
Source      Python scripts
Test        Example job files and results for testing installation

2.3.
Create symbolic links to the Reduce and Probe executables, e.g., in your 
user's $USER_HOME/bin directory, using the following shell commands:
cd $USER_HOME/bin
ln -s PROGRAMS_DIR/reduce.3.23.130521.linuxi386 reduce
ln -s PROGRAMS_DIR/probe.2.16.130520.linuxi386 probe


3. TEST INSTALLATION

Go to the test directory:
INST_DIR/RINerator_V0.5.1/Test

Run the job file to compute the network .sif files by typing the command:
INST_DIR/RINerator_V0.5.1/Source/get_chains.py PDB/pdb1hiv.ent Results/ INPUT/chains_1hiv_A.txt

The following files should be generated in the directory Results:

pdb1hiv_h.ent            PDB with hydrogen atoms
pdb1hiv_h.probe          probe result file 
pdb1hiv_h.sif            network file of residue interactions, can be loaded into Cytoscape
pdb1hiv_h_nrint.ea       attribute file with number of interactions between residues
pdb1hiv_h_intsc.ea       attribute file with score of interaction between residues

These files should be identical to precomputed files that are included in directory
INST_DIR/RINerator_V0.5.1/Test/OUTPUT/ (you can compare them with diff).




4. RUNNING RINERATOR V0.5.1

4.1.
To create a RIN from all chains (and ligands) of a protein in a PDB file, execute the shell 
command:
INST_DIR/RINerator_V0.5.1/Source/get_chains.py path_pdb path_output path_chains [path_ligands]

Parameters:

path_pdb        PDB file or directory containing PDB files of the same protein structure
path_output     directory to save generated files
path_chains     file with chain identifiers separated by commas
                chain identifier might be any letter or an empty string
                examples: "A,B,I" or "" or "A,"
path_ligands    [optional] file with ligand identifiers separated by new lines
                each ligand is listed with a name, a chain identifier and a residue number 
                separated by commas
                examples: "NOA,I,1" or "NOA,,1"

Test command from within the RINerator_V0.5.1/Test directory: 
../Source/get_chains.py PDB/pdb1hiv.ent Results/ INPUT/chains_1hiv_all.txt INPUT/ligands_1hiv_all.txt 


4.2.
To create a RIN from a specific selection of residues in a PDB file, execute the shell command:

INST_DIR/RINerator_V0.5.1/Source/get_segments.py path_pdb path_output path_segments [path_ligands]

Parameters:

path_pdb        PDB file or directory containing PDB files of the same protein structure
path_output     directory to save generated files
path_segments   file with segment identifiers separated by new lines; each segment consists of a 
                model number, a chain identifier, a starting and ending residue number separated 
                by commas; starting and ending residue numbers should be one of these:
                  "number"  residue number
                  "_"       any residue, e.g., if set as starting and ending residue number, all 
                            residues in the chain are considered
                  "None"    no residue, if set as ending residue, only the starting residue is 
                            considered
                examples: "0,A,_,_" or "0,B,25,None" or "0,B,50,70"
path_ligands    [optional] file with ligand identifiers separated by new lines; each ligand is 
                listed with a name, a chain identifier and a residue number separated by commas
                examples: "NOA,I,1" or "NOA,,1"
   
Test command from within the RINerator_V0.5.1/Test directory: 
../Source/get_segments.py PDB/pdb1hiv.ent Results/ INPUT/segments_1hiv.txt INPUT/ligands_1hiv_all.txt


4.3.
To calculate conservation scores from a user-specified MSA file, execute the shell command:
INST_DIR/RINerator_V0.5.1/Source/get_conservation.py gap_format output_format path_alignment path_out path_log [path_id]

Parameters:

gap_format      any symbol will be considered as a gap, e.g. "-" or "."
output_format   "name+score"   identifier conservation_score
                "resid+score"  index conservation_score
                "score"        conservation_score
path_alignment  file with multiple sequence alignment in FASTA format
path_out        file with 1 or 2 columns (according to output_format) separated by space
path_log        log file
path_id         [optional] file with nodes identifiers (according to the NONgaps positions in the 
                first sequence in alignment) in TXT format (should not contain empty lines)

Test command from within the RINerator_V0.5.1/Test directory:
../Source/get_conservation.py - name+score INPUT/pdb1hiv_ConsrufDB_align.fasta Results/pdb1hiv_h_cons.txt Results/pdb1hiv_h_cons.log OUTPUT/pdb1hiv_h_res.txt

Note that we use the OUTPUT/pdb1hiv_h_res.txt identifier file since it contains *only* the residue 
nodes from chain A in contrast to Results/pdb1hiv_h_res.txt, which currently contains all residue 
nodes assuming the previous test commands were executed.


4.4.
To retrieve data from external resources, such as AAindex and ConSurfDB, execute the shell command:
INST_DIR/RINerator_V0.5.1/Source/get_data.py path_id path_output pdb_id [path_input]

Parameters:

path_id         file with nodes identifiers (according to the RIN specifications) in TXT format
path_output     file to save retrieved data
pdb_id          PDB identifier (The script will try to retrieve the conservation scores from the 
                ConSurfDB website, if no consurf.grades files are found in path_input.)
path_input:     [optional] path with additional input data, any of the following files:
                  consurf.grades  conservation scores from ConSurfDB. If more than one file, e.g., 
                                  for each chain, the chain identifier should be included in the 
                                  file name: consurf_A.grades
                  *.sif           network file generated by RINerator. This file will be used to 
                                  calculate the number of unweighted residue interactions of each 
                                  residue node.
                  *_nrint.ea      edge attributes generated by RINerator. This file will be used 
                                  to calculate the number of atomic interactions of each residue 
                                  node.
                  *_intsc.ea      edge attributes generated by RINerator. This file will be used 
                                  to calculate the sum of atomic interaction scores for each 
                                  residue node.

Test command from within the RINerator_V0.5.1 directory: 
Source/get_data.py Test/Results/pdb1hiv_h_res.txt Test/Results/pdb1hiv_h_data.na 1hiv


5. RUNNING JOBS FILES (EARLIER VERSIONS)

RINerator_V04 and below worked with user-editable job_files. There are still available in the
INST_DIR/RINerator_V0.5.1/Test/JOBS directory and their usage is described in the following section.

For generating RINs for single chains in a PDB file without models, go to 5.1; for including 
ligands, go to 5.2; for defining residue segments or specifying the model number, go to 5.3; 
for full selection control go to 5.4; for multiple runs with the same PDB file, go to 5.5; for
multiple files of the same PDB structure, go to 5.6.

If the symbolic links to Reduce and Probe do not work or could not be created for some reason,
edit the test job file with text editor and set the paths of the reduce and probe programs, e.g.:
reduce_cmd = 'REDUCE_INSTALLATION_DIRECTORY/reduce.3.23.130521.linuxi386'
probe_cmd = 'PROBE_INSTALLATION_DIRECTORY/probe.2.16.130520.linuxi386'


5.1 SELECTING CHAINS

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
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py test_job_chains.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues
sif_file_name_res.txt       list of all residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.


5.2 SELECTING CHAINS AND LIGANDS

In order to compute a network for all or for a single chain as well as ligands in a given 
PDB structure, say xxx.pdb, you need to copy the test script test_job_chains_ligands.py and 
edit the lines:

reduce_cmd:       reduce command
probe_cmd:        probe command
pdb_path:         path of the input pdb
pdb_h_path:       path for the output pdb file with hydrogen atoms
probe_path:       path for the probe output file
pdb_filename:     file name of the input pdb
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
chains:           identifiers of the chains to be included in the network
ligand*:          a single ligand
ligands:          a list of ligands to be included in the network

If you want to select only chain A, write:
chains = ['A']

For chain A and B, write:
chains = ['A', 'B']

For example:
ligand1 = ['NOA', 'I', 1]
all_ligands = [ligand1]
will select ligand NOA in chain I with residue number 1.

The syntax for a ligand is:
ligand = [1,2,3]

1: string ligand name
2: string chain identifier
3: start of ligand (could be integer, string or None)

The start of a ligand could be any of these three values:
'_'                                  if the ligand contains several residues and all are to be selected
None                                 if the residue number is not specified
residue_number                       the residue number, e.g. 1

Run the modified script test_job_chains.py with the command:
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py test_job_chains_ligands.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues
sif_file_name_res.txt       list of all residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.


5.3. SELECTING RESIDUE SEGMENTS AND SPECIFYING MODEL NUMBER

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
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py test_job_segments.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues
sif_file_name_res.txt       list of all residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.

5.4. ADVANCED SELECTION

In order to compute a network for an arbitrary selection of residues and ligands for a given 
PDB structure, say xxx.pdb, you need to copy the test script test_job_ligand.py and edit it 
the lines:
reduce_cmd:       reduce command
probe_cmd:        probe command
pdb_path:         path of the input pdb
pdb_h_path:       path for the output pdb file with hydrogen atoms
probe_path:       path for the probe output file
pdb_filename:     file name of the input pdb
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
component*:       a single selection
components:       a list of selections to be included in the network

For example:
component1 = ['1hiv', 'protein', [[0,'A',[' ','_',' '],[' ','_',' ']], 
[0,'B',[' ','_',' '],[' ','_',' ']], [0,'I',[' ','_',' '],[' ','_',' ']]]]
will select all residues in chain A and chain B.

component2 = ['HOH', 'water', [[0,'A',['W',302,' '],['W',390,' ']], 
[0,'B',['W',304,' '],['W',389,' ']], [0,'I',['W',301,' '],['W',339,' ']]]]
will select all water molecules in chain A and chain B.

component3 = ['NOA', 'ligand', [[0,'I',['H_NOA',1,' '],[None,None,None]]]]
will select the ligand NOA in chain I with residue number 1.

components = [component1, component2, component3]
will include all of the above selections.

For more examples, see next section.

The syntax for each component is:
component = [1, 2, [[3,4,[5,6,7],[8,9,10]], ...]]
1: string label
2: should be either 'protein' or 'water' or 'ligand'
3: model number, always 0 for first model (in NMR PDB files) or when there are 
   no models in the PDB file
4: string with chain identifier
5,6,7: definition of start of a segment
5: if start residue is a standard amino acid residue then is space string(' ')
   This is the hetero-flag defined in Biopython, only if residue is hetero atom
   (HETATM in PDB), then this is 'H_' plus the name of the hetero-residue or 'W' for water. 
   See Bio.PDB FAQ documentation in Biopython web site for more information.
6: residue number of start residue in segment
7: insertion code, in most case there is no insertion code and you should use
   space string (' ')
8,9,10: definition of end of segment, same syntax as start of a segment if present, 
   otherwise 'None'

After defining the selection, run the modified script test_job_ligand.py with the command:
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py test_job_ligand.py

The following files are generated:
pdb_h_path/xxx_h.ent        pdb file with hydrogen atoms
probe_path/xxx_h.probe      a probe result file
sif_file                    the network sif file
sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
sif_file_name_intsc.ea      edge attribute file with interaction score between residues
sif_file_name_res.txt       list of all residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.


5.5. ADVANCED SELECTION AND MULTIPLE RUNS

For advanced selection and multiple runs, you need to use these two files:
run_reduce_probe_job.py   script to generate pdb file with hydrogen bonds and probe file 
                          with interactions
test_job_all.py           script to generate network file of residue interactions (.sif file)


5.5.1. RUN REDUCE AND PROBE

In run_reduce_probe_job.py you need to set the names of several files and directories:
reduce_cmd:      the reduce command
probe_cmd:       the probe command
pdb_filename:    the pdb file name in
pdb_path:        the path of the input pdb
pdb_h_path:      the path for the output pdb file with hydrogen atoms
probe_path:      the path for the probe output file

You can run the script with the command:
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py run_reduce_probe_job.py

Then, you get new PDB file xxx_h.ent stored in path defined in pdb_h_path,
and a probe result file xxx_h.probe stored in path defined in probe_path


5.5.2. GENERATE NETWORK SIF FILE

In test_job_all.py, edit the lines:
pdb_path:         path where to find the pdb file with hydrogen atom coordinates
pdb_filename:     name of pdb file with hydrogen atoms
probe_path:       the path for the probe output file
probe_filename:   name of probe file
sif_file:         name of sif file
sel_id:           string identifier for the selection, can be any string
component*:       a single selection
components:       list of selections to be included in the network

You need to select the residues that are going to be included in the network of interactions.
For example:
component1 = ['1hiv', 'protein', [[0,'A',[' ',2,' '],[' ',57,' ']]]]
selects residues 2-57 in chain A, while
component2 = ['NOA', 'ligand', [[0,'I',['H_NOA',1,' '],[None,None,None]]]]
selects the ligand NOA in chain I.

All components to be included in the RIN are added to the list:
components = [component1, component2]

The syntax for a component is:
component = [1, 2, [[3,4,[5,6,7],[8,9,10]], ...]]
1: string label
2: should be either 'protein' or 'water' or 'ligand'
3: model number, always 0 for first model (in NMR PDB files) or when there are 
   no models in the PDB file
4: string with chain identifier
5,6,7: definition of start of a segment
5: if start residue is a standard amino acid residue then is space string(' ')
   This is the hetero-flag defined in Biopython, only if residue is hetero atom
   (HETATM in PDB), then this is 'H_' plus the name of the hetero-residue or 'W' for water. 
   See Bio.PDB FAQ documentation in Biopython web site for more information.
6: residue number of start residue in segment
7: insertion code, in most case there is no insertion code and you should use
   space string (' ')
8,9,10: definition of end of segment, same syntax as start of a segment if present, 
   otherwise 'None'

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

5.6. MULTIPLE FILES FOR THE SAME STRUCTURE

In order to compute a network for all chains and ligands for multiple files of the same 
PDB structure, you need to copy the test script test_job_directory.py and edit the lines:

reduce_cmd:   reduce command
probe_cmd:    probe command
pdb_path:     path of the input pdb
rin_path:     path for the output
chains:       identifiers of the chains to be included in the network
ligand*:      a single ligand
ligands:      a list of ligands to be included in the network

For the chains and ligands syntax, see section 4.2.

Run the modified script test_job_directory.py with the command:
INST_DIR/RINerator_V0.5.1/Source/get_ncint.py test_job_directory.py

The following files are generated for each pdb file xxx.pdb in pdb_path:
rin_path/xxx_h.ent                   pdb file with hydrogen atoms
rin_path/xxx_h.probe                 a probe result file
rin_path/sif_file                    the network sif file
rin_path/sif_file_name_nrint.ea      edge attribute file with number of interactions between residues
rin_path/sif_file_name_intsc.ea      edge attribute file with interaction score between residues
rin_path/sif_file_name_res.txt       list of all residues

The interaction score is defined as the sum of the interaction scores between all atoms of
two residues. See probe publication: Word, et al. (1999) J. Mol. Biol. 285, 1709-1731.




Send comments and questions to:
Nadezhda T. Doncheva
Max-Planck-Institut Informatik
Campus E1 4
66123 Saarbruecken, Germany
email: doncheva _AT_ mpi-inf.mpg.de

