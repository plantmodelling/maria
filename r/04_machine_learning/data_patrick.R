

data <- fread("model/2015-03-17_root_data.csv", header = T)
data$image <- as.character(data$image)
data$image <- substr(data$image, start = 0, stop = nchar(data$image)-5)

# Load the estimator data
ests <- fread("model/2015-03-17_root_estimators.csv", header=T)
ests$image <- as.character(ests$image)
ests$image <- substr(ests$image, start = 0, stop = nchar(ests$image)-9)

data <- data[,c("image", "tot_root_length"), with=F]

ests <- merge(ests, data, by="image")
setnames(ests, "tot_root_length", "to_fit")

# Get the index in each parameters combinaison
ests$image <- as.character(ests$image)
ests$rep <- substr(ests$image, start=nchar(ests$image)-3, stop =nchar(ests$image)-3)
ind <- unique(ests$rep)

test <- ests[ests$rep == 1,]
training <- ests[ests$rep != 1,]

test <- test[,2:42, with=F]
training <- training[,2:42, with=F]

write.csv(test, "~/Desktop/test-set.csv")
write.csv(training, "~/Desktop/training-set.csv")

