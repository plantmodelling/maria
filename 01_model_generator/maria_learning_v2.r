
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
#function colVar computes the corrected variances of the coloumns,
#i.e. let n be the number of coloumns, then the function sums each coloumn 
#and divides each sum by n - 1
#
#Arguments:
#X			a matrix 
#
#Returns:
#the corrected row variance
#
#-----------------------------------------------------------------------
colVar <- function(X)
{
  n     <- dim(X)[2] 
  res   <- colSums(sweep(X, 2, colMeans(X))^2)/(n - 1)
  return(res)
}

#--------------------------------------------------------------------------
# function fsample samples a number of "nr" values within the provided "range"
# where it ensures that all values within the range are drawn when the
# number of values to sample is larger than the number of values in the
# given range
# Arguments:
# range		range where to sample from
# nr			nr of values to sample
#
# Returns:
# A vector of size nr of sampled values from the provided range
#--------------------------------------------------------------------------
fsample <- function(range, nr)
{
  l <- length(seq(range))
  
  set.seed(42)
  
  if ( nr <= l )
    res <-sample(range,nr)
  else
  {
    div  <- nr %/% l
    rest <- nr %% l
    
    cur <- 1
    
    res <- rep(0, nr)
    
    for(i in 1:div)
    {
      set.seed(42 + i)
      res[cur : (cur + l - 1)] <- sample(range, l)
      cur <- cur + l
    }
    
    if (rest > 0)
    {
      set.seed(42)
      res[cur : nr] <- sample(range, nr - cur  + 1)
    }
    
  }		 
  
  return(res)
}

#----------------------------------------------------------------------
#function crossVal returns cross-validation matrix. The matrix has a
#number of "nrfolds" columns. Each column represents the indices of a fold.
#If the number of folds is not a divisor of the sample size, i.e. one fold
#has not enough values, then the missing entries are filled by already
#used values.
#
#Arguments:
#size				The sample size
#foldNr			The number of folds
#
#Returns:
#A matrix of folds 
#
#-----------------------------------------------------------------------
#Example :
#>crossVal(10, 5)
#      [,1] [,2] [,3] [,4] [,5]
#[1,]   10    3    4    5    2
#[2,]    9    6    8    1    7
#
#-----------------------------------------------------------------------
crossVal <- function(size, nrfolds)
{
  if ( nrfolds > size )
    stop("The number of folds has to be smaller or equal to the sample size")
  nrmiss <- size %% nrfolds 
  crossSeq <- fsample(1:size, size + nrfolds-nrmiss)
  return(matrix(crossSeq, ncol = nrfolds))
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
  
  tempdata          <- matrix(as.numeric(unlist(data)), ncol = nrcol)
  
  indexset          <- NULL
  
  #the models work only with coloumns where no entry is NA
  #so replace in every correspond coloumn the NA values by its median
  for(i in 1:nrcol)
  {
    indices         <- which(is.na(data[,i]));
    numberOfNAs     <- length(indices);
    if (numberOfNAs > 0)
    {
      tempdata[indices,i] <- median(data[,i], na.rm=T)
      indexset <- c(indexset,i)
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
#function modelRF generates a list of random forest model(s) for the given data 'Xdata',
#one model for each response variable, indexed by 'colnr'.
#In case of exactly one response variable,
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
    
    resRF[[i]]<-randomForest(formul , data=Ydata, importance=TRUE, ntree=nrtree)
  }
  #List entries equal the name of the response variables in order to easily refer to its model
  names(resRF) <- colnames(Xdata)[colnr]
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
  
  return();
}

#----------------------------------------------------------------------
#function generalErrorEstimate computes an estimate of the
#MSE generalization error for each point.
#Due to the Bias-variance decomposition it holds
#      MSE = variance + bias^2 + noise
#where we set the noise to zero.
#The error is estimated on a training set.
#Arguments:
#rfModels         a set of rfModels, where each set comprises a fit for each
#                 response variable, i.e. a random forest;
#data             the trainings data
#
#Returns:
#A vector of errors, where each entry represents the error estimate of the
#corresponding data point.
#
#-----------------------------------------------------------------------
generalErrorEstimate <- function(rfModels, data, f_vec)
{
  rsModelsBU <- rfModels
  rfModels <- rfModels$model
  data <- rs1[,c(2:ncol(rs1))]
  n             <- length(rfModels[[1]])
  nrModels      <- length(rfModels)
  generalError  <- matrix(0,nrow = dim(data)[1],ncol=n)
  
  for (i in 1:n)
  {
    modelPred <- matrix(0, ncol = dim(data)[1], nrow = nrModels )
    for(k in 1:nrModels)
    {
      modelPred[k,] <- predict(rfModels[[k]][[i]], data) 
    }
    variance <- colVar(modelPred)
    bias <-  colMeans(modelPred) - data[,f_vec[i]]
    generalError[,i] <- variance + bias^2
  }
  return(generalError)
}


#---------------------------------------------------------------------
#function predictRF computes the prediction for a given data set
#given a set of random forest models
#
#Arguments:
#rfModels         a set of rfModels, where each set comprises a fit for each
#                 response variable, i.e. a random forest;
#data             the data to predict
#
#Returns:
#A vector of predictions for the given data
#
#-----------------------------------------------------------------------
predictRFs <- function(rfModels, data)
{
  n             <- length(rfModels[[1]])
  nrModels      <- length(rfModels)
  result        <- matrix(0,nrow = dim(data)[1],ncol=n)
  
  for (i in 1:n)
  {
    modelPred <- matrix(0, ncol = dim(data)[1], nrow = nrModels )
    
    for(k in 1:nrModels)
    {
      modelPred[k,] <- predict(rfModels[[k]][[i]], data) 
    }
    result[,i] <- colMeans(modelPred)
  }
  return(result)
  
}


#----------------------------------------------------------------------
#function main generates a set of random forest models for predicting data
#and estimates a MSE generalization error for each data
#
#At the start the data is read from a .csv file named 'fname',
#then the validity of the parameters 'f_vec' and 'p_vec' is checked,
#and finally potential NA values are replaced by a vanilla random forest prediction.
#
#The first step computes "nrModels" random forest models of 'nrt' tress for
#each response variable.
#Then an estimate of the generalization error is computed.
#Finally a data frame of the models and the error estimate is returned.
#
#
#Arguments:
#fname              the .csv filename
#f_vec              the response variables, i.e. an index vector corresponding to the coloumns of the data
#p_vec              the prediction variables, i.e. an index vector corresponding to the permittable data coloumns
#                       permittable means here, those data coloumns that are allowed to be included as predictors
#nrt                the number of trees for a random forest model
#                       (this factor affects the most the running time and precision, for large data sets a value of 5-20 is fine)
#nrModels
#
#Returns:
#A data frame of a list of "nrModels" random forest models and an error estimate is returned. Each forest model
#contains one random forest  for each response variable.
#-----------------------------------------------------------------------
maria_learning <- function(fname, f_vec, p_vec, nrt, nrModels)
{
  # Load the data
  data       <- fname; #read.csv(get('fname'), header=TRUE)
  # Check the validity of predictor and response indices
  validityTest(f_vec,p_vec,dim(data)[2])
  # Update the index vector of response variables
  temp      <- f_vec
  f_vec     <- NULL
  for(i in 1:length(temp))
    f_vec <- c(f_vec, which(p_vec==temp[i]))
  
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
  # Create the folds for training the models
  size       <- dim(data.train)[1] 
  folds      <- crossVal(size,nrModels)
  
  # MODEL GENERATION
  rfModels   <- list()
  # Generate random forest models
  for (i in 1:nrModels)
  {
    data.train.model   <- data.train[folds[,i],]
    rfModels[[i]]      <- modelRF(data.train.model,f_vec, nrt)
  }
  
  
  # GENERALIZATION ERROR ESTIMATION
  error        <- generalErrorEstimate(rfModels, data.test, f_vec)
  
  
  # FINAL PREDICTION
  return(list(model=rfModels,error=error))
}
