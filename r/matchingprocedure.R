# Set working directory
setwd("C:/Iko/Studie/Master Environmental biology/Stage2/Model Assisted phenotyping/Models5")

## Open files descriptors
Data <- read.csv("output_Dat.csv", sep = ";")
Test <- read.csv("output_test.csv", sep = ";")
Weights <- read.csv("weights2.csv", sep = ";")
ScaleData <- rbind(Test, Data)
countcolData = length(colnames(ScaleData))
Tot = length(rownames(ScaleData))
ScaleData[,5:countcolData] <- scale(ScaleData[,5:countcolData])
for(i in 5:(countcolData)){
  
  ScaleData[,i] <- ScaleData[,i] * Weights[4,i-3]
  
}
TestList <- split(ScaleData, as.factor(ScaleData$Is))
data <- unsplit(TestList[2], 1)
test <- unsplit(TestList[1], 1)
countcomp = length(rownames(test))
countrow = length(rownames(data))

##Make Distance Matrix
library("pdist", lib.loc="~/R/win-library/3.0")
distnm <- matrix(nrow=countrow,ncol=(countcomp+1))
distnm[,1] <- data$X
distance <- pdist(data[,5:countcolData], test[,5:countcolData])
dinstancematrix <- as.matrix(distance)
distnm[,2:(countcomp+1)] <- dinstancematrix

#Open files with real data
Base <- read.csv("root_data_match.csv", sep = ";")
Base_test <- read.csv("root_data_test.csv", sep = ";")
ScaleData2 <- rbind(Base_test, Base)
countcolBase = length(colnames(ScaleData2))
Tot2 = length(rownames(ScaleData2))
ScaleData2[,5:countcolBase] <- scale(ScaleData2[,5:countcolBase])
TestList2 <- split(ScaleData2, as.factor(ScaleData2$Is))
base <- unsplit(TestList2[2], 1)
basetest <- unsplit(TestList2[1], 1)
countcomp2 = length(rownames(basetest))
countrow2 = length(rownames(base))

##Make Distance Matrix real data
library("pdist", lib.loc="~/R/win-library/3.0")
distnm2 <- matrix(nrow=countrow2,ncol=(countcomp2+1))
distnm2[,1] <- base$X
distance2 <- pdist(base[,5:countcolBase], basetest[,5:countcolBase])
dinstancematrix2 <- as.matrix(distance2)
distnm2[,2:(countcomp2+1)] <- dinstancematrix2

##Print Closest match in matrix
Matches <- matrix(nrow = countcomp, ncol=8)
Matches[,1] <- as.character(test[,2])
Matches[,2] <- test$X

library("data.table", lib.loc="~/R/win-library/3.0")
R1=0
R2=0
R3=0
tot =0
Count1 = 0
Count2 = 0
Count3 = 0
Count4 = 0
Value = 0
Rank1 =0
Rank2 = 0
Rank3 = 0
Rank4 = 0
for (i in 1:countcomp){
  min1 <- order(distnm[,i+1])[1]
  min2 <- order(distnm[,i+1])[2]
  min3 <- order(distnm[,i+1])[3]
  
  Matches[i,3] <- data$X[min1]
  Matches[i,4] <- data$X[min2]
  Matches[i,5] <- data$X[min3]
  tot <- tot+1
  No <- as.integer(Matches[i,2])+5
  
  ED <- distnm2[min1,i+1]
  Matches[i,7] <- ED
  distnm2 <- distnm2[order(distnm2[,i+1]),]
  RA <- which(distnm2[,1] == Matches[i,3])
  RB<- which(distnm2[,1] == Matches[i,4])
  RC <- which(distnm2[,1] == Matches[i,5])
  Matches[i,8] <- RA

  if(as.integer(Matches[i,3])<No && Matches[i,3]> Matches[i,2]){R1 <- R1+1
                                                   Matches[i,6] <- 1
                                                   Rank1 <- Rank1+RA
                                                   Count1 <- Count1+1}
  else if(as.integer(Matches[i,4])<No && Matches[i,4]> Matches[i,2]){R2 <- R2+1
                                                       Matches[i,6] <- 2
                                                       Rank2 <- Rank2+RB
                                                       Count2 <- Count2+1}
  else if(as.integer(Matches[i,5])<No && Matches[i,5]> Matches[i,2]){R3 <- R3+1
                                                       Matches[i,6] <- 3
                                                       Rank3 <- Rank3+RC
                                                       Count3 <- Count3+1}
  else {Matches[i,6] <- NA
        Value <- Value+ED
        Rank4 <- Rank4+RA
        Count4 <- Count4+1}
  
  
}

##Print how many matches
text <- "out of"
text1 <- "match as first"
text2 <- "match as second"
text3 <- "match as third"

R2b<- R1+R2
R3b<- R1+R2+R3
R1a <- as.double((R1/tot)*100)
R2a <- as.double(R2b/tot*100)
R3a <- as.double (R3b/tot*100)


A <- paste(R1, text, tot, text1, "(", R1a, "percent)")
B <- paste(R2b, text, tot, text2,"(", R2a, "percent)")
C <- paste(R3b, text, tot, text3, "(", R3a, "percent)")

#Print average distance of non-matches
dist <- Value/Count4
print(dist)
RankA <- Rank1/Count1
RankB <- Rank2/Count2
RankC <- Rank3/Count3
RankD <- Rank4/Count4

print(A)
print(B)
print(C)
print(dist)
print(RankA)
print(RankB)
print(RankC)
print(RankD)

