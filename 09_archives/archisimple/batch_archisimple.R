
setwd("/Users/guillaumelobet/Desktop/archisimple/")

P_duree <- 20
P_vitEmissionPrim <- 0.2 #--------------
P_nbMaxPrim <- 40 #--------------
P_ageEmissionTard <- 14
P_dBaseMaxTard <- 10
P_vitEmissionTard <- 2
P_propDiamTard <- 0.6
P_nbMaxTard  <-  10
P_diamMin  <-  0.04 #--------------
P_diamMax <- 0.8 #--------------
P_penteVitDiam <- 20 #--------------
P_tendanceDirTropisme <- 1
P_intensiteTropisme <- 0.01 #--------------
P_ageMaturitePointe <- 1
P_distRamif <- 0.1
P_propDiamRamif <- 0.4 #--------------
P_coeffVarDiamRamif <- 0.3 #--------------
P_probaMaxArret <- 0.4
P_probaEffetDiam <- 1
P_TMD <- 0.1
P_penteDureeVieDiamTMD <- 3000
P_coeffCroissRad <- 0
P_angLat <- 1.3
P_arabido <- 0
P_tertiary <- 0
name <- ""
basename <- "outputs/sim"

counter <- 0

for(P_penteVitDiam in seq(from=10, to=30, by=10)){
  
  for(P_vitEmissionPrim in c(0.3, 1)){
    
  for(P_intensiteTropisme in c(0.05 , 0.15)){
    
    for(P_propDiamRamif in seq(from=0.2, to=0.4, by=0.1)){
                 
        for(P_duree in seq(from=20, to=20, by=5)){
            
          for(P_distRamif in seq(from=2, to=4, by=2)){
              
            for(P_angLat in seq(from=0.5, to=1, by=0.5)){
              
              for(P_diamMax in seq(from=0.6, to=1, by=0.2)){
                
                for(i in 1:5){
             
                  t <- format(Sys.time(), "%H%M%OS3")
                  name <- paste(basename, "-", P_intensiteTropisme,"-", P_propDiamRamif, "-", P_penteVitDiam, "-", P_vitEmissionPrim ,"-", P_distRamif, "-", P_angLat, "-", P_diamMax, "-", P_duree , "-", i, ".rsml", sep="")
                  var <- c(P_duree,
                           P_vitEmissionPrim,
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
                           name
                  )
                  ##system.time(for(i in 1:2e6) k = i+i)
                  cat(var, file=paste("param.txt",sep=""), sep='\n')
                  system("/Users/guillaumelobet/Desktop/archisimple/ArchiSimp5")
                  counter <- counter+1
                }
              }
            }
          }
        }
      }
    }
  }
}

print(paste(counter," root systems were generated"), quote=F)

# path <- "/Users/guillaumelobet/Dropbox/research/projects/research/rootGuess/scripts/archisimple/outputs/sim-50.rsml"
# pl <- rsmlToPlant(path, threed = T)
# plot(pl, threed = T)
