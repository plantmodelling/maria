par(mfrow=c(2,2), mar=c(4,4,4,3))
##############################################################################
##################         VARY N_TREES
##############################################################################
n_trees                     <- 25               # the number of trees for a random forest model
n_var                       <- 5                # the number of most important variables of a random forest that shall be used as 
accu                        <- 0.8             # the level of desired precision that is expected a response variable to have in order to considered

precs <- data.frame(vars=numeric(), val=numeric())

if(doplot) par(mfrow=c(4,4), mar=c(4,4,4,3))
for(n_trees in seq(from=5, to=80, by=5)){
  # Get the set of linear equations with the whole dataset
  regs <- maria_learning(fname = rs, 
                         f_vec = c(73:88), 
                         p_vec = c(2:71,73:88), 
                         nrt = n_trees, 
                         ntop = n_var, 
                         acc = accu)      
  
  first <- which(regs$best < 0) 
  second <- which(regs$best >= 0) 
  prec <- c(regs$prec_1[first,1], regs$prec_2[second,1])
  if(doplot){
    plot((prec), main=paste0("n_trees = ", n_trees, " / prec = ",round(mean(prec),3)), ylim=c(0.5,1))
    abline(h=mean(prec))
  }
  precs <- rbind(precs, data.frame(vars=n_trees, val=prec))
}

if(doplot) par(mfrow=c(1,1), mar=c(5,5,4,3))
means <- tapply(precs$val, precs$vars, "mean")
boxplot(precs$val ~ precs$vars, main="Number of trees in the forest")
points(means, pch=4, lwd=3, col="blue")


##############################################################################
##################         VARY N_VAR
##############################################################################
n_trees                     <- 25               # the number of trees for a random forest model
n_var                       <- 5                # the number of most important variables of a random forest that shall be used as 
accu                        <- 0.8             # the level of desired precision that is expected a response variable to have in order to considered

precs <- data.frame(vars=numeric(), val=numeric())
if(doplot) par(mfrow=c(4,4), mar=c(4,4,4,3))

for(n_var in seq(from=5, to=80, by=5)){
  # Get the set of linear equations with the whole dataset
  regs <- maria_learning(fname = rs, 
                         f_vec = c(73:88), 
                         p_vec = c(2:71,73:88), 
                         nrt = n_trees, 
                         ntop = n_var, 
                         acc = accu)      
  
  first <- which(regs$best < 0) 
  second <- which(regs$best >= 0) 
  prec <- c(regs$prec_1[first,1], regs$prec_2[second,1])
  if(doplot){
    plot((prec), main=paste0("n_var = ", n_var, " / prec = ",round(mean(prec),3)), ylim=c(0.5,1))
    abline(h=mean(prec))
  }
  precs <- rbind(precs, data.frame(vars=n_var, val=prec))
}

if(doplot) par(mfrow=c(1,1), mar=c(5,5,4,3))
means <- tapply(precs$val, precs$vars, "mean")
boxplot(precs$val ~ precs$vars, main="Numver of variables")
points(means, pch=4, lwd=3, col="blue")


##############################################################################
##################         VARY ACCU
##############################################################################
n_trees                     <- 25               # the number of trees for a random forest model
n_var                       <- 5                # the number of most important variables of a random forest that shall be used as 
accu                        <- 0.8             # the level of desired precision that is expected a response variable to have in order to considered

precs <- data.frame(vars=numeric(), val=numeric())
if(doplot) par(mfrow=c(4,4), mar=c(4,4,4,3))

for(accu in seq(from=0.8, to=0.99, by=0.02)){
  # Get the set of linear equations with the whole dataset
  regs <- maria_learning(fname = rs, 
                         f_vec = c(73:88), 
                         p_vec = c(2:71,73:88), 
                         nrt = n_trees, 
                         ntop = n_var, 
                         acc = accu)      
  
  first <- which(regs$best < 0) 
  second <- which(regs$best >= 0) 
  prec <- c(regs$prec_1[first,1], regs$prec_2[second,1])
  if(doplot){
    plot((prec), main=paste0("accu = ", accu, " / prec = ",round(mean(prec),3)), ylim=c(0.5,1))
    abline(h=mean(prec))
  }
  precs <- rbind(precs, data.frame(vars=accu, val=prec))
}

if(doplot) par(mfrow=c(1,1), mar=c(5,5,4,3))
means <- tapply(precs$val, precs$vars, "mean")
boxplot(precs$val ~ precs$vars, main="Accuracy threshold")
points(means, pch=4, lwd=3, col="blue")