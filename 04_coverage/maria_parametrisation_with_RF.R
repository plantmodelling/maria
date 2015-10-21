

# Parameters
library("pdist")
library("data.table")

min.index <- function(x){which(x == min(x))[1]}
min.value <- function(x){min(x)[1]}


# Load the ground truth data
#data <- fread("../07_maria_shiny/data/root_data.csv", header = T)
data <- fread("~/Desktop/data.csv", header = T)
data$image <- as.character(data$image)
data$image <- substr(data$image, start = 0, stop = nchar(data$image)-5)

# Load the estimator data
#ests <- fread("../07_maria_shiny/data/root_estimators.csv", header=T)
ests <- fread("~/Desktop/estimators.csv", header=T)
ests$image <- as.character(ests$image)
ests$image <- substr(ests$image, start = 0, stop = nchar(ests$image)-9)

diff <- nrow(ests) - nrow(data)
if(diff < 0) data <- data[0:nrow(ests),]
if(diff > 0) ests <- ests[0:nrow(data),]

ests.original <- ests

image <- ests[,image]

# Get the names of the columns
ground_truths <- colnames(data)[3:(length(colnames(data))-4)]
estimators <- colnames(ests)[3:length(colnames(ests))]

# Plot the distribution of the ground truth data
# par(mfrow=c(3,4))
# for(g in ground_truths){
#   hist(data[[g]], breaks=20, col="darkgrey", border="white", main=g, freq=F)
#   lines(density(data[[g]]), lwd=2, col="red")
# }


# Get the correlations between the esitmators and the ground truth data
weights <- data.frame(ground_truths)
i <- 1
par(mfrow=c(4,4))
for(g in ground_truths){
  for(e in estimators){
    fit <- lm(ests[[e]] ~ data[[g]])
    weights[[e]][[i]] <- summary(fit)$r.squared
    if(summary(fit)$r.squared > 0.85){
      plot(data[[g]], ests[[e]], main=round(summary(fit)$r.squared,2),xlab=g,ylab=e)
      abline(a=0,b=1, col="red",lwd=3)
    }
  }
  i <- i+1
}

# scale the data
ests[,3:ncol(ests)] = data.table(scale(ests[, 3:ncol(ests), with=F]))

# Weight the data
for(e in estimators) ests[[e]] <- ests[[e]] * mean(weights[[e]])
remove(e, g, i, fit) 

# Get the index in each parameters combinaison
ests$image <- as.character(ests$image)
#ests$rep <- substr(ests$image, start=nchar(ests$image)-3, stop =nchar(ests$image)-3)
ests$rep <- c(1:nrow(ests))
ind <- unique(ests$rep)

# Here we loop over the data in order to make multiple sample (based in the index value)
t1 <- Sys.time()

pred = NULL
for(i in ind){
  
  # get the test and base data
  test <- ests[ests$rep == i, ]
  base <- ests[ests$rep != i, ]
  
  # Get the distance matrix
  distances <- as.matrix(pdist(base[,estimators, with=F], test[,estimators, with=F]))
  test$match <- base$image[apply(distances, 2, min.index)]
  test$dist <- apply(distances, 2, min.value)
  
  # Save the distance data (test image + match)
  pred <- rbind(pred, test[,c("match", "image", "dist"), with=F])
}

t2 <- Sys.time()-t1
message(paste("Time for distance computation (",nrow(ests), ") = ",t2))
remove(i, t1, t2, ind, test, base, distances)

ests <- ests.original

setnames(data,'image','match')
setkey(pred, match)
setkey(data, match)
pred$match <- gsub("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/", "", pred$match)
pred$image <- gsub("/Users/guillaumelobet/Desktop/Work/archisimple/outputs/images/", "", pred$image)
pred <- merge(pred, data, by = "match")
setnames(data,'match','image')
setkey(pred, image)
setkey(data, image)
pred <- merge(pred, data, by="image")

# Plot the correlation between ground truth values
#pred <- pred[pred$tot_root_length.y < 30000,]

# Create list containing the error terms
model <- list()

ground_truths <- colnames(data)[3:(length(colnames(data)))]
ground_truths <- ground_truths[-5]

par(mfrow=c(3,4))
for(v in ground_truths){
  x <- pred[[paste(v,".x", sep="")]]  
  y <- pred[[paste(v,".y", sep="")]]
  
  x[is.nan(x)] <- 0
  y[is.nan(y)] <- 0
  
  fit <- lm(y ~ x) 
  # predicts + interval
  newx <- seq(0, max(x), length.out=500)
  preds <- predict(fit, newdata = data.frame(x=newx), interval = 'prediction', level=0.999)
  error <- data.frame("value"=((preds[,1] - preds[,2]) / max(preds[,1]))*100, "x"= newx)
  model[[v]][['error']] <- error
  model[[v]][['pred']] <- preds
  model[[v]][['fit']] <- fit
  
  plot(x, y, col="#00000030", type='n', pch=20, 
       main=paste(v," \n r2=", round(summary(fit)$r.squared,3)), 
       ylab="ground-truth", xlab="prediction")
  points(x, y, col="#00000010", pch=20)
  polygon(c(rev(newx), newx), c(rev(preds[ ,3]), preds[ ,2]), col = '#dc021880', border = NA)
  abline(fit, lwd=2)
  abline(a = 0, b=1, lty=2, lwd=2, col="#00000060")
}
remove(fit, preds, newx, x, y, v)

par(mfrow=c(1,1))
hist(pred$dist, breaks=100, col="#00000090", border="white", freq=F)
lines(density(pred$dist), lwd=2, col="red")

# Save the data as an RData object
save.image("~/Dropbox/research/projects/research/MARIA/maria_scripts/r/maria_shiny/data/maria.RData")


