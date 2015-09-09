

# Parameters
library("pdist")
scaling = T
weighting = T

# Load the ground truth data
real <- read.csv("model/2015-03-05_root_data.csv", sep=",")
real$image <- as.character(real$image)
real$image <- substr(real$image, start = 0, stop = nchar(real$image)-5)

# Load the estimator data
ests <- read.csv("model/2015-03-05_root_estimators.csv", sep=",")
ests$image <- as.character(ests$image)
ests$image <- substr(ests$image, start = 0, stop = nchar(ests$image)-9)


# Get the names of the columns
gt <- colnames(real)[3:length(colnames(real))]
es <- colnames(ests)[2:length(colnames(ests))]

# Get the correlations between the esitmators and the ground truth data
we <- data.frame(gt)
i <- 1
for(g in gt){
  for(e in es){
    fit <- lm(ests[[e]] ~ real[[g]])
    we[[e]][[i]] <- summary(fit)$r.squared
  }
  i <- i+1
}

# scale the data
if(scaling) ests[,2:ncol(ests)] <- scale(ests[,2:ncol(ests)])

# Weight the data
if(weighting){
  for(e in es){  
    ests[[e]] <- ests[[e]] * mean(we[[e]])
  }
}


# Get the index in each parameters combinaison
ests$image <- as.character(ests$image)
ests$rep <- substr(ests$image, start=nchar(ests$image)-3, stop =nchar(ests$image)-3)

ind <- unique(ests$rep)

# Here we loop over the data in order to make multiple sample (based in the index value)
pred = NULL
for(i in ind){
  
  # get the test and base data
  test <- ests[ests$rep == i, ]
  base <- ests[ests$rep != i, ]

  # Get the number of observations
  nvar <- ncol(test)

  # Get the distance matrix
  distances <- as.matrix(pdist(base[,2:(nvar-1)], test[,2:(nvar-1)]))
  
  # Add the image names to the matrix
  distMat <- matrix(nrow=nrow(base),ncol=nrow(test)+1)
  distMat[,1] <- base$image
  distMat[,2:(nrow(test)+1)] <- as.matrix(distances) 
  
  # Find the closest match for each test image
  for(j in 1:nrow(test)){
    temp <- as.numeric(distMat[,j+1])
    min <- min(temp)
    x <- distMat[,1][distMat[,j+1] == min]
    test$match[j] <- base$image[base$image == x]
    test$dist[j] <- min 
    test$med[j] <- median(temp)
    test$mean[j] <- mean(temp)
  }
  
  # Save the distance data (test image + match)
  pred <- rbind(pred, test[c("match", "image", "dist", "med", "mean")])
  remove(distances)
  remove(distMat)
}


# Get the parameters for the test and the matches
obs <- merge(pred, ests, by.x="match", by.y="image")
obs <- merge(obs, ests, by.x="image", by.y="image")

# Get the ground truth for the test and the matches
pred <- merge(pred, real, by.x="match", by.y="image")
pred <- merge(pred, real, by.x="image", by.y="image")


# Plot the correlation between ground truth values
pred <- pred[pred$tot_root_length.y < 30000,]
par(mfrow=c(3,4))
for(v in gt){
  x <- pred[[paste(v,".y", sep="")]]
  y <- pred[[paste(v,".x", sep="")]]
  fit <- lm(y ~ x) 
  # predicts + interval
  newx <- seq(0, max(x)*2, length.out=100)
  preds <- predict(fit, newdata = data.frame(x=newx), interval = 'confidence', level=0.99)
  
  plot(y, x, col="#00000030", type='n', pch=20, main=paste(v," \n r2=", round(summary(fit)$r.squared,3)), ylab="ground-truth", xlab="prediction")
  polygon(c(rev(newx), newx), c(rev(preds[ ,3]), preds[ ,2]), col = '#ff8e2d80', border = NA)
  abline(fit, lwd=2)
  points(y, x, col="#00000030", pch=20)
  abline(a = 0, b=1, lty=2, lwd=2, col="#00000060")
}

# Correlation between estimators
par(mfrow=c(2,2))
var <- c("area", "width", "depth", "length")
for(v in var){
  fit <- lm(obs[[paste(v,".x", sep="")]] ~ obs[[paste(v,".y", sep="")]])
  
  
  plot(obs[[paste(v,".y", sep="")]], obs[[paste(v,".x", sep="")]], 
       col="#00000050", pch=20,
       main=paste(v, round(summary(fit)$r.squared,2)))
  abline(fit, lwd=2)
  abline(a = 0, b=1, lty=2, lwd=2, col="#00000060")
}

par(mfrow=c(1,1))
hist(pred$dist, breaks=100)

