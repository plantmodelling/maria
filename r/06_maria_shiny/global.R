
# Global libraries
  library("shiny")
  library("pdist")
  library("data.table")  


# Global functions
  min.index <- function(x){which(x == min(x))[1]}
  min.value <- function(x){min(x)[1]}
  
  
  
# Global datasets

  pred <- NULL;
  
  # Color parameters
  col.distri <- "#99bcdd80"
  col.error <- "#c44d5880"
  col.failed <- "#c02942"
  col.passed <- "#4ecdc4"
  col.out <- "#c0294250"
  
  param_error <- 0;
  
  load("data/maria.RData")
  
#   estim <- read.csv("data/estimators.csv")
#   estim <- as.character(estim$x)
# 
#   matching <- read.csv("data/matching.csv")
#   matching <- as.numeric(matching$x)
# 
#   weights <- read.csv("data/weighting.csv")
#   
#   data <- read.csv("data/root_data.csv")
#   if(colnames(data)[1] == "X") data <- data[,2:ncol(data)]
#   data$image <- as.character(data$image)
# #   data$image <- substr(data$image, start = 1, stop = nchar(data$image)-5)
#   
#   ests <- read.csv("data/root_estimators.csv")
#   if(colnames(ests)[1] == "X") ests <- ests[,2:ncol(ests)]
#   ests$image <- as.character(ests$image)
# #   ests$image <- substr(ests$image, start = 1, stop = nchar(ests$image)-9)
#   
#   params <- colnames(data)
#   params <- params[3:length(params)]

  
 # Functions
  
  # Re-generate a root image using Archisimple.cpp and RSML_reader.jar
  render_roots <- function(arg){
    
    P_duree <- 20
    P_simultEmiss <- 0 #Rajout d'un switch (1 = oui ; 0 = non) pour la modification permettant l'emission de 3 racines seminales simultan?ment 3 jours apr?s la primaire
    P_vitEmissionPrim <- 0.8 #--------------
    P_nbSeminales <- 10 # Rajout nombre de seminales, utile uniquement si P_simultEmiss est active
    P_ageEmissionTard <- 14
    P_dBaseMaxTard <- 10
    P_vitEmissionTard <- 2
    P_propDiamTard <- 0.6
    P_nbMaxTard  <-  10
    P_diamMin  <-  0.14 #--------------
    P_tendanceDirTropisme <- 1
    P_intensiteTropisme <- 0.01 #--------------
    P_ageMaturitePointe <- 1
    P_distRamif <- 5 #--------------
    P_propDiamRamif <- 0.4 #--------------
    P_coeffVarDiamRamif <- 0.6 #--------------
    P_probaMaxArret <- 0.4
    P_probaEffetDiam <- 1
    P_TMD <- 0.1
    P_penteDureeVieDiamTMD <- 3000
    P_coeffCroissRad <- 0
    P_angLat <- 1.3
    P_arabido <- 0
    P_tertiary <- 0
    name <- ""
    P_sacrifice <- 0 #Switch on/off (1 = oui ; 0 = non) pour la fonction de sacrifice de racine
    P_condemnedRoot <- 2
    P_sacrificeTime <- 15
    P_nbMaxPrim <- 40 #-------------
    

      args <- strsplit(arg, "-")[[1]]
      
      
      P_penteVitDiam <- args[2] #--------------
      P_vitEmissionPrim <- args[3] #--------------
      P_intensiteTropisme <- args[4] #--------------
      P_propDiamRamif <- args[5] #--------------
      P_distRamif <- args[6] #--------------
      P_angLat <- args[7]
      P_diamMax <- args[8] #--------------
      P_duree <- args[10]
      
      var <- c(P_duree,
                       P_simultEmiss,
                       P_vitEmissionPrim,
                       P_nbSeminales,
                       P_nbMaxPrim,
                       P_ageEmissionTard,
                       P_dBaseMaxTard,
                       P_vitEmissionTard,
                       P_propDiamTard,
                       P_nbMaxTard,
                       P_diamMin,
                       P_diamMax,
                       P_penteVitDiam,
                       P_tendanceDirTropisme,
                       P_intensiteTropisme,
                       P_ageMaturitePointe,
                       P_distRamif,
                       P_propDiamRamif,
                       P_coeffVarDiamRamif,
                       P_probaMaxArret,
                       P_probaEffetDiam,
                       P_TMD,
                       P_penteDureeVieDiamTMD,
                       P_coeffCroissRad,
                       P_angLat,
                       P_arabido,
                       P_tertiary,
                       P_sacrifice,
                       P_condemnedRoot,
                       P_sacrificeTime
          )
      cat(var, file=paste("model/param.txt",sep=""), sep='\n')
      cur <- getwd()
      setwd(dir = "model")
      system("./ArchiSimp5Maria")
      setwd(cur)
      system('java -jar model/RSML_reader.jar "model"')
  }
