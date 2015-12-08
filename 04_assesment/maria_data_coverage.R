# LOAD LIBRARIES
library(ggplot2)
require(gridExtra)

setwd("~/Dropbox/research/projects/research/MARIA/maria_scripts/07_maria_shiny/data/")


rs <- read.csv("root_data_monocot-2D.csv")

plots <- list()
for(n in colnames(rs)[-c(1)]){
  rs$var <- rs[[n]]
  temp <- rs[!rs$var %in% boxplot.stats(rs$var)$out,]
  if((max(temp$var)-min(temp$var)) > 0){
    pl <- ggplot(data = temp, aes(x=var)) + 
      geom_bar(stat="bin", fill="grey", colour="white") + 
      xlab(n) + 
      theme_classic()
    plots[[(length(plots) + 1)]] = pl
  }
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


