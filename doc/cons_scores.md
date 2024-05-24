Conservation Computation
------------------------

This method implements the reliable metric for quantifying the conservation of all amino acid positions in a multiple sequence alignment (MSA) of a given protein as proposed by Valdar [^1]. The conservation score is calculated based on three elements:

*   `t(x)`: symbol diversity at position $x$ (normalized to take account of sequence redundancy)
*   `r(x)`: stereochemical diversity at position $x$ (based on the BLOSUM62 matrix)
*   `g(x)`: fraction of gaps at position $x$

Each of the above elements has a different contribution to the final score as defined by three parameters: $\\alpha$, $\\beta$, and $\\gamma$: $$score(x) = (1-t(x))^{\\alpha}(1-r(x))^{\\beta}(1-g(x))^{\\gamma}$$ For a formal definition of each term look below. By default they are set to $1$, $0.5$, and $2$, respectively. For each amino acid position this method will provide a value in the range from $0$ to $1$, where $score(x) = 1$ means that the position $x$ is strictly conserved, while $score(x)=0$ means that the position $x$ is not conserved.

The conservation scores derived from a multiple sequence alignment of a protein and its homologues can usually be used to infer the importance of the residues for maintaining the structure and function of the protein [^1]. Recommended software for generating multiple sequence alignment is listed below:

*   [MAFFT](http://mafft.cbrc.jp/alignment/software/): MSA tool that uses Fast Fourier Transforms. Suitable for medium-large alignments.
*   [MUSCLE](https://www.ebi.ac.uk/Tools/msa/muscle/): accurate MSA tool, especially good with proteins. Suitable for medium alignments.

**Input data:**  
1.  File with a multiple sequence alignment of the protein  
    The alignment should be in a FASTA format. The first sequence in the alignment should be the sequence of the corresponding protein.  
    Allowed symbols for amino acids are:  
    `'A','R','N','D','C','Q','E','G','H','I','J','L','K','M','F','P','S','T','W','Y','V','B','Z','X'`
    Different gap formats are allowed as explained below.    
2.  File with node identifiers  
    Each line in the file is an identifier for an amino acid position (node) in the appropriate order (according to the order of positions without gaps in the first sequence from the alignment). This file should not contain empty lines and the number of lines should be equal to the number of protein nodes. It is required only when the output format is "name+score". If another format is used, an empty string should be used as a file name. 
3.  Output format  
	* `name+score`: space-separated file with the name of the node and the appropriate score. This format requires a file with node identifiers.
	* `resid+score`: space-separated file with a residue number (according to the order in the sequence) and appropriate score.
	* `score`: file with conservation scores (according to the order in the sequence).  
4.  Gap format  
    Symbol for the gap format, e.g., "-", ".".

**Output data:** 
1.  File with scores according to the output format.
2.  Log file with information about the success of the computation.

  

* * *

Conservation Definition
-----------------------

W. Valdar[^1] proposes a generalized formula for scoring conservation as a function of three variables: symbol diversity $t$, stereochemical diversity $r$, and gaps $g$. For a position $x$, it is defined as $$score(x) = (1-t(x))^{\\alpha}(1-r(x))^{\\beta}(1-g(x))^{\\gamma}$$ where $\\alpha$, $\\beta$, and $\\gamma$ weight the importance of each term.

*   The symbol diversity $t(x)$ is specified as Shannon's entropy: $$t(x)={\\lambda\_t}\\sum\_{a}^{K}{p\_a \\log\_2 p\_a}$$ where $K$ is the alphabet size, i.e., 20 amino acids and one gap symbol, and $p\_a$ is the probability of observing the $a$th symbol type. $\\lambda\_t$ scales the entropy to range $\[0,1\]$ and is defined as $$\\lambda\_t=\[\\log\_2(\\min(N,K))\]^{-1}$$ where $N$ the number of sequences in the alignment. By normalizing each $p\_a$, a sequence weighting can be incorporated into Shannon's entropy: $$p\_a=\\sum\_{i\\in \\left \\{ i:s\_i(x)=a \\right \\} }{\\omega\_i}$$ where $w\_i$ is the weight of the $i$th sequence and $s\_i(x)$ is the symbol type at position $x$ in that sequence. Ideally, the sum of all weights should be $1$. Thus, the weighting scheme of Henikoff and Henikoff is used: $$\\omega\_i=\\frac{1}{L}\\sum\_{x}^{L}\\frac{1}{k\_x n\_{x\_i}}$$ where $L$ is the length of the alignment, $k\_x$ is the number of symbol types present at the $x$th position and $n\_{x\_i}$ is the frequency of amino acid $i$ at position $x$.
    
*   The stereochemical diversity is denoted by $r(x)$ and uses a substitution matrix. Each amino acid $a$ is represented by a point $X\_a$ in 20-dimensional space such that $$X\_a=\\begin{bmatrix}M(a,a\_1) \\\\ M(a, a\_2) \\\\ ... \\\\M(a,a\_{20}) \\end{bmatrix}$$ where $a\_i$ is the the $i$th amino acid type and $M(a,b)$ is the similarity between amino acids $a$ and $b$ based on the BLOSUM62 matrix denoted by $m$: $$M(a,b)=\\frac{m(a,b)}{\\sqrt{m(a,a)m(b,b)}}$$ Then, for a position $x$, we define the consensus amino acid type as point $\\overline{X}(x)$: $$\\overline{X}(x)=\\frac{1}{k\_x}\\sum\_{a}^{k\_x}X\_a$$ where $k\_x$ is the number of amino acide types present at position $x$. Finally, the stereochemical diversity is calculated as the average distance of observed amino acids from the consensus point: $$r(x)=\\lambda\_r\\frac{1}{k\_x}\\sum\_{a}^{k\_x}|\\overline{X}(x)-X\_a|$$ where the scalar $\\lambda\_r=\\left\[\\sqrt{20(max(M)-min(M))^2}\\right\]^{-1}$ ensures $r=1$.
    
*   The gap cost $g(x)$ is simply defined as the fraction of symbols in column $x$ that are gaps.
    

  

* * *

[^1]: Valdar, W (2002): _Scoring Residue Conservation_, Proteins, Vol. 48:227-241.
