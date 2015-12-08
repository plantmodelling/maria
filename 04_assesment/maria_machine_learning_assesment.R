  setwd("~/Dropbox/research/projects/research/MARIA/maria_scripts/01_model_generator")
  source("maria_learning.r")  
  library(ggplot2)
  library(grid)
  PkgTest("randomForest")
  
  ##############################################################################################################################
  
  
  specie <- "monocot"
  type_name <- "2D"
  reps <- 10000
  
  #-------------------------------------------------------------------
  # SELECT THE RIGHT DATAFILES
  #-------------------------------------------------------------------
  
  lfiles <- list.files("../07_maria_shiny/data/")
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
  
  f1 <- files[files$specie == specie & files$type == type_name & files$size >= reps & files$file == "estimators",]
  to_load_ests <-  f1$name[f1$size == min(f1$size)]
  f1 <- files[files$specie == specie & files$type == type_name & files$size >= reps & files$file == "data",]
  to_load_data <-  f1$name[f1$size == min(f1$size)]
  
  descriptors <- read.csv(paste0("../07_maria_shiny/data/",to_load_ests))
  
  parameters <- read.csv(paste0("../07_maria_shiny/data/",to_load_data))
  colnames(parameters)[colnames(parameters) == "width"] <- "true_width"
  colnames(parameters)[colnames(parameters) == "depth"] <- "true_depth"
#   
#   # Add the simulation parameters to the set of variables to estimate
#   for(i in 1:nrow(parameters)){
#     args <- strsplit(as.character(parameters$image[i]), "-")[[1]]
#     parameters$P_penteVitDiam[i] <- args[3] #--------------
#     parameters$P_angInitMoyVertPrim[i] <- args[5] #--------------
#     parameters$P_intensiteTropisme[i] <- args[6] #--------------
#     parameters$P_propDiamRamif[i] <- args[7] #--------------
#     parameters$P_distRamif[i] <- args[8] #--------------
#     parameters$P_diamMax[i] <- args[10] #--------------
#     parameters$P_angLat[i] <- args[11]
#     parameters$P_maxLatAge[i] <- args[12]
#     parameters$P_duree[i] <- args[13]
#   }
#   for(i in 18:26) parameters[,i] <- as.numeric(parameters[,i])
  
  descriptors$image <- gsub(paste0("/data/guillaume/maria_scripts/01_model_generator/archisimple/outputs/",specie,"/",type_name,"/image/"), "", descriptors$image)
  descriptors$image <- gsub(paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",specie,"/",type_name,"/images/"), "", descriptors$image)
  descriptors$image <- gsub(".jpg", "", descriptors$image)
  descr_ind <- c(2:(ncol(descriptors))) # Indexes of the descriptors
  
  rs <- merge(descriptors, parameters, by = "image")
  rs <- rs[sample(1:nrow(rs), reps),] # If the dataset is bigger than wanted, downsize it
  
  cols <- colnames(rs)
  for(c in cols) rs <- rs[!is.nan(rs[[c]]),] # Remove the row containing  NA values
  for(c in cols) rs <- rs[!is.na(rs[[c]]),] # Remove the row containing  NA values
  
  remove(f1, descriptors, parameters, to_load_data, to_load_ests, i)
  
  
  #-------------------------------------------------------------------
  # PARAMETERS TO ESTIMATE
  #-------------------------------------------------------------------
  
  to_est <-c("tot_root_length","true_width","true_depth","direction",
             "n_primary","tot_prim_length","mean_prim_length","mean_prim_diameter",
             "mean_lat_density","n_laterals","tot_lat_length","mean_lat_length","mean_lat_diameter","mean_lat_angle",
             "tree_size","tree_magnitude","tree_altitude"
              )
  if(specie == "dicot") to_est <- to_est[-5] 
  to_est_ind <- match(to_est, colnames(rs))
  descr_ind <- c(descr_ind, to_est_ind)
  
  #-------------------------------------------------------------------
  # RUN THE MODEL
  #-------------------------------------------------------------------
  
  
  vec.models <- c(5,20,200)
  vec.trees <- c(5,20,200)
  
  models <- GenerateModels(fname = NULL, 
                           mat.data = rs, 
                           vec.models = vec.models, 
                           vec.trees = vec.trees, 
                           vec.f = to_est_ind, 
                           vec.p = descr_ind)
  

  save(models, file=paste0("~/Desktop/models/rfmodels-",specie,"-",type_name,"-",reps,".RData"))
  
  for(w in c(0.5)){
        message(paste0("Using weight of ",w))
        vec.weights <- rep(w, length(to_est))
        model <- SelectModel(models, vec.weights)
        save(model, file=paste0("~/Desktop/models/model-",specie,"-",type_name,"-",reps,"-",w,".RData"))
  }   
  
  
  
  #-------------------------------------------------------------------
  #-------------------------------------------------------------------
  # TEST THE MODELS WITH A DIFFERENT DATASET
  #-------------------------------------------------------------------
  #-------------------------------------------------------------------
  
  #-------------------------------------------------------------------
  # LOAD AND PREPARE THE DATASETS
  #-------------------------------------------------------------------
  reps2 <- 2000
  
  f1 <- files[files$specie == specie & files$type == type_name & files$size >= reps2 & files$file == "estimators",]
  to_load_ests <-  f1$name[f1$size == min(f1$size)]
  f1 <- files[files$specie == specie & files$type == type_name & files$size >= reps2 & files$file == "data",]
  to_load_data <-  f1$name[f1$size == min(f1$size)]
  
  descriptors1 <- read.csv(paste0("../07_maria_shiny/data/",to_load_ests))
  dlength <- ncol(descriptors1)
  
  parameters1 <- read.csv(paste0("../07_maria_shiny/data/",to_load_data))
  colnames(parameters1)[colnames(parameters1) == "width"] <- "true_width"
  colnames(parameters1)[colnames(parameters1) == "depth"] <- "true_depth"
  descriptors1$image <- gsub(paste0("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/",specie,"/",type_name,"/images/"), "", descriptors1$image)
  descriptors1$image <- gsub(paste0("/data/guillaume/maria_scripts/01_model_generator/archisimple/outputs/",specie,"/",type_name,"/image/"), "", descriptors1$image)
  descriptors1$image <- gsub(".jpg", "", descriptors1$image)
  vars <- colnames(descriptors1)[-1]
  
  # Add the simulation parameters to the set of variables to estimate
  for(i in 1:nrow(parameters1)){
    args <- strsplit(as.character(parameters1$image[i]), "-")[[1]]
    parameters1$P_penteVitDiam[i] <- args[3] #--------------
    parameters1$P_angInitMoyVertPrim[i] <- args[5] #--------------
    parameters1$P_intensiteTropisme[i] <- args[6] #--------------
    parameters1$P_propDiamRamif[i] <- args[7] #--------------
    parameters1$P_distRamif[i] <- args[8] #--------------
    parameters1$P_diamMax[i] <- args[10] #--------------
    parameters1$P_angLat[i] <- args[11]
    parameters1$P_maxLatAge[i] <- args[12]
    parameters1$P_duree[i] <- args[13]
  }
  for(i in 18:28) parameters1[,i] <- as.numeric(parameters1[,i]) 
  
  rs1 <- merge(descriptors1, parameters1, by = "image")
  rs1 <- rs1[sample(1:nrow(rs1),reps2),]
  rs1[,2:ncol(rs1)] <- PredictNAvalues(rs1[,2:ncol(rs1)])
  
  remove(f1, to_load_data, to_load_ests, parameters1, descriptors1, i)
  
  
  #-------------------------------------------------------------------
  # GET THE ESTIMATIONS FROM THE MODEL AND THE EXPECTED ERROR
  #-------------------------------------------------------------------
  w <- 0.5
  load(paste0("~/Desktop/models/model-",specie,"-",type_name,"-",reps,"-",w,".RData")) 
  load(paste0("~/Desktop/models/rfmodels-",specie,"-",type_name,"-",reps,".RData")) 
  
  results <- PredictRFs(model, rs1[,c(2:dlength)])
  errors <- GeneralPointErrorEstimate(lst.rfModels = model, 
                                      mat.data = rs1[,c(2:ncol(rs1))], 
                                      vec.f = models$ResponseVar)
  
  Plot(vec.x = rs1$tot_root_length, vec.y = results$tot_root_length, 
       vec.errors = errors$tot_root_length, type="b")
  
  #-------------------------------------------------------------------
  # COMPARE THE MODELS WITH THE BEST CORRESPONDING  SINGLE LINEAR ESTIMATION
  #-------------------------------------------------------------------
  
  metaPrec <- data.frame(var1=character(), var2=character(), r1=numeric(), r2=numeric(), type=character())
  
  # Look at the quality of the estimations
  for(te in to_est){
    # Get the best single variable
    max <- 0
    keep <- ""
    for(v in vars){
      fit <- lm(rs1[[te]] ~ rs1[[v]])
      r2 <- summary(fit)$r.squared
      if(r2 > max){
        max <- r2
        keep <- v
      }
    }
    
    # TODO
    
    
    # Compare it to the Random Forest
    estimation <- results[[te]] 
    estimation2 <- rs1[[keep]]
    truth <- rs1[[te]]
    
    diff <- abs(mean(estimation, na.rm = T) /mean(truth, na.rm = T))
    if (diff > 1) diff <- abs(mean(truth, na.rm=T) /mean(estimation, na.rm=T))
    diff2 <- abs(mean(estimation2, na.rm=T) /mean(truth, na.rm=T))
    if (diff2 > 1) diff2 <- abs(mean(truth, na.rm=T) /mean(estimation2, na.rm=T))
    
    fit <- lm(truth ~ estimation)
    fit2 <- lm(truth ~ estimation2)
    
    temp <- data.frame(var1=te, var2=keep,
                           r1=summary(fit)$r.squared, r2=summary(fit2)$r.squared, 
                           type="r-squared")
    metaPrec <- rbind(metaPrec, temp)
    temp <- data.frame(var1=te, var2=keep,
                       r1=diff, r2=diff2, 
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
    theme(axis.text.x=element_blank(),axis.title.x=element_blank(), legend.position="none")
  
  remove(temp, fit, fit2, diff, diff2, estimation, estimation2, truth)
  remove(v, keep, max, r2, te)
  
  #-------------------------------------------------------------------
  # PLOT THE REGRESSIONS BETWEEN THE GROUND-TRUTH AND THE MODEL VALUES
  #-------------------------------------------------------------------
  
  par(mfrow=c(4,4), mar=c(4,4,4,3))
  for(te in to_est){
    estimation <- results[[te]] 
    truth <- rs1[[te]]
    #diff <- abs(mean(estimation, na.rm=T) /mean(truth, na.rm=T))
    #if(diff != 1){
      fit <- lm(estimation ~ truth)
      title <- paste0(te, " \n ", round(summary(fit)$r.squared, 3))
      plot(truth, estimation, main = title, col="#00000050")
      abline(a = 0, b = 1, col="blue", lwd=2)
      abline(fit, lwd=2, lty=2, col="red")
    #}
  }
  remove(te, estimation, truth, diff, fit, title)
  
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
  
  # Make each plot, in the correct location
  for (i in 1:length(plots)) {
    # Get the i,j matrix positions of the regions that contain this subplot
    matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
    
    print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                    layout.pos.col = matchidx$col))
  }
  

        
        
        