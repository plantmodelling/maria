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
      
      #r <- nrow(ests)
      descriptors <- Data()
      d <- nrow(descriptors)
      
      # check which regression is better for every variable
      first <- which(maria_regs$best < 0) 
      second <- which(maria_regs$best >= 0) 
      
      # Get the predictions from the machine learning
      # This has to be done in two passes, since the machine learning 
      # was build iterativelly. 
      results <- data.frame()
      results1 <- data.frame()
      
      for(k in 1:2){
        reg <- maria_regs[[k]]
        if(!is.null(reg)){
          to_est <- names(reg)
          results <- data.frame()
          for(te in to_est){
            preds <- predict(reg[[te]], newdata=descriptors)
            if(length(results) == 0) results <- preds
            else results <- cbind(results, preds)
          }
          colnames(results) <- c(to_est)
          if(k == 1){
            descriptors <- cbind(descriptors, results)
            results_1 <- results
          }
        }
      }
      new_names <- gsub(" ","", c(colnames(results_1[,first]), colnames(results[,second])))
      results <- data.frame(cbind(results_1[,first], results[,second]))
      colnames(results) <- new_names        

      return(results)
      
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
      
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      x <- descriptors[,get(input$var)] # Histogram data
      x1 <- Data()[,get(input$var)] # User data

      # Draw the histogram
      d <- density(x)      
      plot(d, type='n', 
           axes=F, 
           xlab=input$var, 
           xlim=range(x, x1), 
           main=paste("Histogram of ",input$var)); 
      axis(1); axis(2)
      polygon(d, col=col.distri, border=NA)      
      
      
      good <- x1[x1 > min(x) & x1 < max(x)]
      bad <- x1[x1 < min(x) | x1 > max(x)]
      abline(v=good, col = col.passed)
      abline(v=bad, col = col.failed, lwd=2)
        
    })
    
    
    #------------------------------------------------------
    # Plot the parameters distribution
    
    output$resultPlot <- renderPlot({
  
      # draw the histogram with the specified number of bins
      if (is.null(input$data_file) || input$runMARIA == 0) { return()}
      
      #Load the simulated parameters
      x <- parameters[, get(input$param)]
      model <- maria_regs

      # yrange <- range(model[[input$param]]$error, -model[[input$param]]$error)
      # xrange <- range(model[[input$param]]$x, na.rm = T)
#       min <- min(model[[input$param]]$x[model[[input$param]]$error < 5])
#       max <- max(model[[input$param]]$x[model[[input$param]]$error < 5])
#       min.y <- min(yrange)
#       min.x <- min(xrange)
#       max.y <- max(yrange)
#       max.x <- max(xrange)

      # Plot 2
      layout(matrix(c(1,2), 2, 1, byrow = TRUE), heights=c(1,3))      
      
      # Error plot
      par(mar=c(0, 4, 3, 3))
      plot(x, type='n', axes=F, 
           # ylim=yrange, xlim=xrange, 
           main=paste("Histogram of ",input$param), ylab="error [%]")
#       polygon(c(min.x, min.x, min, min), c(min.y, max.y, max.y, min.y), col=col.out, border = NA)
#       polygon(c(max, max, max.x, max.x), c(min.y, max.y, max.y, min.y), col=col.out, border = NA)
#       polygon(c(rev(model[[input$param]]$x), model[[input$param]]$x), 
#               c(rev(model[[input$param]]$error), -model[[input$param]]$error), 
#               col = col.error, border = NA)
#       abline(h=0, col="white", lty=2)  
#       abline(v=Results()[, input$param], col=col.passed)
      axis(2)
      
      
      # Histogram
      par(mar=c(4, 4, 2, 3))
#       hist(x, breaks = 150, col = 'darkgrey', border = 'white', xlab=input$param, freq=F, main="", xlim=xrange)
      d <- density(x)      
      plot(d, type='n', axes=F, xlab=input$param, xlim=range(x), main=""); 
      axis(1); axis(2)
      polygon(d, col=col.distri, border=NA)
      # polygon(c(min.x, min.x, min, min), c(0, 10, 10, 0), col=col.out, border = NA)
      # polygon(c(max, max, max.x, max.x), c(0, 10, 10, 0), col=col.out, border = NA)
      abline(v=Results()[,input$param], col="#3ac090")
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
      
      descriptors_names <- colnames(descriptors[2:ncol(descriptors)])
      outdata <- NULL
      proceed <- T
      for(e in descriptors_names){
        x1 <- Data()[,get(e)]
        x2 <- descriptors[,get(e)]
        x2 <- x2[!is.nan(x2)]
        if(min(x1) < min(x2) || max(x1) > max(x2)){
          outdata <- c(outdata, e)
          proceed <- F
        }
      }
      if(proceed) "Data are in range"
      else {
        param_error <- 1-round(length(outdata) / length(descriptors_names), 3)        
        message <- paste("<b>WARNING: Data out of range:</b>")
        for(od in outdata) message <- paste(message, od, ",")
        message <- paste(message, "</br>Score = ",param_error)
        HTML(message)
      }
    })

    
    
    
  }
)