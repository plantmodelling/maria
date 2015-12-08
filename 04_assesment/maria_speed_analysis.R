library(ggplot2)
library(gridExtra)
library(ggExtra)

remove = T

setwd("~/Dropbox/research/projects/research/MARIA/maria_scripts/04_coverage/data")

speeds <- c(4000, 8000)
species <- c("dicot")
types <- c("3D")

rs <- data.frame(image=character(), width=numeric(), height=numeric(),
                 time_start=numeric(), time_end=numeric(), time=numeric(),
                 size=numeric(), weight=numeric(), time_cum=numeric())

for(speed in speeds){
  for(specie in species){
    for(type in types){
      tryCatch({
        rs1 <- read.csv(paste0("speed_test_",specie,"_",type,"_",speed,".csv"))
        rs1$time <- rs1$time_end - rs1$time_start
        rs1$size <- rs1$width * rs1$height
        rs1$weight <- (rs1$size * 4) / 1e6
        rs1$time_cum <- cumsum(rs1$time)
        # Remove outlyiers
        if(remove){
          rs1 <- rs1[!rs1$time %in% boxplot.stats(rs1$time)$out,]
          rs1 <- rs1[!rs1$size %in% boxplot.stats(rs1$size)$out,]
        }
        rs1$type <- paste0(speed, "Mb RAM")
        rs1 <- rs1[1:min(1500, nrow(rs1)),]
        rs <- rbind(rs, rs1)
        },
        error=function(e) NULL
      )
    }
  }
}

scatter <- ggplot(data=rs, aes(x=weight, y=time, colour=type)) + 
  geom_point(alpha=0.4) +
  geom_smooth(method = lm, se=F, size=1) + 
  xlab("Image size [Mb]") + 
  ylab("Processing time [ms]")+ 
  theme_classic() + 
  theme(legend.title=element_blank())
print(scatter)

#ggMarginal(scatter, type="histogram", col="white", fill="grey")
