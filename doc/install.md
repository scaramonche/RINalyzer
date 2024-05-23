Requirements and Installation for Cytoscape 3.x
-----------------------------------------------

1. Requirements:
	1. Java: Ensure that Java 6 or 7 is installed using this test [web page](http://www.java.com/en/download/help/testvm.xml). If Java is not installed, just update it or download and install it from [here](http://java.com/en/download/manual.jsp).
	2. Cytoscape: Download and install the latest Cytoscape version (3.1.0 and above) from the [Cytoscape download page](http://www.cytoscape.org/download.html).
	3. UCSF Chimera: Download and install the latest UCSF Chimera version (1.8 and above) from the [UCSF Chimera download page](http://www.cgl.ucsf.edu/chimera/download.html).
2. Install RINalyzer and structureViz app:
	1. Download the structureViz 2 app from the [Cytoscape App Store](http://apps.cytoscape.org/apps/structureViz2).
	2. Download the RINalyzer 2 app from the [Cytoscape App Store](https://apps.cytoscape.org/apps/rinalyzer).
	3. Start Cytoscape and go to the App Manager (Apps → App Manager).
	4. Click the Install from File... button and select the RINalyzer.jar file.
	5. Repeat the same action with the structureViz.jar file.
	6. To check if the installation was successful, go to Apps to find the RINalyzer and structureViz menu options. 
3.  Launch UCSF Chimera from within Cytoscape:
	1. Start Cytoscape
	2. If not using the default Chimera installation directory, set the path to the Chimera executable file in Apps → structureViz → Settings .... These settings are specific to each network in Cytoscape.
	3. Go to Apps → structureViz → Launch Chimera.
	4. Once Chimera has been started successfully, the last used path is saved in the session file. If no other path is specified in the structureViz Settings, this path is used for launching Chimera next time.

  

* * *

Requirements and Installation for Cytoscape 2.x
-----------------------------------------------

1.  Install RINalyzer plugin:
	1.  Ensure that Java 6 is installed using this test [web page](http://www.java.com/en/download/help/testvm.xml). If Java 6 is not installed, just update it or download and install it from [here](http://java.com/en/download/manual.jsp).
	2.  Download and install the latest Cytoscape version 2.8.0 from the [Cytoscape download page](http://www.cytoscape.org/download.php).
	3.  Download the file RINalyzer.jar and copy it into the plugins folder of Cytoscape, e.g.  
	    C:\\Program Files\\Cytoscape\_v2.8.0\\plugins\\ on a Windows machine; /Applications/Cytoscape\_v2.8.0/plugins/  
	    on a Macintosh; and $HOME/Cytoscape\_v2.8.0/plugins/ on a Linux machine.
	4.  Now start Cytoscape and find the RINalyzer menu options in the Plugins menu.
2.  Enable RINalyzer features requiring UCSF Chimera:
	1.  Download and install the latest Chimera version 1.4 from the [UCSF Chimera download page](http://www.cgl.ucsf.edu/chimera/download.html).
	2.  Configure the path to the Chimera application in Cytoscape:
		*   Go to the Cytoscape Preferences Editor (Edit → Preferences → Properties).
		*   Click the Add button and enter the name of the property: Chimera.chimeraPath
		*   Click OK and enter the path to the Chimera application. On a Linux machine this could be $HOME/chimera/bin, on a Windows machine:  
		    C:\\Program Files\\Chimera\\bin, and on a Macintosh: /Applications/ Chimera.app/Contents/MacOS.
		*   Save the new preferences by clicking the option Make Current Cytoscape Properties Default at the bottom of the dialog.	
	3.  Now PDB files can be loaded in Chimera through the RINalyzer menu options.
3.  Take the first steps toward using RINalyzer [here](./tutorials.md).

  
