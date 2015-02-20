
setwd("model/")

synth.est <- read.csv("output-MARIA-without1.csv", sep=";")
synth.est <-synth.est[,1:20]
synth.real <- read.csv("root_data.csv", sep=";")

synth.real$image <- as.character(synth.real$image)
synth.est$image <- as.character(synth.est$image)
synth.real$image <- substr(synth.real$image, start = 0, stop = nchar(synth.real$image)-5)
synth.est$image <- substr(synth.est$image, start = 0, stop = nchar(synth.est$image)-1)
synth.est$image <- paste(synth.est$image, "-", synth.est$Rep, sep="")

synth <- merge(synth.est, synth.real, by = "image", all=F)

ests <- colnames(synth.est)
ests <- ests[5:length(ests)]
n.ests <- length(ests)

real <- colnames(synth.real)
real <- real[3:length(real)]
n.real <- length(real)

col <- 3
row <- 4
par(mfrow=c(row,col), mar=c(1, 3, 3, 1))
for(i in 1:n.real){
  plot(sort(synth[[real[i]]]), main=real[i], axes=F, pch=20)
  axis(2)
}

col <- 3
row <- 4
par(mfrow=c(row,col), mar=c(1, 3, 3, 1))
for(i in 1:n.ests){
  plot(sort(synth[[ests[i]]]), main=ests[i], axes=F, pch=20)
  axis(2)
}


par(mfrow=c(row,col), mar=c(4, 4, 3, 1))
for(r in real){
  for(e in ests){
    fit <- lm(synth[[r]] ~ synth[[e]])
    if(summary(fit)$r.squared > 0.8){
      plot(synth[[e]], synth[[r]], ylab=r, xlab=e, main=round(summary(fit)$r.squared, 2))
      abline(fit)
    }
  }
}

