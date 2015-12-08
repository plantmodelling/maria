# LOAD LIBRARIES

library(ggplot2)
require(gridExtra)

# LOAD DATA
data <- read.csv("~/Dropbox/research/projects/research/MARIA/maria_scripts/07_maria_shiny/data/root_all_data.csv")
est <- read.csv("~/Dropbox/research/projects/research/MARIA/maria_scripts/07_maria_shiny/data/root_all_estimators.csv")

data <- data[1:nrow(est),]

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


# MAKE THE PCA
data.pca<-prcomp(data[,2:(ncol(data)-6)], retx = T, scale=T)
est.pca<-prcomp(est[,2:(ncol(est)-2)], retx = T, scale=T)

data <- cbind(data, data.pca$x)
est <- cbind(est, est.pca$x)

# PLOT THE DATA // BY TYPE
plot1 <- qplot(data = data, x = PC1, y = PC2, colour = type, main="Ground-truth data") + 
  stat_ellipse(level = 0.95, size=1.5) + 
  theme_bw() + 
  #facet_grid(. ~ specie) + 
  scale_x_continuous(name=paste("PC1 (",round(summary(data.pca)$importance[2,1],4)*100,"%)",sep="")) +
  scale_y_continuous(name=paste("PC2 (",round(summary(data.pca)$importance[2,2],4)*100,"%)",sep="")) 


plot2 <- qplot(data = est, x = PC1, y = PC2, colour = type, main = "Image estimators") + 
  stat_ellipse(level = 0.95, size=1.5) + 
  theme_bw()  + 
  #facet_grid(. ~ specie) + 
  scale_x_continuous(name=paste("PC1 (",round(summary(est.pca)$importance[2,1],4)*100,"%)",sep="")) +
  scale_y_continuous(name=paste("PC2 (",round(summary(est.pca)$importance[2,2],4)*100,"%)",sep="")) 

# PLOT THE DATA // BY SPECIES

plot3 <- qplot(data = data, x = PC1, y = PC2, colour = specie, main="Ground-truth data") + 
  stat_ellipse(level = 0.95, size=1.5) + 
  theme_bw() + 
  scale_x_continuous(name=paste("PC1 (",round(summary(data.pca)$importance[2,1],4)*100,"%)",sep="")) +
  scale_y_continuous(name=paste("PC2 (",round(summary(data.pca)$importance[2,2],4)*100,"%)",sep="")) 

plot4 <- qplot(data = est, x = PC1, y = PC2, colour = specie, main = "Image estimators") + 
  stat_ellipse(level = 0.95, size=1.5) + 
  theme_bw() +
  scale_x_continuous(name=paste("PC1 (",round(summary(est.pca)$importance[2,1],4)*100,"%)",sep="")) +
  scale_y_continuous(name=paste("PC2 (",round(summary(est.pca)$importance[2,2],4)*100,"%)",sep="")) 

grid.arrange(plot1, plot2, plot3, plot4, ncol=2, nrow=2)
g <- arrangeGrob(plot1, plot2, plot3, plot4, ncol=2, nrow=2)

ggsave(file="10_others/figures/fig2.pdf", g) 

