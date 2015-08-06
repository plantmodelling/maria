
—
type: manual
title: Root system generation using Archisimple
author: Guillaume Lobet
—

# Root system generation using Archisimple

Archisimple (Pagès et al 2014) can be used to generate any number of root system of the fly. The procedure is the following:

- compile ArchiSimple (in the terminal)

		g++ -o ArchiSimp5Maria ArchiSimp5Maria.cpp

- edit the file batch_ArchiSimple.r to 
	- define the range of parameters you want to simulate
	- define were is the source folder
- run the script
- for each set of parameters, the script:
	- creates an input file with the simulation parameters
	- run ArchiSimple and creates an RSML file
	- Run RSML_reader.jar to create the output image
	- Run MARIA.jar to analyse the image


The the source folder must contain:

- ArchiSimp5Maria
- ArchiSimp5Maria.cpp
- MARIA.jar
- demand.cv
- param.txt
- sol.txt
- volrac.txt
- RSML_reader.jar
- outputs
	- images
	- rsml
	
	

