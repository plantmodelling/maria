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
library(ggplot2)

options(scipen=999) # Disable scientific notation

#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------


# Where is ArchiSimple
setwd("/Users/guillaumelobet/Dropbox/research/projects/research/MARIA/maria_scripts/01_model_generator/") # Where is the ArchiSimple folder?

verbatim <- T           # Display messages
delete_old <- T         # Delete old simulation data

create_rsml <- F        # Create the RSML files ArchiSimple
create_images <- F      # Using RSML_reader.jar
analyse_images  <- F    # Using MARIA_J.jar
machine_learning <- T   # Machine learning to define the equations for the fitting
create_clusters <- F    # k-means clustering to the image fitting


# Range of parameters
P_type_range                <- c(1,2)        # Type of simulation (1 = 3D, 2 = 2D, 3 = shovelomics)
P_specie_range              <- c("monocot", "dicot")  # Type of species ("monocot", "dicot")
repetitions                 <- 10000       # number of repetitions
P_maxSegments               <- 10000 # Max number of segments allowed in the simulation

P_duree_range               <- c(5,20)  # Total length lenght of the simulation [days] 12 is a good compromise between the speed of the simulation and the final size of the root system
P_nbMaxPrim_dicot           <-  c(1,1)  # Number of primary axes. Put c(10,70) to have a monocot, c(1,1) to have a dicot
P_coeffCroissRad_dicot      <- c(0,0.5)   # Coefficient of radial growth. 0 for monocots
P_nbMaxPrim_monocot         <-  c(10,70)  # Number of primary axes. Put c(10,70) to have a monocot, c(1,1) to have a dicot
P_coeffCroissRad_monocot    <- c(0,0)   # Coefficient of radial growth. 0 for monocots

# Influence total size
P_penteVitDiam_range        <- c(10, 50)    # Slope between the diameter and the root growth [-]
P_propDiamRamif_range       <- c(0.2, 0.9)  # Relation between the diameter of a parent and a child root
P_distRamif_range           <- c(1, 6)      # Distance between two successive lateral roots [mm]
P_diamMax_range             <- c(0.1, 1.7)  # Max diameter for the primary roots [mm]
P_maxLatAge_range           <- c(1, 25)     # Maximal age growing age for the laterals [days]
P_vitEmissionPrim_range     <- c(0.1, 5)    # Speed of emission of the primary roots [root/day]

# Influence root exploration
P_angLat_range              <- c(0.8, 1.7)      # Emission angle for the laterals [radian]
P_intensiteTropisme_range   <- c(0, 0.035)  # strenght of the gravitropic response
P_angInitMoyVertPrim_range  <- c(0.8, 1.7)        # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_tertiary                  <- 0                # Generate the tertiary roots

# Clustering parameters
n_clusters                  <- 50               # Number of clusters to create

# Random Forest parameters

vec.models <- c(5,20)                 # Vector with the number of models to try
vec.trees <- c(5,20)                  # Vector with the number of tree to try in each model

to_est_base <-c("tot_root_length",         # Vector of parameters to estimate with the machine learning
           #"true_width","true_depth",
           "direction",
           "n_primary","tot_prim_length","mean_prim_length","mean_prim_diameter",
           "mean_lat_density","n_laterals","tot_lat_length","mean_lat_length","mean_lat_diameter","mean_lat_angle",
           "tree_size","tree_magnitude","tree_altitude","tree_index"
)

#------------------------------------------------------------------------
#------------------------------------------------------------------------
#------------------------------------------------------------------------

# Root system parameters

## General
P_penteDureeVieDiamTMD <- 2000
P_coeffCroissRad <- 0.5 # Coefficient of radial growth
P_probaMaxArret <- 0.6 # Probability of a root to stop growing
P_TMD <- 0.1

## Primary roots
P_diamMax <- 1.2 # Max diameter for the primary roots [mm]
P_penteVitDiam <- 15 #51 # Slope between the diameter and the root growth [-]
P_angInitMoyVertPrim <- 1.4 # Emission angle for the principal roots. Between 0 and PI/2 [radian]
P_slopePrimAngle <- -0.3 # Slope btw the age of the and the insertion angle of the primary
P_simultEmiss <- 0 # Emmission of seminal roots, 3 days after the start (0 = NO, 1 = YES)
P_nbSeminales <- 5 # Number of seminal roots
P_nbMaxPrim <- 40 # Max number of primary axis

## Secondary roots
P_intensiteTropisme <- 0.001 # strenght of the gravitropic response
P_propDiamRamif <- 0.8 # Relation between the diameter of a parent and a child root
P_distRamif <- 2 # Distance between two successive lateral roots [mm]
P_maxLatAge <- 10  # Maximal age growing age for the laterals [days]
P_angLat <- 1.3 # Emission angle for the laterals [radian]
P_diamMin  <-  0.0014 # Min diameter for a root [mm]
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

for(P_specie in P_specie_range){
  for(P_type in P_type_range){
    
    if(P_type == 1) type_name   <- "3D"
    if(P_type == 2) type_name   <- "2D"
    if(P_type == 3) type_name   <- "shov"
    
    # Set up the directories
    dir.img <- paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",P_specie,"/",type_name,"/images")
    dir.rsml <- paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",P_specie,"/",type_name,"/rsml")
    #dir.rsml <- paste0("/Users/guillaumelobet/Desktop/test")
    if(delete_old){
      unlink(dir.img, recursive = TRUE, force = TRUE)
      unlink(dir.rsml, recursive = TRUE, force = TRUE)
    }
    dir.create(dir.img, showWarnings = F)
    dir.create(dir.rsml, showWarnings = F)
    
    if(P_specie == "dicot"){
      P_nbMaxPrim_range       <- P_nbMaxPrim_dicot
      P_coeffCroissRad_range  <- P_coeffCroissRad_dicot
    }else{
      P_nbMaxPrim_range       <- P_nbMaxPrim_monocot
      P_coeffCroissRad_range  <- P_coeffCroissRad_monocot      
    }
    
    

######################## Create the RSML files
    if(create_rsml){
      setwd("./archisimple/") # Set the correct repository
      
      params_range <- data.frame(penteVitDiam = numeric(), 
                                nbMaxPrim = numeric(),
                                vitEmissionPrim = numeric(), 
                                angInitMoyVertPrim = numeric(),
                                intensiteTropisme = numeric(), 
                                propDiamRamif = numeric(), 
                                distRamif = numeric(), 
                                coeffCroissRad = numeric(), 
                                diamMax = numeric(), 
                                angLat = numeric(),
                                maxLatAge = numeric(),
                                duree = numeric())
        
      for(i in 1:repetitions){ # Repetitions
        
          # Change the name pre-fix based on the simualution type
          basename <- paste0(dir.rsml,"/", P_specie, "-", type_name)
         
          P_nbMaxPrim <- round(runif(1, P_nbMaxPrim_range[1], P_nbMaxPrim_range[2]), 0)
          P_diamMax <- round(runif(1, P_diamMax_range[1], P_diamMax_range[2]), 3)
          P_vitEmissionPrim <- round(runif(1, P_vitEmissionPrim_range[1], P_vitEmissionPrim_range[2]), 3)
          P_angLat <- round(runif(1,P_angLat_range[1],P_angLat_range[2]),3)
          P_angInitMoyVertPrim <-round(runif(1,P_angInitMoyVertPrim_range[1],P_angInitMoyVertPrim_range[2]),3)
          P_penteVitDiam <- round(runif(1,P_penteVitDiam_range[1],P_penteVitDiam_range[2]),3)
          P_intensiteTropisme <- round(runif(1,P_intensiteTropisme_range[1],P_intensiteTropisme_range[2]),5)
          P_propDiamRamif <- round(runif(1,P_propDiamRamif_range[1],P_propDiamRamif_range[2]),3)
          P_distRamif <- round(runif(1,P_distRamif_range[1],P_distRamif_range[2]),3) 
          P_maxLatAge <- round(runif(1,P_maxLatAge_range[1],P_maxLatAge_range[2]),3)
          P_duree <- round(runif(1,P_duree_range[1],P_duree_range[2]),0)
          P_coeffCroissRad <- round(runif(1,P_coeffCroissRad_range[1],P_coeffCroissRad_range[2]),3)
      
          params_range <- rbind(params_range, data.frame(penteVitDiam = P_penteVitDiam, 
                                                         nbMaxPrim = P_nbMaxPrim,
                                                         vitEmissionPrim = P_vitEmissionPrim, 
                                                         angInitMoyVertPrim = P_angInitMoyVertPrim,
                                                         intensiteTropisme = P_intensiteTropisme, 
                                                         propDiamRamif = P_propDiamRamif, 
                                                         distRamif = P_distRamif, 
                                                         coeffCroissRad = P_coeffCroissRad, 
                                                         diamMax = P_diamMax, 
                                                         angLat = P_angLat,
                                                          maxLatAge = P_maxLatAge,
                                                         duree = P_duree)
                                )
          
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
                 P_slopePrimAngle,
                 P_maxSegments
          )   
          cat(var, file=paste("param.txt",sep=""), sep='\n') # Create the input file for Archisimple
          t <- Sys.time()
          
          system("./ArchiSimp5Maria")  # Run Archisimple
          
          t1 <- Sys.time() - t
          tot_time <- tot_time + t1
                
          counter <- counter+1
          
          # Counter to track the evolution of the simulations
          prog <- ( counter / (repetitions*length(P_type_range) * length(P_specie_range)) ) * 100
          if(prog > percent){
            message(paste0(round(prog), "% of root systems generated / Mean simulation time = ",
                           round(((tot_time/i)),3), " / total simulation time = ",round((tot_time),3)))
            percent <- percent + 5
          }
      }
      write.csv(params_range, paste0("../../04_coverage/params_range_",P_specie,"-",type_name,".csv"))
      setwd("../") # Back to the old repo
    }
  
    
######################## Create and save the images

    if(create_images){
      message("Creating root images and ground truth data")
      system(paste0('java -Xmx6000m -jar RSML_reader.jar ',dir.rsml,' ',dir.img,' "data/root_data_',P_specie,'-',type_name,'-',repetitions,'.csv"'))
      #system('java -Xmx4000m -jar RSML_reader.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/rsml/" "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet//Desktop/root_data.csv"')
    }
  
######################## Analyse the images
    
    if(analyse_images){
      message("Analysing root images")
      system(paste0('java -Xmx6000m -jar MARIAJ.jar ',dir.img,' "data/root_estimators_',P_specie,'-',type_name,'-',repetitions,'.csv" "300"'))  
      #system('java -Xmx4000m -jar MARIAJ.jar "/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/" "/Users/guillaumelobet/Desktop/root_estimators.csv" "3"')  
    }
    
########################  Machine learning to retrieve the parameters
    
    if(machine_learning){
      message("Machine learning started")
      source("maria_learning.r")  
      PkgTest(c("randomForest", "ggplot2", "gridExtra"))

      #-------------------------------------------------------------------
      # SELECT THE RIGHT DATAFILES
      #-------------------------------------------------------------------
      
      lfiles <- list.files("data/")
      files <- data.frame(name = lfiles, specie=character(length(lfiles)),
                          type=character(length(lfiles)), size=numeric(length(lfiles)), file=character(length(lfiles)), 
                          stringsAsFactors=FALSE)
      for(i in 1:nrow(files)){
        n <- strsplit(gsub("_", "-",gsub(".csv", "", files$name[i])), "-")[[1]]
        files$specie[i] <- n[3]
        files$type[i] <- n[4]
        files$file[i] <- n[2]
        files$size[i] <- as.numeric(n[5])
      }
      remove(lfiles, i, n)
      
      #-------------------------------------------------------------------
      # LOAD AND PREPARE THE DATASETS
      #-------------------------------------------------------------------
      
      f1 <- files[files$specie == P_specie & files$type == type_name & files$size >= repetitions & files$file == "estimators",]
      to_load_ests <-  f1$name[f1$size == min(f1$size) & !grepl("analysis",f1$name)]
      f1 <- files[files$specie == P_specie & files$type == type_name & files$size >= repetitions & files$file == "data",]
      to_load_data <-  f1$name[f1$size == min(f1$size)]
      
      descriptors <- read.csv(paste0("data/",to_load_ests))
      dlength <- ncol(descriptors)
      vars <- colnames(descriptors)[-1]
      
      parameters <- read.csv(paste0("data/",to_load_data))
      colnames(parameters)[colnames(parameters) == "width"] <- "true_width"
      colnames(parameters)[colnames(parameters) == "depth"] <- "true_depth"
      
      descriptors$image <- gsub(paste0("/data/guillaume/maria_scripts/01_model_generator/archisimple/outputs/",P_specie,"/",type_name,"/image/"), "", descriptors$image)
      descriptors$image <- gsub(paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",P_specie,"/",type_name,"/images/"), "", descriptors$image)
      descriptors$image <- gsub(".jpg", "", descriptors$image)
      descr_ind <- c(2:(ncol(descriptors))) # Indexes of the descriptors
      
      rs <- merge(descriptors, parameters, by = "image")
      #rs <- rs[sample(1:nrow(rs), repetitions),] # If the dataset is bigger than wanted, downsize it
      
      cols <- colnames(rs)
      for(c in cols) rs <- rs[!is.nan(rs[[c]]),] # Remove the row containing  NA values
      for(c in cols) rs <- rs[!is.na(rs[[c]]),] # Remove the row containing  NA values
      
      remove(f1, to_load_data, to_load_ests)
      
      
      #-------------------------------------------------------------------
      # PARAMETERS TO ESTIMATE
      #-------------------------------------------------------------------
      to_est <- to_est_base
      if(P_specie == "dicot") to_est <- to_est[-match("n_primary",to_est)] 
      to_est_ind <- match(to_est, colnames(rs))
      descr_ind <- c(descr_ind, to_est_ind)
      
      
      #-------------------------------------------------------------------
      # RUN THE MODEL
      #-------------------------------------------------------------------
      
      models <- GenerateModels(fname = NULL, 
                               mat.data = rs, 
                               vec.models = vec.models, 
                               vec.trees = vec.trees, 
                               vec.f = to_est_ind, 
                               vec.p = descr_ind)

      
      par(mfrow=c(3,4))
      for(i in 1:12) ShowPerformance(models$Errors, i, 1)
      
      save(models, file=paste0("data/rfmodels-",P_specie,"-",type_name,"-",repetitions,".RData"))
      
      vec.weights <- rep(0.9, length(to_est))
      model <- SelectModel(models, vec.weights)
      save(model, file=paste0("data/model-",P_specie,"-",type_name,"-",repetitions,".RData"))
      
      
      #-------------------------------------------------------------------
      # GET THE ESTIMATIONS FROM THE MODEL AND THE EXPECTED ERROR
      #-------------------------------------------------------------------

      results <- PredictRFs(model, rs[,c(2:dlength)])
      errors <- GeneralPointErrorEstimate(lst.rfModels = model, 
                                          mat.data = rs[,c(2:ncol(rs))], 
                                          vec.f = models$ResponseVar)
      
      #-------------------------------------------------------------------
      # COMPARE THE MODELS WITH THE BEST CORRESPONDING  SINGLE LINEAR ESTIMATION
      #-------------------------------------------------------------------
      
      metaPrec <- data.frame(var1=character(), var2=character(), r1=numeric(), r2=numeric(), type=character())
      
      # Look at the quality of the estimations
      for(te in to_est){
        
        # Get the best single variable (regression)
        max <- 0
        keep <- ""
        for(v in vars){
          fit <- lm(rs[[te]] ~ rs[[v]])
          r2 <- summary(fit)$r.squared
          if(r2 > max){
            max <- r2
            keep <- v
          }
        }
        
        # Get the best single variable (RSME)
        max1 <- 0
        keep1 <- ""
        for(v in vars){
          estimation <- rs[[v]]
          truth <- rs[[te]]
          diff <- abs(mean(estimation, na.rm = T) / mean(truth, na.rm = T))
          if (diff > 1) diff <- abs(mean(truth, na.rm=T) / mean(estimation, na.rm=T))
          if(diff > max1){
            max1 <- diff
            keep1 <- v
          }
        }
        
        # Compare it to the Random Forest
        estimation <- results[[te]] 
        truth <- rs[[te]]
        
        diff <- abs(mean(estimation, na.rm = T) /mean(truth, na.rm = T))
        if (diff > 1) diff <- abs(mean(truth, na.rm=T) /mean(estimation, na.rm=T))
        
        fit <- lm(truth ~ estimation)
        
        # Save the data
        
        temp <- data.frame(var1=te, var2=keep,
                           r1=summary(fit)$r.squared, r2=max, 
                           type="r-squared")
        
        metaPrec <- rbind(metaPrec, temp)
        
        temp <- data.frame(var1=te, var2=keep1,
                           r1=diff, r2=max1, 
                           type="mean error")
        
        metaPrec <- rbind(metaPrec, temp)
      }
      
      metaPrec$rdiff <- metaPrec$r1 - metaPrec$r2
      metaPrec$col <- "0"
      metaPrec$col[metaPrec$rdiff < 0] <- "1"
      ggplot(data=metaPrec, aes(x=var1, y=rdiff, fill=col)) +
        geom_bar(stat="identity", position=position_dodge())+
        facet_grid(.~type) + 
        theme_bw() +
        ylab("Relative difference in estimation [-]") + 
        scale_fill_manual(values = c("grey", "orange")) + 
        theme(axis.text.x=element_blank(),axis.title.x=element_blank(), legend.position="none") + 
        ggsave(paste0("outputs/rf_accuracy-",P_specie,"-",type_name,"-",repetitions,".pdf"))
      
      remove(temp, fit, fit2, diff, diff2, estimation, estimation2, truth)
      remove(v, keep, max, r2, te)
      
      #-------------------------------------------------------------------
      # PLOT THE REGRESSIONS BETWEEN THE GROUND-TRUTH AND THE MODEL VALUES
      #-------------------------------------------------------------------
      # Create list containing the error terms
      error.reg <- list()
      
      pdf(paste0("outputs/rf_regressions-",P_specie,"-",type_name,"-",repetitions,".pdf"))
      par(mfrow=c(4,4), mar=c(4,4,4,3))
      for(te in to_est){

        x = rs[[te]]
        y = results[[te]]
        
        fit = lm(y~x)
        #Prediction intervals
        newx <- data.frame(x=seq(0, max(x), length.out=500))
        preds <-  predict(fit, newdata =newx, interval="prediction")
        error <- data.frame("value"=((preds[,1] - preds[,2]) / max(preds[,1]))*100, "x"= newx)
        error.reg[[te]][['error']] <- error
        error.reg[[te]][['pred']] <- preds
        error.reg[[te]][['fit']] <- fit   
        #Confidence intervals
        fitted.values = preds[,1]
        pred.lower = preds[,2]
        pred.upper = preds[,3]
        
        title <- paste0(te, " \n ", round(summary(fit)$r.squared, 3))
        plot(x,y, main=title, col="#00000050", xlab="Ground-truth", ylab="Prediction")
        polygon(c(rev(newx$x), newx$x), c(rev(pred.upper), pred.lower), col = '#dc021880', border = NA)
        abline(a = 0, b = 1, col="blue", lwd=2)
        abline(fit, lwd=2, lty=2, col="red")
      }
      remove(te, estimation, truth, fit, title, preds, error)
      dev.off()
      
      # Save the data as an RData object
      save(error.reg, file=paste0("~/Desktop/models/errors-",P_specie,"-",type_name,"-",repetitions,".RData"))
      
      
      
      
      #-------------------------------------------------------------------
      # LOOK AT THE ERROR DISTRIBUTIONS
      #-------------------------------------------------------------------
      
      plots <- list()
      for(te in to_est){
        temp <- data.frame(val=results[[te]], error=(errors[[te]]/results[[te]])*100)
        #temp <- temp[!is.na(temp$val),]
        temp$ind <- c(1:nrow(temp))
        g <- ggplot(temp, aes(x=val, y=0)) + 
          geom_errorbar(aes(ymin=-error, ymax=+error), width=0, alpha=.3) +
          geom_point(alpha=.5) +
          ggtitle(te) + 
          theme_classic()
        plots[[(length(plots) + 1)]] = g
      }
      
      # Set up the page
      grid.newpage()
      layout <- matrix(seq(1, 16) ,ncol = 4, nrow = 4)
      pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
      
      g <- arrangeGrob(plots[[1]], plots[[2]], plots[[3]],plots[[4]], 
                       plots[[5]], plots[[6]],plots[[7]], plots[[8]],
                       plots[[9]], plots[[10]],plots[[11]], plots[[12]],
                       plots[[13]], plots[[14]],plots[[15]], plots[[16]],  nrow=4)
      ggsave(paste0("outputs/rf_errors-",P_specie,"-",type_name,"-",repetitions,".pdf"), g)
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
    
  }
}
#------------------------------------------------------------------------

# Print the results
diff = Sys.time() - t
print(paste((repetitions*length(P_type_range) * length(P_specie_range))," root systems were generated and analysed in ",diff," seconds"), quote=F)


#------------------------------------------------------------------------


# References

# Pagès et al. (2013). 
# Calibration and evaluation of ArchiSimple, a simple model of root system architecture, 
# 290, 76–84. doi:10.1016/j.ecolmodel.2013.11.014
