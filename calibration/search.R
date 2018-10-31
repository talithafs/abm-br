path <- file.path(getwd(),"corrs")
csvs <- system(paste0("ls ",path),intern = T)

beveridge <- NULL
okun <- NULL
counter <- NULL

for(i in 1:length(csvs)){
  
  df <- read.csv(file.path(path,csvs[i]),stringsAsFactors = F)
  
  if(grepl("beveridge",csvs[i])){
    beveridge <- df
  }
  else if(grepl("counter",csvs[i])){
    counter <- df
  }
  else if(grepl("okun",csvs[i])) {
    okun <- df
  }
}

ordered <- vector(mode="list",length = 4)

ordered[[1]] <- okun[order(okun$GdpDev.UnempRate),][,c(1,13)]
ordered[[2]] <- okun[order(okun$Inflation.UnempRate),][,c(1,14)]
ordered[[3]] <- beveridge[order(beveridge$VacanRatio.UnempRate),][,c(1,13)]
ordered[[4]] <- counter[order(counter$GdpGrowth.UnempRate),][,c(1,13)]

points <- data.frame(matrix(nrow=nrow(beveridge),ncol=2))
points[,1] <- beveridge[,1]
points[,2] <- rep(0,nrow(points))
names(points) <- c("id","points")

for(i in 1:nrow(points)){
  
  id <- points[i,1]
  
  for(j in 1:4){

    inx <- which(ordered[[j]]$run == id)
    points[i,2] = points[i,2] + inx 
  }
}

View(points[order(points$points),])
