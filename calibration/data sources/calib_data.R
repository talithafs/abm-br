library(BETS)
library(zoo)
library(lubridate)

stdz.dates <- function(...){
  list.series <- list(...)
  
  max <- as.Date("1900-01-01")
  min <- as.Date("3000-01-01")
  
  for(series in list.series){
    
    s <- as.Date(as.yearmon(paste(start(series),collapse = "-")))
    e <- as.Date(as.yearmon(paste(end(series),collapse = "-")))
    
    if(s > max){
      max <- s
    }
    
    if(e < min){
      min <- e
    }
  }
  
  for(i in 1:length(list.series)){
    list.series[[i]] <- window(list.series[[i]], start = c(year(max),month(max)), end = c(year(min),month(min)))
  }
  
  return(list.series)
}

## -- Inflation
infl <- window(BETSget(433),start = c(1995,1))

## -- Unemployment
#unemp <- BETSget(24369)
# unemp.mean <- mean(unemp)
unemp <- read.csv("desocupacao.csv")
unemp <- ts(unemp[,2],start=c(2002,3),frequency = 12)

## -- 30 days employed (% workforce)
perm <- read.csv("permanencia.csv")
perm <- ts(perm[,2],start=c(2002,3),frequency = 12)

## -- Bankruptcy rate
bkrpt.f <- read.csv("inadimplencia_pj.csv")
bkrpt.f <- ts(bkrpt.f[,2], start=c(2011,3), frequency = 12)

bkrpt.c <- read.csv("inadimplencia_pf.csv")
bkrpt.c <- ts(bkrpt.c[,2], start=c(2011,3), frequency = 12)

## -- Output gap 
gap <- read.csv("hiato.csv", stringsAsFactors = F) 
gap$date <- as.yearqtr(gsub("T","Q",gap$date))
gap <- to.monthly(gap,freq="quarterly")

## -- Anual credit growth
# credit <- read.csv("cresc_credito.csv")
# mean.cred <- mean(credit[,2])
credit <- BETSget(20539)

## -- Capital to assets
cap <- BETSget(25627)
cap <- to.monthly(data.frame(as.yearqtr(as.Date(cap)),as.vector(cap)), freq = "quarterly")

## cap adequacy 
adeq <- BETSget(21819)
adeq <- to.monthly(data.frame(as.yearqtr(as.Date(adeq)),as.vector(adeq)), freq = "quarterly")

series <- stdz.dates(infl,unemp,perm,bkrpt.f,bkrpt.c,gap,credit,cap,adeq)
names(series) <- c("infl","unemp","perm","bkrpt.f","bkrpt.c","gap","credit","cap","adeq")

#infl.mean <- mean()
#infl.vol <- sd()
