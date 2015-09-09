shinyServer(
  
  function(input, output) {
    
    #------------------------------------------------------
    # LOAD THE USER DATA
    
    Data <- reactive({
      
      inFile <- input$data_file   
      if (is.null(inFile)) return(NULL)
      data <- fread(inFile$datapath)  
      return(data)
      
    })   
    
    #------------------------------------------------------
    # PROCESS THE DATA
    
    Results <- reactive({
      
      if(input$runMARIA == 0){return()}
      
      r <- nrow(ests)
      d <- nrow(Data())
      
      # Get the estimations using the machine learning regressions.
      results <- data.frame(id = c(1:nrow(Data())))
      for(k in 1:length(maria_reg)){
        reg <- maria_reg[[k]]
        to_est <- names(reg)
        results <- data.frame(id = c(1:nrow(Data())))
        for(te in to_est){
          preds <- predict(reg[[te]]$fit, newdata=Data())
          results <- cbind(results, preds)
        }
        colnames(results) <- c("id", to_est)
        if(k < length(maria_reg)){
          Data() <- cbind(Data(), results)
        }
      }
      colnames(results) <- c("id", paste("est_", to_est, sep=""))
      
      
      
      # Reduce the size of the dataset
      #message(nrow(ests))
      #ests <- ests[ests$depth > min(Data()$depth)*0.8 & ests$depth < max(Data()$depth) * 1.2,]
      #ests <- ests[ests$depth > (min(rs$depth)*0.8) & ests$depth < (max(rs$depth) * 1.2),]
      #message(nrow(ests))
      
      original <- ests[,c("image", "depth", "width", "area"), with=F]
      
      # Merge the data and the test for the weighting
      ests <- rbind(ests, Data())
      
      # scale the data
      ests[,2:ncol(ests)] = data.table(scale(ests[, 2:ncol(ests), with=F]))
      
      # Weight the data
      for(e in estimators) ests[[e]] <- ests[[e]] * mean(weights[[e]])
      
      test <- ests[(r+1):nrow(ests),]
      base <- ests[0:r,]
      nvar <- length(estimators)      
      
      # Get the distance matrix
      distances <- as.matrix(pdist(base[,estimators, with=F], test[,estimators, with=F]))
      test$match <- base$image[apply(distances, 2, min.index)]
      test$dist <- apply(distances, 2, min.value)
      
      # Save the distance data (test image + match)
      pred <- test[,c("match", "image", "dist"), with=F]      
      
      if(length(data$image) > 0) setnames(data,'image','match')
      setnames(original,'image','match')  
      
      obs <- merge(pred, data, by="match")
      obs <- merge(obs, original, by="match")

      return(obs)
      
    })
    
    #------------------------------------------------------
    # Display the root image
    # Send a pre-rendered image, and don't delete the image after sending it
    
    output$rootImage <- renderImage({
      
      if (is.null(input$data_file) || input$runMARIA == 0) {return(NULL)}
      
      # When input$n is 3, filename is ./images/image3.jpeg
      filename <- as.character(Results()$image[as.numeric(input$imid)])
      
      # Return a list containing the filename and alt text
      list(src = filename, width=200)     
      
    }, deleteFile = FALSE
    )
    
    output$rootMatch <- renderImage({
      
      if (is.null(input$data_file) || input$runMARIA == 0) { return(NULL)}
      
      
      render_roots(Results()$match[as.numeric(input$imid)])
      
      # When input$n is 3, filename is ./images/image3.jpeg
      filename <- normalizePath(file.path('./model/root_image.rsml.jpg', sep=''))
      
      # Return a list containing the filename and alt text
      list(src = filename, width=200)     
      
    }, deleteFile = FALSE
    )
    
    
    #------------------------------------------------------
    # Plot the estimator distribution
    
    output$distPlot <- renderPlot({      
      
#       input$var <- "diam_max"
#      x <- ests[,get(input$var)]
#       hist(x, breaks = 50, col = 'white', border = 'white', xlab=input$var, freq=F, main=paste("Histogram of ",input$var))
#      d <- density(x)      
#      plot(d, type='n', axes=F, xlab=input$var, xlim=range(x), main=paste("Histogram of ",input$var)); axis(1); axis(2)
#      polygon(d, col=col.distri, border=NA)  
      
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      x <- ests[,get(input$var)] # Histogram data
      x1 <- Data()[,get(input$var)] # User data
      
      # Draw the histogram
      d <- density(x)      
      plot(d, type='n', axes=F, xlab=input$var, xlim=range(x, x1), main=paste("Histogram of ",input$var)); axis(1); axis(2)
      polygon(d, col=col.distri, border=NA)      
      
      
      good <- x1[x1 > min(x) & x1 < max(x)]
      bad <- x1[x1 < min(x) | x1 > max(x)]
      abline(v=good, col = col.passed)
      abline(v=bad, col = col.failed, lwd=2)
        
    })
    
    
    #------------------------------------------------------
    # Plot the parameters distribution
    
    output$resultPlot <- renderPlot({
      
#       
#       x <- data[, get(input$param)]
#       
#       # Plot 1
#       yrange <- range(model[[input$param]]$error$value, -model[[input$param]]$error$value)
#       xrange <- range(model[[input$param]]$error$x)
#       min <- min(model[[input$param]]$error$x[model[[input$param]]$error$value < 5])
#       max <- max(model[[input$param]]$error$x[model[[input$param]]$error$value < 5])
#       min.y <- min(yrange)
#       min.x <- min(xrange)
#       max.y <- max(yrange)
#       max.x <- max(xrange)
#       
#       layout(matrix(c(1,2), 2, 1, byrow = TRUE), heights=c(1,3))      
#       # Error
#       par(mar=c(0, 4, 3, 3))
#       plot(x, type='n', axes=F, main=paste("Histogram of ",input$param),
#            ylim=yrange, xlim=xrange, ylab="error [%]")
#       polygon(c(min.x, min.x, min, min), c(min.y, max.y, max.y, min.y), col="#dc021820", border = NA)
#       polygon(c(max, max, max.x, max.x), c(min.y, max.y, max.y, min.y), col="#dc021820", border = NA)
#       polygon(c(rev(model[[input$param]]$error$x), model[[input$param]]$error$x), 
#               c(rev(model[[input$param]]$error$value), -model[[input$param]]$error$value), 
#               col = col.error, border = NA)
#       abline(h=0, col="white", lty=2)  
#       axis(2)
#       # Histogram
#       par(mar=c(4, 4, 2, 3))
# #       hist(x, breaks = 150, col = 'darkgrey', border = 'white', xlab=input$param, freq=F, main="", xlim=xrange)
#       d <- density(x)      
#       plot(d, type='n', axes=F, xlab=input$param, xlim=range(x), main=""); axis(1); axis(2)
#       polygon(d, col=col.distri, border=NA)
#       polygon(c(min.x, min.x, min, min), c(0, 10, 10, 0), col=col.out, border = NA)
#       polygon(c(max, max, max.x, max.x), c(0, 10, 10, 0), col=col.out, border = NA)
#       
      
      # draw the histogram with the specified number of bins
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      #Load the data
      x <- data[, get(input$param)]
  
      yrange <- range(model[[input$param]]$error$value, -model[[input$param]]$error$value)
      xrange <- range(model[[input$param]]$error$x)
      min <- min(model[[input$param]]$error$x[model[[input$param]]$error$value < 5])
      max <- max(model[[input$param]]$error$x[model[[input$param]]$error$value < 5])
      min.y <- min(yrange)
      min.x <- min(xrange)
      max.y <- max(yrange)
      max.x <- max(xrange)

      # Plot 2
      layout(matrix(c(1,2), 2, 1, byrow = TRUE), heights=c(1,3))      
      
      # Error plot
      par(mar=c(0, 4, 3, 3))
      plot(x, type='n', axes=F, main=paste("Histogram of ",input$param),ylim=yrange, xlim=xrange, ylab="error [%]")
      polygon(c(min.x, min.x, min, min), c(min.y, max.y, max.y, min.y), col=col.out, border = NA)
      polygon(c(max, max, max.x, max.x), c(min.y, max.y, max.y, min.y), col=col.out, border = NA)
      polygon(c(rev(model[[input$param]]$error$x), model[[input$param]]$error$x), 
              c(rev(model[[input$param]]$error$value), -model[[input$param]]$error$value), 
              col = col.error, border = NA)
      abline(h=0, col="white", lty=2)  
      abline(v=Results()[, get(input$param)], col=col.passed)
      axis(2)
      
      
      # Histogram
      par(mar=c(4, 4, 2, 3))
#       hist(x, breaks = 150, col = 'darkgrey', border = 'white', xlab=input$param, freq=F, main="", xlim=xrange)
      d <- density(x)      
      plot(d, type='n', axes=F, xlab=input$param, xlim=range(x), main=""); axis(1); axis(2)
      polygon(d, col=col.distri, border=NA)
      polygon(c(min.x, min.x, min, min), c(0, 10, 10, 0), col=col.out, border = NA)
      polygon(c(max, max, max.x, max.x), c(0, 10, 10, 0), col=col.out, border = NA)
      abline(v=Results()[,get(input$param)], col="#3ac090")
    })
    
    
    
    #------------------------------------------------------
    # Plot the matching distribution
    
    output$matching <- renderPlot({
      
#       #hist(pred$dist, breaks=150, freq=T, col = 'darkgray', border = 'white',
#       #     xlab="Matching distances [-]", main="Distribution of theoretical distances")
#       #       hist(x, breaks = 50, col = 'white', border = 'white', xlab=input$var, freq=F, main=paste("Histogram of ",input$var))
#       d <- density(pred$dist)      
#       plot(d, type='n', axes=F, xlab="Matching distances [-]", xlim=range(pred$dist), main="Distribution of theoretical distances")
#       axis(1); axis(2)
#       polygon(d, col=col.distri, border=NA)  
      
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      
#       hist(pred$dist, breaks=150, freq=T, col = 'darkgray', border = 'white', 
#            xlim=range(pred$dist, Results()$dist))
      d <- density(pred$dist)      
      plot(d, type='n', axes=F, xlab="Matching distances [-]", xlim=range(pred$dist), main="Distribution of theoretical distances")
      axis(1); axis(2)
      polygon(d, col=col.distri, border=NA)  
      abline(v = Results()$dist, col=col.passed)
      
    })    
    
    
    
    #------------------------------------------------------
    # Visualize the result of the matching as a table
    
    
    output$results <- renderTable({
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      head(Results())
    })
    
    #------------------------------------------------------
    # Download th result from the matching
    
    output$downloadData <- downloadHandler(
      filename = function() {"MARIA_results.csv"},
      content = function(file) {
        write.csv(Results(), file)
      }
    )
    
    #------------------------------------------------------
    ## Output a warning message
    output$caption1 <- renderUI( {
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      outdata <- NULL
      proceed <- T
      for(e in estimators){
        x1 <- Data()[,get(e)]
        x2 <- ests[,get(e)]
        x2 <- x2[!is.nan(x2)]
        if(min(x1) < min(x2) || max(x1) > max(x2)){
          outdata <- c(outdata, e)
          proceed <- F
        }
      }
      if(proceed) "Data are in range"
      else {
        param_error <- 1-round(length(outdata) / length(estimators), 3)        
        message <- paste("<b>WARNING: Data out of range:</b>")
        for(od in outdata) message <- paste(message, od, ",")
        message <- paste(message, "</br>Score = ",param_error)
        HTML(message)
      }
    })

    
    
    
  }
)