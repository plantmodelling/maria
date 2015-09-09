setwd("C:/Iko/Studie/Master Environmental biology/Stage2/Model Assisted phenotyping/Models4")
Data <- read.csv("root_data.csv", sep = ";")
Data2 <- read.csv("output-param.csv", sep = ";")
test <- read.csv("test.csv", sep = ";")

output_data2 <- prcomp(Data2[,4:28],
                center = TRUE,
                scale. = TRUE) 
plot(output_data)
summary(output_data)

library("FactoMineR")
pcaOutput2 <- PCA(Data[,11:21])

library("ggbiplot")
g <- ggbiplot(output_data2, obs.scale = 1, var.scale = 1, groups = as.factor(Data[,3]),ellipse = TRUE, 
              circle = TRUE, choices = c(1,2))
g <- g + scale_color_discrete(name = '')
g <- g + theme(legend.direction = 'horizontal', 
               legend.position = 'top')
print(g)