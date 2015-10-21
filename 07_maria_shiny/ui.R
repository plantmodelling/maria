
library(shiny)

shinyUI(fluidPage(
  
  # Application title
  titlePanel(h1("- MARIA -")),
  
  # Sidebar with a slider input for the number of bins
  sidebarLayout(
    sidebarPanel(
      #h4("Model Assisted Root Image Analysis"),
      helpText("MARIA is a Model Assisted Root Image Analysis pipeline developped by Guillaume Lobet, Iko Koevoets, Manu Noll and Loïc Pagès.
             "),
      tags$hr(),      
      
      fileInput('data_file', 'Choose CSV File', accept=c('text/csv', 'text/comma-separated-values,text/plain', '.csv')),
      
      selectInput("species", label = "Choose a plant species",
                  choices = c("Monocots", "Dicots"), selected = "Monocots"),
      
      selectInput("setup", label = "Choose a setup",
                  choices = c("2D", "3D", "Shovelomics"), selected = "2D"),
      
      checkboxInput("saveRSML", label = "Get corresponding RSML files", value = F),
            
      actionButton(inputId = "runMARIA", label="Unleash MARIA"),
      
      tags$hr(),
      
      img(src = "MARIA_logo.png", width = 200)
    ),
    
    # Show a plot of the generated distribution
    mainPanel(
      tabsetPanel(     
        tabPanel("Estimator distribution",
                 helpText("The histogram represent the distribution of the different estimators 
                          in the synthetic population. Green lines represent the loaded experimental data"),
                 h6(htmlOutput("caption1")),                  
                 tags$hr(),                 
                 selectInput("var", label = "Choose a variable to display",
                             choices = estimators, selected = estimators[1]),
                 plotOutput("distPlot"),
                 value=1
        ),
        tabPanel("Matching procedure",
                 helpText("The histogram represent the theoretical distribution of distances after the matching procedure. 
                          Green lines represent the distances of the loaded experimental data"),
                 tags$hr(),                 
                 plotOutput("matching"),
                 value = 2),
        tabPanel("Parameter distribution",
                 helpText("The histogram represent the distribution of the different parameters 
                          in the synthetic population. Green lines represent the loaded experimental data. 
                          Red areas highlight value were the theoritical error is greater than 5%."),
                 tags$hr(),                 
                 selectInput("param", label = "Choose a parameters to display", choices = ground_truths, selected = ground_truths[1]),
                 plotOutput("resultPlot"),
                 value=3
        ),    
        tabPanel("Visual comparison",
                 helpText("Visualy compare the orginal image output and the matched image. 
                          Original images path is written in the output file of MARIA-R. 
                          The Matched image is regenerated using ArchiSimple and is therefore slightly different than the one used for the matching.
                          Requires Java to run."),
                 tags$hr(),                                  
                 selectInput("imid", label = "Choose an image to display", choices = c(1:20), selected = 1),
                 flowLayout(
                   verticalLayout(h4("Experiment"),tags$hr(),imageOutput("rootImage")),
                   verticalLayout(h4("Match"),tags$hr(),imageOutput("rootMatch"))
                 ),
                 value=4
        ),          
        tabPanel("Download results",
                 tags$hr(),
                 downloadButton('downloadData', 'Download'),
                 tags$hr(),                 
                 tableOutput('results'),
                 value = 5),
        id="tabs1"
      )
    )
  )
))