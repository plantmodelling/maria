# LOAD LIBRARIES
library(ggplot2)
require(gridExtra)

# LOAD DATA
data <- read.csv("~/Desktop/archisimple/outputs/root_data.csv")
est <- read.csv("~/Desktop/archisimple/outputs/root_estimators.csv")



est$type <- "2D"
est$type[grepl("3D", est$image)] <- "3D"
est$type[grepl("shov", est$image)] <- "shov"
est$type <- factor(est$type)

est$specie <- "monocot"
est$specie[grepl("dico", est$image)] <- "dicot"
est$specie <- factor(est$specie)

data$type <- "2D"
data$type[grepl("3D", data$image)] <- "3D"
data$type[grepl("shov", data$image)] <- "shov"
data$type <- factor(data$type)

data$specie <- "monocot"
data$specie[grepl("dico", data$image)] <- "dicot"
data$specie <- factor(data$specie)

# Look at the coverage of the synthetic population
#data <- data[data$type == "3D",]
#data$index <- as.numeric(factor(data$image))

vars <- colnames(data)[2:7]
types <- "3D"#unique(data$type)


for(t in types){
  for(v in vars){
    rs <- data.frame(var=sort(data[[v]][data$type == t]))
    rs$diff <- c(0, diff(rs$var))
    rs$index <- c(1:nrow(rs))
  
    plot1 <- ggplot(rs, aes(index, var), main = v) + 
      geom_point() +
      xlab("Image index [-]") +
      ylab(v) + 
      ggtitle(paste(t, " : ", v)) +
      theme_bw()

    plot2 <- ggplot(rs, aes(index, diff)) + 
      geom_point() +
      geom_abline(intercept=mean(rs$diff), slope=0, colour="red")  +
      xlab("Image index [-]") +
      ylab(paste(v, " difference")) + 
      ggtitle(paste("Mean diff : ", round(mean(rs$diff), 2))) +
      theme_bw()
  
    grid.arrange(plot1, plot2, ncol=2)
  }
}


