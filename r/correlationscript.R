#Set working directory to where the files are
setwd("C:/Iko/Studie/Master Environmental biology/Stage2/Model Assisted phenotyping/Models5")

#Read the files (pay notice to seperator)
Param <- read.csv("output-310115-MARIA.csv", sep = ";")
Data <- read.csv("root_data2.csv", sep = ";")

#Set up the matrix (pay attention to where the data starts (depending on your file))
countx= colnames(Param)
county= colnames(Data)
x=Param[,4:length(countx)]
y=Data[,4:length(county)]
listlength = length(x)*length(y)

output <- matrix(nrow=listlength, ncol=3)
labx <- colnames(x)
laby <- colnames(y)

##Make an excel file of the pairwise correlations
Corrtot <- cor(x, y, "pairwise.complete.obs") # get correlations 
library(xlsx)
write.xlsx(Corrtot, "Correlations2.xlsx") #Save correlations

##Loop to get a matrix with correlation and P values. Produce graphs of all correlations higher than 0.8.
for (i in 1:length(y) ){
  A <- i
  B <- (i*length(x)) - length(x)
  for(i in 1:length(x)){
    corr.value <- cor.test(x[,i], y[,A], method="p")
    output[(B+i),1] <- paste(laby[A], "*", labx[i])
    output[(B+i),2] <- corr.value$p.value
    output[(B+i),3] <- corr.value$estimate
    if (corr.value$estimate > 0.7){
    name <- paste(laby[A], labx[i], ".tiff")
    tiff(name)
    corr <- paste("r=", corr.value$estimate, ", p=", corr.value$p.value )
    plot(y[,A], x[,i], xlab=laby[A], ylab = labx[i], main = corr)
    dev.off()
    }
  } 

}

##produce graphs grouped by factor (showing correlation value)

for (i in 1:length(y) ){
  A <- i
  B <- (i*length(x)) - length(x)
  for(i in 1:length(x)){
    corr.value1 <- cor.test(~ x[,i] + y[,A], subset=Data[,2]==0.01)
    corr.value2 <- cor.test(~ x[,i] + y[,A], subset= Data[,2]==0.11)
    #corr.value3 <- cor.test(~ x[,i] + y[,A], subset= (Data$diameter.growth == 30))
    name <- paste(laby[A], labx[i], ".tiff")
    tiff(name)
    corr <- paste("r1=", corr.value1$estimate, ", r2=", corr.value2$estimate)
    plot(y[,A], x[,i], xlab=laby[A], ylab = labx[i], main = corr, col = as.factor(Data[,2]))
    dev.off()
  } 
}


