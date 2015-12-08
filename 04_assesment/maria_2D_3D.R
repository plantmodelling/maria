

# LOAD LIBRARIES
library(ggplot2)
require(gridExtra)

# LOAD DATA
data <- read.csv("~/Desktop/archisimple/outputs/root_data.csv")
est <- read.csv("~/Desktop/archisimple/outputs/root_estimators.csv")

data <- data[1:nrow(est),]

est$type <- "2D"
est$type[grepl("3D", est$image)] <- "3D"
est$type[grepl("shov", est$image)] <- "shov"
est$type <- factor(est$type)

est$specie <- "monocot"
est$specie[grepl("dico", est$image)] <- "dicot"
est$specie <- factor(est$specie)

path <- "/Users/guillaumelobet/Desktop/archisimple/outputs/images/"
est$image <- as.character(est$image)
est$image <- factor(substr(est$image, nchar(path)+1, nchar(est$image)-4))

rs <- merge(data, est, by="image")
rs <- rs[rs$type != "shov",]

ggplot(rs, aes(x=area, y=tot_root_length, color=type)) +
  geom_point(size=4, alpha=0.8) +
  geom_smooth(method=lm, se=T, fullrange=F) +
  theme_bw()



