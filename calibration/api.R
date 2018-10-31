get.corr <- function(series1, series2){
  return(round(as.numeric(cor.test(series1, series2)$estimate),2))
}

calc.corrs <- function(df,type){
  
  if(type == 1) {
    corr1 = get.corr(df$GdpDev, df$UnempRate)
    corr2 = get.corr(df$Inflation, df$UnempRate)
    return(data.frame("GdpDev.UnempRate" = corr1, "Inflation.UnempRate" = corr2))
  }
  else if(type == 2) {
    corr = get.corr(df$UnempRate, df$VacanRatio)
    return(data.frame("VacanRatio.UnempRate" = corr))
  }
  else if(type == 3) {
    corr = get.corr(df$UnempRate, df$GdpGrowth)
    return(data.frame("GdpGrowth.UnempRate" = corr))
  }
  else {
    return(NULL)
  }
}

write.corrs <- function(pars,type,time){
  
  csv <- NULL
  
  if(type == 1){
    csv <- paste0("../../corrs/okun_",time,".csv")
  } 
  else if(type == 2){
    csv <- paste0("../../corrs/beveridge_",time,".csv")
  }
  else {
    csv <- paste0("../../corrs/counter_cycle_",time,".csv")
  }
  
  if(!file.exists(csv)){
    file.create(csv)
    write(paste(names(pars),collapse = ","),file = csv)
  }
  
  write(paste(as.character(pars),collapse = ","), file = csv, append = T)
}

get.type <- function(type){
  if(type == 1){
    return("okun")
  } 
  else if(type == 2){
    return("beveridge")
  }
  else {
    return("countercycle")
  }
}
