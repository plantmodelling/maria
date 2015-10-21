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

verbatim <- F           # Display messages
delete_old <- T         # Delete old simulation data

stochasticity <- T      # Try to have parameters covering the whoel space of posibilities by varying them.

create_rsml <- T        # Create the RSML files ArchiSimple
create_images <- T      # Using RSML_reader.jar
analyse_images  <- T    # Using MARIA_J.jar
machine_learning <- T   # Machine learning to define the equations for the fitting
create_clusters <- F    # k-means clustering to the image fitting


# Range of parameters
P_type                      <- 3        # Type of simulation (1 = 3D, 2 = 2D, 3 = shovelomics)
if(P_type == 1) type_name   <- "3D"
if(P_type == 2) type_name   <- "2D"
if(P_type == 3) type_name   <- "shov"
specie                      <- "monocot"  # Type of species ("monocot", "dicot")
repetitions                 <- 500       # number of repetitions for each parameter set

P_duree_range               <- c(5,15)  # Total length lenght of the simulation [days] 12 is a good compromise between the speed of the simulation and the final size of the root system
P_nbMaxPrim_range           <-  c(10,70)  # Number of primary axes. Put c(10,70) to have a monocot, c(1,1) to have a dicot
P_coeffCroissRad_range      <- c(0,0)   # Coefficient of radial growth. 0 for monocots

# Influence total size
P_penteVitDiam_range        <- c(10, 35)    # Slope between the diameter and the root growth [-]
P_propDiamRamif_range       <- c(0.1, 0.9)  # Relation between the diameter of a parent and a child root
P_distRamif_range           <- c(2, 6)      # Distance between two successive lateral roots [mm]
P_diamMax_range             <- c(0.1, 1.7)  # Max diameter for the primary roots [mm]
P_maxLatAge_range           <- c(1, 10)     # Maximal age growing age for the laterals [days]

# Influence root exploration
P_angLat_range              <- c(0.8, 1.7)      # Emission angle for the laterals [radian]
P_intensiteTropisme_range   <- c(0.001, 0.031)  # strenght of the gravitropic response
P_angInitMoyVertPrim_range  <- c(1, 1.7)        # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_tertiary                  <- 0                # Generate the tertiary roots

# Clustering parameters
n_clusters                  <- 50               # Number of clusters to create

# Random Forest parameters
n_trees                     <- 15               # the number of trees for a random forest model
                                                # (this factor affects the most the running time and precision
                                                # for large data sets a value of 5-20 is fine)
n_var                       <- 5                # the number of most important variables of a random forest that shall be used as 
                                                # predictors in the linear model ( to much variables causes overfitting )
accu                        <- 0.8             # the level of desired precision that is expected a response variable to have in order to considered
                                                # as a predictor variable in the remodeling step two

#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------

dir.img <- paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",specie,"/",type_name,"/images")
dir.rsml <- paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",specie,"/",type_name,"/rsml")
if(delete_old){
  unlink(dir.img, recursive = TRUE, force = TRUE)
  unlink(dir.rsml, recursive = TRUE, force = TRUE)
}
dir.create(dir.img, showWarnings = F)
dir.create(dir.rsml, showWarnings = F)


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
P_IC_meca <- 0.03   # Mecanical impedence of the soil
P_shovel <- 70 # Depth of sampling for the shovelomics


# Loop over the parameters space
counter <- 0
t <- Sys.time()

tot_time <- 0
mean_time <- NA

# Evaluate the number of simulations
percent <- 0

######################## Create the RSML files
if(create_rsml){
  setwd("./archisimple/") # Set the correct repository
  for(i in 1:repetitions){ # Repetitions
      # check if dicot or monocot root
      species <- "dicot"                  
      if(P_nbMaxPrim > 1){
        P_coeffCroissRad <- 0
        species <- "monocot"
      }
    
      # Change the name pre-fix based on the simualtion type
      basename <- paste0(dir.rsml,"/", species, "-", type_name)
     
      P_nbMaxPrim <- round(runif(1, P_nbMaxPrim_range[1], P_nbMaxPrim_range[2]), 0)
      P_diamMax <- round(runif(1, P_diamMax_range[1], P_diamMax_range[2]), 3)
      P_angLat <- round(runif(1,P_angLat_range[1],P_angLat_range[2]),3)
      P_angInitMoyVertPrim <-round(runif(1,P_angInitMoyVertPrim_range[1],P_angInitMoyVertPrim_range[2]),3)
      P_penteVitDiam <- round(runif(1,P_penteVitDiam_range[1],P_penteVitDiam_range[2]),3)
      P_intensiteTropisme <- round(runif(1,P_intensiteTropisme_range[1],P_intensiteTropisme_range[2]),5)
      P_propDiamRamif <- round(runif(1,P_propDiamRamif_range[1],P_propDiamRamif_range[2]),3)
      P_distRamif <- round(runif(1,P_distRamif_range[1],P_distRamif_range[2]),3) 
      P_maxLatAge <- round(runif(1,P_maxLatAge_range[1],P_maxLatAge_range[2]),3)
      P_duree <- round(runif(1,P_duree_range[1],P_duree_range[2]),0)
      P_coeffCroissRad <- round(runif(1,P_coeffCroissRad_range[1],P_coeffCroissRad_range[2]),3)
  
      # Setup the name of the file, containing the principal info about the simulation
      name <- paste(basename, "-", 
                    P_penteVitDiam,"-", 
                    P_vitEmissionPrim, "-", 
                    P_angInitMoyVertPrim, "-",
                    P_intensiteTropisme, "-", 
                    P_propDiamRamif, "-", 
                    P_distRamif ,"-", 
                    P_coeffCroissRad, "-", 
                    P_diamMax , "-", 
                    P_angLat , "-",
                    P_maxLatAge , "-",
                    P_duree, "-r", 
                    i, 
                    sep="")
      if(verbatim) message(name)
      
      var <- c(P_duree,
             P_simultEmiss,
             P_vitEmissionPrim,
             P_nbSeminales,
             P_nbMaxPrim,
             P_diamMin,
             P_diamMax,
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
      t <- Sys.time()
      system("./ArchiSimp5Maria")  # Run Archisimple
      t1 <- Sys.time() - t
      tot_time <- tot_time + t1
            
      counter <- counter+1
      
      # Counter to track the evolution of the simulations
      prog <- ( counter / max(repetitions) ) * 100
      if(prog > percent){
        message(paste0(round(prog), "% of root systems generated / Mean simulation time = ",
                       round(((tot_time/i)),3), " / total simulation time = ",round((tot_time),3)))
        percent <- percent + 5
      }
  }
  setwd("../") # Back to the old repo
}
  
    
######################## Create and save the images

    if(create_images){
      message("Creating root images and ground truth data")
      system(paste0('java -Xmx4000m -jar RSML_reader.jar ',dir.rsml,' ',dir.img,' "../07_maria_shiny/data/root_data_',specie,'-',type_name,'.csv"'))
      #system('java -Xmx4000m -jar RSML_reader.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/" "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet//Desktop/root_data.csv"')
    }
  
######################## Analyse the images
    
    if(analyse_images){
      message("Analysing root images")
      system(paste0('java -Xmx4000m -jar MARIAJ.jar ',dir.img,' "../07_maria_shiny/data/root_estimators_',specie,'-',type_name,'.csv" "3"'))  
      #system('java -Xmx4000m -jar MARIAJ.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet/Desktop/root_estimators.csv" "3"')  
    }
    
########################  Machine learning to retrieve the parameters
    
    if(machine_learning){
      message("Machine learning started")
      source("maria_learning.r")  
      
      descriptors <- read.csv(paste0("../07_maria_shiny/data/root_estimators_",specie,"-",type_name,".csv"))
      parameters <- read.csv(paste0("../07_maria_shiny/data/root_data_",specie,"-",type_name,".csv"))
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
        parameters$P_duree[i] <- args[13]
      }
      for(i in 18:26) parameters[,i] <- as.numeric(parameters[,i])
      rs <- cbind(descriptors, parameters)
      
      par(mfrow=c(4,4))
      for(i in 2:26){
        hist(parameters[,i], main=colnames(parameters[i]), breaks=20,freq = F)
        lines(density(parameters[,i]), col='red', lwd=2)
      }

      
# Get the set of linear equations with the whole dataset
      regs <- maria_learning(fname = rs, 
                                  f_vec = c(73:96), 
                                  p_vec = c(2:71,73:96), 
                                  nrt = n_trees, 
                                  ntop = n_var, 
                                  acc = accu)      
      
      to_est <- names(regs[[1]])
      
#       # Format the dataset
#       maria_regs <- list()
#       for(te in to_est){
#         maria_regs[[te]] <- list(fit1 = regs[[1]][[te]], 
#                                  fit2 = regs[[2]][[te]], 
#                                 # error = error[[te]],
#                                  x = param_test[[te]])
#       }
#       
#       # Save the results to be reused in shiny
#       maria_regs <- regs
#       save(maria_regs, file="../07_maria_shiny/data/maria_regs.RData")
#       regs$best
      
      # Vizualize the results
      #descriptors <- read.csv("../07_maria_shiny/data/root_estimators.csv")
      results <- test_training(regs, descriptors)
      
      to_est <- to_est[-c(5,19)]
      
      # Look at the quality of the estimations
      par(mfrow=c(4,4), mar=c(4,4,4,3))
      for(te in to_est){
        estimation <- results[[te]] 
        truth <- parameters[[te]]
        fit <- lm(estimation ~ truth)
        title <- paste(te, " || ", round(summary(fit)$r.squared, 3))
        plot(truth, estimation, main = title, col="#00000050")
        abline(a = 0, b = 1, col="blue", lwd=2)
        abline(fit, lwd=2, lty=2, col="red")
      }
      
#       colnames(results) <- paste0("est_",colnames(results))
#       to_match <- cbind(descriptors,results)
#       g_truth <- cbind(descriptors,parameters[-1])
#       write.csv(to_match, "~/Desktop/estimators.csv")
#       write.csv(parameters, "~/Desktop/data.csv")
    
    }
    
########################  Create k-means clusters
  
    if(create_clusters){
      
      message("Clusering started")
      
      descriptors <- fread(paste0("../07_maria_shiny/data/root_estimators.csv"))
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
    
#------------------------------------------------------------------------

# Print the results
diff = Sys.time() - t
print(paste(repetitions," root systems were generated and analysed in ",diff," seconds"), quote=F)


#------------------------------------------------------------------------


# References

# Pagès et al. (2013). 
# Calibration and evaluation of ArchiSimple, a simple model of root system architecture, 
# 290, 76–84. doi:10.1016/j.ecolmodel.2013.11.014
