# LOAD LIBRARIES
library(ggplot2)
require(gridExtra)

setwd("~/Dropbox/research/projects/research/MARIA/maria_scripts/04_coverage/")


rs <- read.csv("data/params_range_dicot-2D.csv")

plots <- list()
for(n in colnames(rs)[-1]){
  rs$var <- rs[[n]]
  pl <- ggplot(data = rs, aes(x=var)) + 
    geom_bar(stat="bin", fill="grey", colour="white") + 
    xlab(n) + 
    theme_classic()
  plots[[(length(plots) + 1)]] = pl
}


# Set up the page
grid.newpage()
layout <- matrix(seq(1, 12) ,ncol = 3, nrow = 4)
pushViewport(viewport(layout = grid.layout(nrow(layout), ncol(layout))))
  
# Make each plot, in the correct location
for (i in 1:length(plots)) {
  # Get the i,j matrix positions of the regions that contain this subplot
  matchidx <- as.data.frame(which(layout == i, arr.ind = TRUE))
    
  print(plots[[i]], vp = viewport(layout.pos.row = matchidx$row,
                                  layout.pos.col = matchidx$col))
}


