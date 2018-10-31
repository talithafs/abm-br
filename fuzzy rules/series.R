require(BETS)
require(dplyr)
require(lubridate)
require(zoo)

to.monthly <- function(df, freq = "daily", format = NULL){
  
  if(freq == "daily"){
    names(df) <- c("date","value")
    
    if(!is.null(format)){
      df$date <- as.Date(df$date, format = format) 
    }
    
    tb <- (df %>% group_by(month=floor_date(date, "month")) %>%
             summarize(value=mean(value)))
    dat <- tb[2,1]$month
    first <- c(year(dat),month(dat))
    return(ts(tb[c(-1,-nrow(tb)),2],start=first,frequency=12))
  }
  else if(freq == "quarterly") {
    names(df) <- c("date","value")
    
    dat1 <- df[1,1]
    first <- c(year(dat1),quarter(dat1)*3 - 2)
    dat2 <- df[nrow(df),1] 
    last <- c(year(dat2),quarter(dat2)*3)
    
    ret <- ts(0, start=first, end=last, frequency=12)

    i = 1
    k = 1
    while(i != length(ret)+1){
      for(j in 1:3){
        ret[i] <- df$value[k]
        i <- i + 1
      }
      k <- k + 1
    }
    
    return(ret)
  } 
  else { # aqui so entra obj ts - deveria ser data.frame data,valor
    year1 <- start(df)[1]
    first <- c(year1,1)
    year2 <- end(df)[1] 
    last <- c(year2,12)
    
    ret <- ts(0, start=first, end=last, frequency=12)
    
    i = 1
    k = 1
    while(i != length(ret)+1){
      for(j in 1:12){
        ret[i] <- df[k]
        i <- i + 1
      }
      k <- k + 1
    }
    
    return(ret)
  }
}

cut.ends <- function(l.series){
  
  max.len <- -1
  for(i in 1:length(l.series)){
    len <- length(l.series[[i]])
    if(len > max.len){
      max.len <- len 
    }
  }
  
  df <- data.frame(matrix(nrow=max.len, ncol=length(l.series)))
  
  for(i in 1:length(l.series)){
    series <- rev(l.series[[i]])
    df[,i] <- c(series, rep(NA, max.len - length(series)))
  }
  
  names(df) <- names(l.series)
  return(na.omit(df))
}

ins.lags <- function(l.series, lags = c(1:3,12)){
  dims <- dim(l.series)
  ret <- data.frame(matrix(nrow = dims[1], ncol = ((dims[2]-1)*length(lags))+1))
  nms <- names(l.series)[-dims[2]]

  i = 1 
  lags <- lags[-1] - 1
  
  for(name in nms){
    series <- l.series[[name]]
    ret[,i] <- series
    names(ret)[i] <- name
    
    for(l in lags){
      i = i + 1
      rem <- 1:l
      lagged <- c(series[-rem],rep(NA,length(rem)))
      ret[,i] <- lagged
      names(ret)[i] <- paste0(name,"_",l+1)
    }
    
    i = i + 1
  }
  
  ret[i] <- l.series[[dims[2]]]
  names(ret)[i] <- names(l.series)[dims[2]]
  return(na.omit(ret))
}

# IPCA accumulated 12 months
ipca.accum <- BETSget(13522) # montlhy

# Swap DI 360 days
di.swap <- BETSget(7827) # monthly

# Inflation expectations (next 12 months)
ipca.expec <- read.csv("expectativas.csv",stringsAsFactors = F) # daily 
ipca.expec <- to.monthly(ipca.expec, format = "%d/%m/%Y")

# Real interest rates, ex-ante
di.swap <- window(di.swap, start=start(ipca.expec))
ri.exante <- di.swap - ipca.expec 

# SELIC target
selic.target <- BETSget(432) # daily
selic.target <- to.monthly(selic.target)

# Annual SELIC
selic.annual <- BETSget(1178) # daily
selic.annual <- round(to.monthly(selic.annual),2)

# GDP gap
gdp.gap <- read.csv("hiato.csv", stringsAsFactors = F) # quarterly
gdp.gap$date <- as.yearqtr(gsub("T","Q",gdp.gap$date))
gdp.gap <- to.monthly(gdp.gap,freq="quarterly")

# IPCA target
ipca.target <- BETSget(13521)
ipca.target <- window(to.monthly(ipca.target, freq="yearly"), end=end(selic.target))

# List of series
l.series <- list(gdp.gap, ri.exante, ipca.accum, ipca.target, selic.target)
names(l.series) <- c("gdp.gap","ri.exante","ipca.accum","ipca.target","selic.target")
l.series <- ins.lags(cut.ends(l.series))
l.series <- data.frame(lapply(l.series, BETS::normalize))


