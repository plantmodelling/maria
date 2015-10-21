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
#   - Then, use a Random Forest algorithm to define the linear equation for each variable
#   - Finaly, perfom a k-means clustering in the simulation data to be re-used during the image fitting

library(data.table)

options(scipen=999) # Disable scientific notation
#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------


# Where is ArchiSimple
setwd("/Users/guillaumelobet/Dropbox/research/projects/research/MARIA/maria_scripts/01_model_generator/") # Where is the ArchiSimple folder?
eval = c(F,T)  # To check the number root system that will be generated, use c(F). To generate then, use c(F,T)
verbatim <- T

# Range of parameters
P_nbMaxPrim_range           <-  c(1) # Number of primary axes. Put 40 to have a monocot, 1 to have a dicot
P_type_range                <- c(1) # Type of simulation (1 = 3D, 2 = 2D, 3 = shovelomics)
repetitions                 <- c(1) # number of repetitions for each parameter set
P_duree <- 12  # Total length lenght of the simulation [days] 12 is a good compromise between the speed of the simulation and the final size of the root system


# Influence total size
P_penteVitDiam_range        <- seq(from=12, to=35, by=10) # Slope between the diameter and the root growth [-]
P_propDiamRamif_range       <- seq(from=0.5, to=0.9, by=0.4) # Relation between the diameter of a parent and a child root
P_distRamif_range           <- seq(from=4, to=6, by=2) # Distance between two successive lateral roots [mm]
P_coeffCroissRad_range      <- seq(from=0, to=0, by=0.5) # Coefficient of radial growth. 0 for monocots
P_diamMax_range             <- seq(from=0.4, to=1.6, by=0.5) # Max diameter for the primary roots [mm]
P_maxLatAge_range           <- seq(from=5, to=25, by=10)  # Maximal age growing age for the laterals [days]

# Influence root exploration
P_angLat_range              <- seq(from=0.4, to=1.5, by=1) # Emission angle for the laterals [radian]
P_intensiteTropisme_range   <- seq(from=0.001, to=0.031, by=0.02) # strenght of the gravitropic response
P_angInitMoyVertPrim_range  <- seq(from=1.3, to=1.7, by=0.2)  # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_tertiary <- 0           # Generate the tertiary roots

stochasticity <- T      # Try to have parameters covering the whoel space of posibilities by varying them.

create_rsml <- T        # Create the RSML files ArchiSimple
create_images <- T      # Using RSML_reader.jar
analyse_images  <- T    # Using MARIA_J.jar
machine_learning <- F   # Machine learning to define the equations for the fitting
create_clusters <- F    # k-means clustering to the image fitting



# Clustering parameters
n_clusters <- 50   # Number of clusters to create

# Random Forest parameters
n_trees <- 10      # the number of trees for a random forest model
                   # (this factor affects the most the running time and precision, for large data sets a value of 5-20 is fine)
n_var <- 5         # the number of most important variables of a random forest that shall be used as 
                   # predictors in the linear model ( to much variables causes overfitting )
accu <- 0.95        # the level of desired precision that is expected a response variable to have in order to considered
                   # as a predictor variable in the remodeling step two

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
P_penteVitDiam <- 15 #51 # Slope between the diametr and the root growth [-]
P_angInitMoyVertPrim <- 1.4 # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_slopePrimAngle <- -0.3 # Slope btw the age of the and the insertion angle of the primary
P_vitEmissionPrim <- 1 # Speed of emission of the primary roots [root/day]
P_simultEmiss <- 0 # Emmission of seminal roots, 3 days after the start (0 = NO, 1 = YES)
P_nbSeminales <- 5 # Number of seminal roots
P_nbMaxPrim <- 40 # Max number of primary axis

## Secondary roots
P_intensiteTropisme <- 0.001 # strenght of the gravitropic response
P_propDiamRamif <- 0.8 # Relation between the diameter of a parent and a child root
P_distRamif <- 2 # Distance between two successive lateral roots [mm]
P_maxLatAge <- 10  # Maximal age growing age for the laterals [days]
P_angLat <- 1.3 # Emission angle for the laterals [radian]
P_diamMin  <-  0.014 # Min diameter for a root [mm]
P_coeffVarDiamRamif <- 0.2 # Variation coefficient for the diameter fof the laterals

# Simulation parameters
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
  countss <- 1
  
######################## Create the RSML files
  
  if(create_rsml){
    setwd("./archisimple/") # Set the correct repository
    
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
                              if(countss < 300e9){
                              countss <- countss+1
                              if(e){
                                # check if dicot or monocot root
                                species <- "dicot"                  
                                if(P_nbMaxPrim > 1){
                                  P_coeffCroissRad <- 0
                                  species <- "monocot"
                                }
             
                                # Change the name pre-fix based on the simualtion type
                                if(P_type == 1) basename <- paste("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/", species, "-3D", sep="")
                                if(P_type == 2) basename <- paste("/Users/guillaumelobet/Desktop /Work/archisimple/outputs/rsml/", species, "-2D", sep="")
                                if(P_type == 3) basename <- paste("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/", species, "-shov", sep="")
                               

                                if(stochasticity){
                                  P_stocha <- (mean(diff(P_diamMax_range)) / P_diamMax) * 0.5
                                  P_diamMax_1 <- round(P_diamMax * runif(1, 1 - P_stocha, 1 + P_stocha), 3) # Add some variability to the diameter
                                  
                                  P_stocha <- (mean(diff(P_angLat_range)) / P_angLat) * 0.5
                                  P_angLat_1 <- round(P_angLat * runif(1, 1 - P_stocha, 1 + P_stocha), 3) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_angInitMoyVertPrim_range)) / P_angInitMoyVertPrim) * 0.5
                                  P_angInitMoyVertPrim_1 <- max(1,round(P_angInitMoyVertPrim  * runif(1, 1 - P_stocha, 1 + P_stocha), 3)) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_penteVitDiam_range)) / P_penteVitDiam) * 0.5
                                  P_penteVitDiam_1 <- round(P_penteVitDiam * runif(1, 1 - P_stocha, 1 + P_stocha), 1) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_intensiteTropisme_range)) / P_intensiteTropisme) * 0.5
                                  P_intensiteTropisme_1 <- round(P_intensiteTropisme  * runif(1, 1 - P_stocha, 1 + P_stocha), 5) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_propDiamRamif_range)) / P_propDiamRamif) * 0.5
                                  P_propDiamRamif_1 <- round(P_propDiamRamif  * runif(1, 1 - P_stocha, 1 + P_stocha), 3) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_distRamif_range)) / P_distRamif) * 0.5
                                  P_distRamif_1 <- round(P_distRamif  * runif(1, 1 - P_stocha, 1 + P_stocha), 3) # Add some variability to the parameter
                                  
                                  P_stocha <- (mean(diff(P_maxLatAge_range)) / P_maxLatAge) * 0.5
                                  P_maxLatAge_1 <- round(P_maxLatAge  * runif(1, 1 - P_stocha, 1 + P_stocha), 2) # Add some variability to the parameter
                                  
                                }
                                else{
                                  P_diamMax_1 <- P_diamMax
                                  P_angLat_1 <- P_angLat
                                  P_angInitMoyVertPrim_1 <-P_angInitMoyVertPrim
                                  P_penteVitDiam_1 <- P_penteVitDiam
                                  P_intensiteTropisme_1 <- P_intensiteTropisme
                                  P_propDiamRamif_1 <- P_propDiamRamif
                                  P_distRamif_1 <- P_distRamif
                                  P_maxLatAge_1 <- P_maxLatAge
                                 }
                                # Setup the name of the file, containing the principal info about the simulation
                                name <- paste(basename, "-", 
                                              P_penteVitDiam_1,"-", 
                                              P_vitEmissionPrim, "-", 
                                              P_angInitMoyVertPrim_1, "-",
                                              P_intensiteTropisme_1, "-", 
                                              P_propDiamRamif_1, "-", 
                                              P_distRamif_1 ,"-", 
                                              P_coeffCroissRad, "-", 
                                              P_diamMax_1 , "-", 
                                              P_angLat_1 , "-", 
                                              P_maxLatAge_1, "-r", 
                                              i, 
                                              sep="")
                                if(verbatim) message(name)
                                
                                var <- c(P_duree,
                                       P_simultEmiss,
                                       P_vitEmissionPrim,
                                       P_nbSeminales,
                                       P_nbMaxPrim,
                                       P_diamMin,
                                       P_diamMax_1,
                                       P_penteVitDiam_1,
                                       P_intensiteTropisme_1,
                                       P_distRamif_1,
                                       P_propDiamRamif_1,
                                       P_coeffVarDiamRamif,
                                       P_probaMaxArret,
                                       P_TMD,
                                       P_penteDureeVieDiamTMD,
                                       P_coeffCroissRad,
                                       P_angLat_1,
                                       P_tertiary,
                                       name,
                                       P_type,
                                       P_IC_meca,
                                       P_shovel,
                                       P_maxLatAge_1,
                                       P_angInitMoyVertPrim_1,
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
                                  percent <- percent + 1
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
    }
    setwd("../") # Back to the old repo
  }
  
  if(e){
    
######################## Create and save the images

    if(create_images){
      message("Creating root images and ground truth data")
      system('java -Xmx4000m -jar RSML_reader.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/" "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "../07_maria_shiny/data/root_data.csv"')
      #system('java -Xmx4000m -jar RSML_reader.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/" "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet//Desktop/root_data.csv"')
    }
  
######################## Analyse the images
    
    if(analyse_images){
      message("Analysing root images")
      system('java -Xmx4000m -jar MARIAJ.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "../07_maria_shiny/data/root_estimators.csv" "3"')  
      #system('java -Xmx4000m -jar MARIAJ.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet/Desktop/root_estimators.csv" "3"')  
    }
    
########################  Machine learning to retrieve the parameters
    
    if(machine_learning){
      message("Machine learning started")
      source("maria_learning.r")  
      
      descriptors <- read.csv("../07_maria_shiny/data/root_estimators.csv")
      parameters <- read.csv("../07_maria_shiny/data/root_data.csv")
      colnames(parameters)[colnames(parameters) == "width"] <- "true_width"
      colnames(parameters)[colnames(parameters) == "depth"] <- "true_depth"
      diff <- nrow(descriptors) - nrow(parameters)
      if(diff < 0) parameters <- parameters[0:nrow(descriptors),]
      if(diff > 0) descriptors <- descriptors[0:nrow(parameters),]
      
      parameters <- parameters[,-c(6,19)] # Remove the non varying patameters (n primary for instance)
      
      # Add the simulation parameters to the set of variables to estimate
      for(i in 1:nrow(parameters)){
        args <- strsplit(as.character(parameters$image[i]), "-")[[1]]
        parameters$P_penteVitDiam[i] <- args[3] #--------------
        parameters$P_angInitMoyVertPrim[i] <- args[5] #--------------
        parameters$P_intensiteTropisme[i] <- args[6] #--------------
        parameters$P_propDiamRamif[i] <- args[7] #--------------
        parameters$P_distRamif[i] <- args[8] #--------------
        parameters$P_diamMax[i] <- args[10] #--------------
        parameters$P_angLat[i] <- args[11]
        parameters$P_maxLatAge[i] <- args[12]
      }
      for(i in 18:25) parameters[,i] <- as.numeric(parameters[,i])
      rs <- cbind(descriptors, parameters)

      
# Get the set of linear equations with the whole dataset
      regs <- maria_learning(fname = rs, 
                                  f_vec = c(73:96), 
                                  p_vec = c(2:71,73:96), 
                                  nrt = n_trees, 
                                  ntop = n_var, 
                                  acc = accu)      
      
      to_est <- names(regs[[1]])
      
      # Format the dataset
      maria_regs <- list()
      for(te in to_est){
        maria_regs[[te]] <- list(fit1 = regs[[1]][[te]], 
                                 fit2 = regs[[2]][[te]], 
                                # error = error[[te]],
                                 x = param_test[[te]])
      }
      
      # Save the results to be reused in shiny
      maria_regs <- regs
      save(maria_regs, file="../07_maria_shiny/data/maria_regs.RData")
      regs$best
      
      # Vizualize the results
      descriptors <- read.csv("../07_maria_shiny/data/root_estimators.csv")
      results <- test_training(regs, descriptors)
      
      to_est <- to_est[-c(5,18)]
      
      # Look at the quality of the estimations
      par(mfrow=c(4,4), mar=c(4,4,4,3))
      for(te in to_est){
        print(te)
        estimation <- results[[te]] 
        truth <- parameters[[te]]
        fit <- lm(estimation ~ truth)
        title <- paste(te, " || ", round(summary(fit)$r.squared, 3))
        plot(truth, estimation, main = title, col="#00000050")
        abline(a = 0, b = 1, lty=2, col="red", lwd=2)
        abline(fit)
      }
      
      colnames(results) <- paste0("est_",colnames(results))
      to_match <- cbind(descriptors,results)
      g_truth <- cbind(descriptors,parameters[-1])
      write.csv(to_match, "~/Desktop/estimators.csv")
      write.csv(parameters, "~/Desktop/data.csv")
    
    }
    
    # Create k-means clusters
    if(create_clusters){
      
      message("Clusering started")
      
      descriptors <- fread("../07_maria_shiny/data/root_estimators.csv")
      save(descriptors, file="../07_maria_shiny/data/maria_descriptors.RData")
      descriptors <- data.frame(descriptors)
      
      parameters <- fread("../07_maria_shiny/data/root_data.csv")
      save(parameters, file="../07_maria_shiny/data/maria_parameters.RData")
      parameters <- data.frame(parameters)
      
      colnames(parameters)[colnames(parameters) == "width"] <- "true_width"
      colnames(parameters)[colnames(parameters) == "depth"] <- "true_depth"
      sim_data <- cbind(parameters, descriptors[2:ncol(descriptors)])
      
      #to_cluster <- na.omit(sim_data[,2:(ncol(sim_data)-4)]) # listwise deletion of missing
      to_cluster <- scale(sim_data[,2:(ncol(sim_data)-4)])
      # K-Means Cluster Analysis
      fit <- kmeans(to_cluster, 5) # 5 cluster solution
      
      # get cluster means 
      clusters <- aggregate(to_cluster, by=list(fit$cluster), FUN=mean)
      save(clusters, file="../07_maria_shiny/data/maria_clusters.RData")
      
      # append cluster assignment
      sim_data <- data.frame(sim_data, fit$cluster) 
      save(sim_data, file="../07_maria_shiny/data/maria_sim_data.RData")
    }
    
   }
  tot_sim <- counter
  counter <- 0
  percent <- 0
  if(!e) message(paste(tot_sim," root systems will be created..."))
}

# Print the results
diff = Sys.time() - t
print(paste(tot_sim," root systems were generated and analysed in ",diff," seconds"), quote=F)

#------------------------------------------------------------------------

# References

# Pagès et al. (2013). 
# Calibration and evaluation of ArchiSimple, a simple model of root system architecture, 
# 290, 76–84. doi:10.1016/j.ecolmodel.2013.11.014
