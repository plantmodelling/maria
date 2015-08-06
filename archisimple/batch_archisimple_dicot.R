setwd("/Users/guillaumelobet/Desktop/archisimple/")

P_duree <- 20
P_simultEmiss <- 0 #Rajout d'un switch (1 = oui ; 0 = non) pour la modification permettant l'emission de 3 racines seminales simultan?ment 3 jours apr?s la primaire
P_vitEmissionPrim <- 0.8 #--------------
P_nbSeminales <- 1 # Rajout nombre de seminales, utile uniquement si P_simultEmiss est active
P_ageEmissionTard <- 14
P_dBaseMaxTard <- 10
P_vitEmissionTard <- 2
P_propDiamTard <- 0.6
P_nbMaxTard  <-  10
P_diamMin  <-  0.01 #--------------
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
basename <- "outputs/rsml/sim"
P_sacrifice <- 0 #Switch on/off (1 = oui ; 0 = non) pour la fonction de sacrifice de racine
P_condemnedRoot <- 2
P_sacrificeTime <- 15
P_nbMaxPrim <- 1 #-------------


P_penteVitDiam <- 25 #--------------
P_vitEmissionPrim <- 1 #--------------
P_intensiteTropisme <- 0.1 #--------------
P_propDiamRamif <- 0.9 #--------------
P_distRamif <- 3 #--------------
P_angLat <- 1.3
P_diamMax <- 0.04 #--------------


# Loop over the parameters space

counter <- 0
t <- Sys.time()

#  for(P_penteVitDiam in seq(from=15, to=15, by=2)){
#   
#   for(P_vitEmissionPrim in seq(from=1, to=1, by=1)){
#     
#     for(P_intensiteTropisme in seq(from=0.01, to=0.05, by=0.01)){
#       
#        for(P_propDiamRamif in seq(from=0.2, to=0.6, by=0.2)){
#                   
#            for(P_distRamif in seq(from=3, to=6, by=1)){
#             
#              for(P_angLat in seq(from=0.5, to=1.5, by=1)){
#               
#                 for(P_diamMax in seq(from=0.15, to=0.16, by=0.01)){
                 
                 for(i in 1:3){
                  
                  name <- paste(basename, "-", P_penteVitDiam,"-", P_vitEmissionPrim, "-", P_intensiteTropisme, "-", P_propDiamRamif, "-", P_distRamif ,"-", P_angLat, "-", P_diamMax , "-", i, sep="")
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
                           name,
                           P_sacrifice,
                           P_condemnedRoot,
                           P_sacrificeTime
                  )
                  cat(var, file=paste("param.txt",sep=""), sep='\n')
                  system("./ArchiSimp5-2D")
                  counter <- counter+1
#                 }
#               }
#           }
#         }
#       }
#     }
#   }
}

# Save the images
system('java -Xmx4000m -jar RSML_reader.jar "outputs/rsml/" "outputs/images/" "outputs/root_data.csv"')
system('java -Xmx4000m -jar MARIA.jar "outputs/images/" "outputs/root_estimators.csv" "30"')
diff = Sys.time() - t
print(paste(counter," root systems were generated in ",diff," seconds"), quote=F)

