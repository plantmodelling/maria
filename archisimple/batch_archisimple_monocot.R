# Guillaume Lobet - University of Liege
# ArchiSimple Batch

# The aim of this script is to run ArchiSimple in a batch mode in order to create
# any number of root systems, with any combinaison of parameters.
# The script works as follow:
#   - Looping over the parameter set (user defined)
#   - For each combinaison, create the param.txt file used by ArchiSimple
#   - Run ArchiSimple and generate the corresponding RSML file
#   - Once all the root systems are generated, create the images using RSML_reader.jar
#   - Once all the images are generated, analyse them using MARIAJ.jar




#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------


# Where is ArchiSimple
setwd("/Users/guillaumelobet/Desktop/archisimple/") # Where is the ArchiSimple folder?
eval = c(F)  # To know the number root system that will be generated, use c(F). To generate then, use c(F,T)
verbatim <- FALSE

# Range of parameters
P_nbMaxPrim_range           <-  c(1) # Number of primary axes. Put 40 to have a monocot, 1 to have a dicot
P_type_range                <- c(2:2) # Type of simuulation (1 = 3D, 2 = 2D, 3 = shovelomics)
repetitions                 <- c(1:5) # Repetitions

# Influence total size
P_penteVitDiam_range        <- seq(from=15, to=25, by=5) # Slope between the diametr and the root growth [-]
P_propDiamRamif_range       <- seq(from=0.5, to=0.9, by=0.9) # Relation between the diameter of a parent and a child root
P_distRamif_range           <- seq(from=4, to=6, by=2) # Distance between two successive lateral roots [mm]
P_coeffCroissRad_range      <- seq(from=0, to=0, by=0.5) # Coefficient of radial growth
P_diamMax_range             <- seq(from=0.4, to=1.6, by=0.1) # Max diameter for the primary roots [mm]
P_maxLatAge_range           <- seq(from=5, to=25, by=5)  # Maximal age growing age for the laterals [days]

# Influence root exploration
P_angLat_range              <- seq(from=1.5, to=1.5, by=0.5) # Emission angle for the laterals [radian]
P_intensiteTropisme_range   <- seq(from=0.001, to=0.031, by=0.041) # strenght of the gravitropic response
P_angInitMoyVertPrim_range  <- seq(from=1.6, to=1.7, by=2.5)  # Emission angle for the principal roots. Between 0 and PI/2 [radian]

create_images <- T      # Using RSML_reader.jar
analyse_images  <- F    # Using MARIA_J.jar

#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------


# Name of the simulation
name <- ""

# Root system parameters

## General
P_penteDureeVieDiamTMD <- 2000
P_coeffCroissRad <- 0.5 # Coefficient of radial growth
P_probaMaxArret <- 0.6 # Probability of a root to stop growing
P_TMD <- 0.1

## Primary roots
P_diamMax <- 1.2 # Max diameter for the primary roots [mm]
P_penteVitDiam <- 30 #51 # Slope between the diametr and the root growth [-]
P_angInitMoyVertPrim <- 1.4 # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_slopePrimAngle <- -0.3 # Slope btw the age of the and the insertion angle of the primary
P_vitEmissionPrim <- 1 # Speed of emission of the primary roots [root/day]
P_simultEmiss <- 0 # Emmission of seminal roots, 3 days after the start (0 = NO, 1 = YES)
P_nbSeminales <- 5 # Number of seminal roots
P_nbMaxPrim <- 40 # Max number of primary axis

## Secondary roots
P_intensiteTropisme <- 0.001 # strenght of the gravitropic response
P_propDiamRamif <- 0.4 # Relation between the diameter of a parent and a child root
P_distRamif <- 2 # Distance between two successive lateral roots [mm]
P_maxLatAge <- 10  # Maximal age growing age for the laterals [days]
P_angLat <- 1.3 # Emission angle for the laterals [radian]
P_tertiary <- 0 # Generate the tertiary roots
P_diamMin  <-  0.014 # Min diameter for a root [mm]
P_coeffVarDiamRamif <- 0.2 # Variation coefficient for the diameter fof the laterals

# Simulation parameters
P_duree <- 12  # Total length lenght of the simulation [days]
P_type <- 1    # Type of simuulation (1 = 3D, 2 = 2D, 3 = shovelomics)
P_IC_meca <- 0.03   # Mecanical impedence of the soil
P_shovel <- 70 # Depth of sampling for the shovelomics


# Loop over the parameters space
counter <- 0
t <- Sys.time()

# Evaluate the number of simulations
tot_sim <- 0
percent <- 0

for(e in eval){
  
  for(P_nbMaxPrim in P_nbMaxPrim_range){
    
    for(P_type in P_type_range){
      
      for(P_penteVitDiam in P_penteVitDiam_range){
        
        for(P_angInitMoyVertPrim in P_angInitMoyVertPrim_range){
          
          for(P_intensiteTropisme in P_intensiteTropisme_range){
            
            for(P_propDiamRamif in P_propDiamRamif_range){
              
              for(P_distRamif in P_distRamif_range){
                
                for(P_coeffCroissRad in P_coeffCroissRad_range){
                  
                  for(P_diamMax in P_diamMax_range){
                    
                    for(P_maxLatAge in P_maxLatAge_range){
                      
                      for(P_angLat in P_angLat_range){
                          
                        for(i in repetitions){ # Repetitions
                          
                          if(e){
                            # check if dicot or monocot root
                            species <- "dicot"                  
                            if(P_nbMaxPrim > 1){
                              P_coeffCroissRad <- 0
                              species <- "monocot"
                            }
         
                            # Change the name pre-fix based on the simualtion type
                            if(P_type == 1) basename <- paste("outputs/rsml/", species, "-3D", sep="")
                            if(P_type == 2) basename <- paste("outputs/rsml/", species, "-2D", sep="")
                            if(P_type == 3) basename <- paste("outputs/rsml/", species, "-shov", sep="")
                           
                            diam <- P_diamMax + (0.1 * runif(1, 0, 1)) # Add some variability to the diameter
                            
                            # Setup the name of the file, containing the principal info about the simulation
                            name <- paste(basename, "-", 
                                          P_penteVitDiam,"-", 
                                          P_vitEmissionPrim, "-", 
                                          P_angInitMoyVertPrim, "-",
                                          P_intensiteTropisme, "-", 
                                          P_propDiamRamif, "-", 
                                          P_distRamif ,"-", 
                                          P_coeffCroissRad, "-", 
                                          diam , "-", 
                                          P_angLat , "-", 
                                          P_maxLatAge, "-r", 
                                          i, 
                                          sep="")
                            if(verbatim) message(name)
                            
                            var <- c(P_duree,
                                   P_simultEmiss,
                                   P_vitEmissionPrim,
                                   P_nbSeminales,
                                   P_nbMaxPrim,
                                   P_diamMin,
                                   diam,
                                   P_penteVitDiam,
                                   P_intensiteTropisme,
                                   P_distRamif,
                                   P_propDiamRamif,
                                   P_coeffVarDiamRamif,
                                   P_probaMaxArret,
                                   P_TMD,
                                   P_penteDureeVieDiamTMD,
                                   P_coeffCroissRad,
                                   P_angLat,
                                   P_tertiary,
                                   name,
                                   P_type,
                                   P_IC_meca,
                                   P_shovel,
                                   P_maxLatAge,
                                   P_angInitMoyVertPrim,
                                   P_slopePrimAngle
                            )
                            cat(var, file=paste("param.txt",sep=""), sep='\n') # Create the input file for Archisimple
                            system("./ArchiSimp5Maria")  # Run Archisimple
                          }
                          counter <- counter+1
                          
                          # Counter to track the evolution of the simulations
                          if(e){
                            prog <- ( counter / tot_sim ) * 100
                            if(prog > percent){
                              message(paste(round(prog), "% of root systems generated"))
                              percent <- percent + 10
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  if(e){
    # Create and save the images
    if(create_images) system('java -Xmx4000m -jar RSML_reader.jar "outputs/rsml/" "outputs/images/" "outputs/root_data.csv"')
  
    # Analyse the images
    if(analyse_images) system('java -Xmx4000m -jar MARIA.jar "outputs/images/" "outputs/root_estimators.csv" "3"')  
   }
  tot_sim <- counter
  counter <- 0
  percent <- 0
  message(paste(tot_sim," root systems will be created..."))
}




diff = Sys.time() - t
print(paste(tot_sim," root systems were generated in ",diff," seconds"), quote=F)

#------------------------------------------------------------------------

# References

# Pagès et al. (2013). 
# Calibration and evaluation of ArchiSimple, a simple model of root system architecture, 
# 290, 76–84. doi:10.1016/j.ecolmodel.2013.11.014
