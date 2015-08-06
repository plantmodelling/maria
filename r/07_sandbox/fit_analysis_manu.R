

ests <- read.csv("~/Desktop/archisimple/outputs/root_estimators.csv")
data <- read.csv("~/Desktop/archisimple/outputs/root_data.csv")
model <- read.csv("~/Desktop/archisimple/outputs/training-set.csv")

par(mfrow=c(1,3))

d1<- density(model$diam_mode)
d2<- density(ests$diam_mode)
plot(d2, col="red")
lines(d1, col="green")

d1<- density(model$tip_count)
d2<- density(ests$tip_count)
plot(d2, col="red")
lines(d1, col="green")

d1<- density(model$width)
d2<- density(ests$width)
plot(d1, col="green")
lines(d2, col="red")

ests$image <- gsub(pattern = "/Users/guillaumelobet/Desktop/archisimple/outputs/images/", "", ests$image)
ests$image <- gsub(pattern = ".jpg", "", ests$image)
rs <- merge(ests, data, by="image")


rs$fit <- (1.017 * log(rs$tip_count)) + (0.005 * rs$width) + (0.073 * rs$diam_mode) + 2.914
fit <- lm(rs$fit ~ log(rs$tot_root_length))
summary(fit)

par(mfrow=c(1,1))
plot(log(rs$tot_root_length), rs$fit)
abline(fit)
abline(a = 0, b = 1, col="red")
