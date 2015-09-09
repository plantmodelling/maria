
#----------------------------------------------------------------------
#function pkgTest installs required packages if they are not installed
#
#Arguments:
#pkg			the package name given in qoutes, e.g. "xtable"
#
#Returns:
#TRUE / stop 	returns TRUE if succesful, otherwise stop message is output
#
#-----------------------------------------------------------------------
pkgTest <- function(pkg)
{
  if (!require(pkg,character.only = TRUE))
  {
    install.packages(pkg,dep=TRUE)
    if(!require(pkg,character.only = TRUE)) stop("Package not found")
  }
  return(TRUE)
}


#----------------------------------------------------------------------
#function divideSet divides randomly the data set into a test and training set
#corresponding to the given percentage
#
#Arguments:
#size			Number of samples in the data
#perc			Percent of data into the test set, by default 30
#
#Returns:
#Returns a list of two vectors of indices, where the first vector is the index set
#for the test set and second vector for the training set
#
#-----------------------------------------------------------------------
divideSet <- function(size, perc=30)
{
  if ( 0 < perc && perc < 100)
  {
    set.seed(perc * size)
    if ( 1 <= floor( perc / 100 * size) )
      test <- sample(1 : floor( perc / 100 * size))
    else test <- NULL
    
    if (size >= 1+floor( perc / 100 * size))
      train <- sample((1+floor( perc / 100 * size)): size)
    else train <- NULL
    
    return( list(test=test, train=train) )
  }
  else
    stop("Percentage has to be in between 0 and 100")
  
}


#----------------------------------------------------------------------
#function predNAvalues predicts for each coloumn the missing NA values 
#with a random forest of 10 trees
#
#Arguments:
#X			A matrix containing data, where a row is a datum
#
#Returns:
#Returns a matrix where the NA values are replaced by predicted values
#
#-----------------------------------------------------------------------
predictNAvalues <- function(data)
{
  nrcol             <- ncol(data);
 
  #tempdata          <- matrix(as.numeric(unlist(data)), ncol = nrcol)
  tempdata          <- data
  
  indexset          <- NULL
  
  #the models work only with coloumns where no entry is NA
  #so replace in every correspond coloumn the NA values by its median
  for(i in 1:nrcol)
  {
    indices         <- which(is.na(data[,i]));
    numberOfNAs     <- length(indices);
    
    if (numberOfNAs > 0)
    {
      indexset            <- c(indexset,i)
      tempdata[indices,i] <- median(data[,i], na.rm=T)
    }
  }
  
  #compute a prediction for the corresponding NA indices based on all other values
   for(i in indexset)
   {
     indices         <- which(is.na(data[,i]));
     fit             <- randomForest(tempdata[,i] ~ . -tempdata[,i], data=as.data.frame(tempdata), ntree=10)
     data[indices,i] <- predict(fit, as.data.frame(tempdata[indices,]))  
   }
  

  #return the matrix where NAs have been replaced
  return(data);	
}


#----------------------------------------------------------------------
#function fitsPrecision computes the precision of the fit in the mean,
#i.e. a prediction for each response variable based on the corresponding model
#is performed, then the ratio of the mean of the fit and the mean of the true data
#is calculated and returned.
#
#Arguments:
#Xdata         input data
#mfit          a list of models
#
#Returns
#A vector comprising model precisions, each entry corresponding to one model 
#
#-----------------------------------------------------------------------
fitsPrecision <- function(Xdata, mfit)
{
  n         <- length(mfit)
  resME     <- matrix(ncol = 2, nrow = n)
  
  for(i in 1:n)
  {
    fit        <- predict(mfit[[i]]$fit, Xdata)
    mean1      <- mean(fit)
    var1       <- var(fit)
    mean2      <- mean(Xdata[  , names(mfit)[i]  ])
    var2       <- var(Xdata[  , names(mfit)[i]  ])
    #print(paste(mean1, mean2, collapse=" "))
    
    if (abs(mean1/mean2) <= 1)
      resME[i,1]    <- abs(mean1/mean2)
    else 
      resME[i,1]    <- abs(mean2/ mean1)
    
    if (var1/var2 <= 1)
      resME[i,2]    <- abs(var1/var2)
    else 
      resME[i,2]    <- abs(var2/ var1)
    
  
    
  }
  
  return(resME)
}


#----------------------------------------------------------------------
#function modelLM generates a list of linear model(s) for the given data 'Xdata',
#one model for each response variable, indexed by 'colnr'. 
#The linear models use as predictor variables the 'ntop' most important variables
#wrt to the random forest fit 'rmfit'.
#In case of exactly only response variable,
#the function returns still a list, containing exactly one model then.
#
#Arguments:
#Xdata         input data
#colnr         a vector of indices representing the response variables, i.e.
#              if the response variables are coloumns 20 and 22 of 'Xdata', then
#              colnr = c(20,22)
#rmfit         a list of random forest models, corresponding to the variables indexed by 'colnr'
#ntop          the number of most important variables to incorporate in the linear model
#
#Returns:
#A list of linear model(s), one for each response variable
#
#-----------------------------------------------------------------------
modelLM<-function(Xdata, colnr, rmfit, ntop)
{
  n            <- length(colnr)
  resLM        <- list()
 
  #Generate for each response variable a linear model
  for(i in 1:n)
  { 
    tofit           <- colnr[i]
   
    #Extract the 'ntop' most important variables wrt to the random forest fit
    var             <- (order(-(rmfit[[i]])$importance[,1]))[1:ntop] 
    
    #Rearrangement of the data, comprising only the most explanatory variables and the one to fit
    var             <- names(rmfit[[i]]$importance[,1])[var]
    Ydata           <- cbind(Xdata[,var], Xdata[,tofit])
    colnames(Ydata) <- c(var, colnames(Xdata)[tofit])
    lpos            <- dim(Ydata)[2]
   
    #Pasting the names of the variables for an explicit formula
    formul          <- formula(paste(colnames(Ydata)[lpos], "~",
                            paste(colnames(Ydata)[-lpos], collapse=" + ")))
    fit      <- lm(formul, data=as.data.frame(Ydata))
    
    # predicts + interval
    # predicts + interval
    predData <- data.frame(seq(0, max(Xdata[,tofit]), length.out=500))
    for(v in var) predData <- cbind(predData, data.frame(seq(0, max(Xdata[,v]), length.out=500)))
    colnames(predData) <- c(colnames(Xdata)[tofit], var)
    
    preds <- predict(fit, newdata = predData, interval = 'confidence', level=0.999)
    error <- data.frame("value"=((preds[,1] - preds[,2]) / max(preds[,1]))*100, "x"= predData)
    
    
    resLM[[i]] <- list(fit = fit, error = error)
  }
  #List entries equal the name of the response variables in order to easily refer to its model
  names(resLM) <- names(Xdata)[colnr]
  return(resLM)
}


#----------------------------------------------------------------------
#function modelRF generates a list of random forest model(s) for the given data 'Xdata',
#one model for each response variable, indexed by 'colnr'. 
#In case of exactly only response variable,
#the function returns still a list, containing exactly one model then.
#
#Arguments:
#Xdata         input data
#colnr         a vector of indices representing the response variables, i.e.
#              if the response variables are coloumns 20 and 22 of 'Xdata', then
#              colnr = c(20,22)
#nrtree        number of trees of the random forest
#
#Returns:
#A list of random forest model(s), one for each response variable
#
#-----------------------------------------------------------------------
modelRF<-function(Xdata, colnr, nrtree)
{
  n               <- length(colnr)
  resRF           <- list()
  
  #Generate for each response variable a random forest model
  for(i in 1:n)
  {  
    tofit           <- colnr[i]
    #Rearrangement of the data, excluding the response variables except the one to fit
    Ydata           <- cbind(Xdata[,-colnr], Xdata[,tofit])
    
    lpos            <- dim(Ydata)[2]
   
    colnames(Ydata) <- c(colnames(Xdata)[-colnr], colnames(Xdata)[tofit])
  
    #Pasting the names of the variables for an explicit formula
    formul          <- formula(paste(colnames(Ydata)[lpos], "~",
                            paste(colnames(Ydata)[-lpos], collapse=" + ")))
   
    resRF[[i]]      <- randomForest(formul , data=Ydata, importance=TRUE, ntree=nrtree)
  }
  #List entries equal the name of the response variables in order to easily refer to its model
  names(resRF) <- colnames(Xdata)[colnr]
  return(resRF)
}


#----------------------------------------------------------------------
#function modelRF generates a list of random forest model(s) for the given data 'Xdata',
#one model for each response variable, indexed by 'colnr'. 
#The model(s) incorporate response variables that are not in the vector 'extargets' as
#predictor variables.
#In case of exactly only response variable,
#the function returns still a list, containing exactly one model then.
#
#Arguments:
#Xdata         input data
#colnr         a vector of indices representing the response variables, i.e.
#              if the response variables are coloumns 20 and 22 of 'Xdata', then
#              colnr = c(20,22)
#extargets     a vector of indices representing response variables to be omitted as 
#              prediction variables 
#nrtree        number of trees of the random forest
#
#Returns:
#A list of random forest model(s), one for each response variable
#
#-----------------------------------------------------------------------
remodelRF<-function(Xdata, colnr, extargets, nrtree)
{
  n            <- length(colnr)
  resRF        <- list()
  
  #Generate for each response variable a linear model
  for(i in 1:n)
  {  
    tofit                 <- colnr[i]
    #Rearrangement of the data, excluding the response variables except the one to fit
    Ydata                 <- cbind(Xdata[,-c(extargets, tofit)], Xdata[,tofit])
    
    lpos                  <- dim(Ydata)[2]
    colnames(Ydata)[lpos] <- colnames(Xdata)[tofit]
    
    #Pasting the names of the variables for an explicit formula
    formul                <- formula(paste(colnames(Ydata)[lpos], "~",
                            paste(colnames(Ydata)[-lpos], collapse=" + ")))
     resRF[[i]]            <- randomForest(formul , data=Ydata, importance=TRUE, ntree=nrtree)
  }
  #List entries equal the name of the response variables in order to easily refer to its model
  names(resRF) <- names(Xdata)[colnr]
  return(resRF)
}


#----------------------------------------------------------------------
#function validityTest checks whether the set of predictor variables
#and the set of response variables are valid, 
#i.e. containing at least one variable, being within range and
#that the response variables are a subset of the permissible data set
#
#Arguments:
#f_vec        the coloumn indices of the response variables
#p_vec        the coloumn indices of the permissible variables
#n            the total number of coloumns in the data set
#
#Returns:
#Stops if a set is not valid, otherwise it simply returns to the toplevel call
#
#-----------------------------------------------------------------------
validityTest <- function(f_vec, p_vec, n)
{
 
  # Check whether 'f_vec' is a valid parameter
  if ( length(f_vec) < 1 )
    stop("Parameter 'f_vec' needs at least one component.")
  if ( length(f_vec) == length(p_vec))
    stop("Number of response variables has to be a subset of the variables of the whole data set.")
  if (max(f_vec) > max(p_vec) || min(f_vec) < min(p_vec) )
    stop("Indices of the parameter 'f_vec' are out of range of the permissible data set.")
  
  
  # Check whether 'p_vec' is a valid parameter
  if ( length(p_vec) < 1 )
    stop("Parameter 'p_vec' needs at least one component.")
  if (max(p_vec) > n || min(p_vec) < 1 )
    stop("Indices of the parameter 'p_vec' are out of range.")
 
  if(length(which(f_vec %*% t(1/p_vec) ==1)) != length(f_vec))
    stop("The response variables have to be a subset of the permissible data set.")
  
  return(TRUE);
}

  
#----------------------------------------------------------------------
#function maria_learning generates a in a two-step approach a linear model 
#for each given response variable in 'f_vec'.
#
#At the start the data is read from a .csv file named 'fname', 
#then the validity of the parameters 'f_vec' and 'p_vec' is checked,
#and finally potential NA values are replaced by linear predictions.
#
#The first step computes a random forest of 'nrt' tress for 
#each response variable.
#Then a linear model is fit to the data only involving
#the 'ntop' most important variables of the correspondent random forest. 
#Evaluating the precision for each response variable prediction 
#returns a set of response variables whose precision is above 'acc'.
#In the second step the function computes a new random forest of 'nrt' trees
#for each response variable where as prediction variables those
#response variables are allowed to be inclued whose precision was above 'acc'.
#Finally a linear model is generated for each response variable
#based on the 'ntop' most important variables of the last random forest model.
#
#
#Arguments:
#fname              the .csv filename
#f_vec              the response variables, i.e. an index vector corresponding to the coloumns of the data 
#p_vec              the prediction variables, i.e. an index vector corresponding to the permittable data coloumns
#                       permittable means here, those data coloumns that are allowed to be included as predictors
#nrt                the number of trees for a random forest model
#                       (this factor affects the most the running time and precision, for large data sets a value of 5-20 is fine)
#ntop               the number of most important variables of a random forest that shall be used as 
#                   predictors in the linear model ( to much variables causes overfitting )
#acc                the level of desired precision that is expected a response variable to have in order to considered
#                   as a predictor variable in the remodeling step two
#
#Returns:
#A list of linear models, one for each response variable.
#-----------------------------------------------------------------------
maria_learning <- function(fname, f_vec, p_vec, nrt, ntop, acc)
{
  # Load the data
  #data  <- read.csv(get('fname'), header=TRUE)
  data <- fname
  # Check the validity of predictor and response indices
  if(validityTest(f_vec,p_vec,dim(data)[2])) message("Dataset is valid")
  # Update the index vector of response variables 
  temp      <- f_vec
  f_vec     <- NULL
  for(i in 1:length(temp))
            f_vec <- c(f_vec, which(p_vec==temp[i]))
  
  #If there are less predictor variables than the desired number 
  #of most important variables, then 'ntop' has to be adequately reduced
  if(ntop > length(p_vec) - length(f_vec))
            ntop  <- length(p_vec) - length(f_vec)
  
  # Check existence of and load the packacke for random forests
  pkgTest("randomForest")
  library(randomForest)
  
  
  # PREPROCESSING THE DATA #
  
  cdata      <- data[,p_vec]
  # Eliminating the NA values
  cdata      <- predictNAvalues(cdata)
  # Divide the data into training and test data
  res        <- divideSet(dim(data)[1])
  data.train <- cdata[res$train,]
  data.test  <- cdata[res$test,]

  # MODEL GENERATION & PREDICTION
  
  # Generate random forest model(s)
  resRF      <- modelRF(data.train, f_vec, nrt)
  # Based on the 'ntop' most explanatory variables wrt to the random forest model
  # generate linear model(s)
  resLM      <- modelLM(data.train, f_vec, resRF,ntop)
  # Compute the precision of the models
  precision  <- fitsPrecision(data.test, resLM)
  
  # FINAL PREDICTION
  
  # Determine the model(s) whose precision is/are below a desired accuracy
  indexvec   <- which(precision[1,] < acc)
  extargets  <- f_vec[indexvec]
  
  # Re-generate random forest model(s), where this time 
  # response variables, whose precision is above the desired accuracy 'acc',
  # are allowed to be used as predictor variables  
 
  if (length(extargets) == 0 || length(extargets)==1 && is.na(extargets))
  {
    return(resLM)
  }
  else
  {
    resFinal   <- remodelRF(data.train,f_vec, extargets, ntop)
    
    # Generate final linear model(s)
    result     <- modelLM(data.train, f_vec, resFinal, ntop)
    mean       <- fitsPrecision(data.test, result)
    return(list(first = resLM, second = result, below = colnames(data.train)[extargets]
))
  }
  
}