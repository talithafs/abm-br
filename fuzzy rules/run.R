sum = 0
n = ncol(l.series) - 1

for(i in 2:n){
  sum = sum + choose(n,i)
}

len = length(l.series)
results = vector(mode = "list", sum)
selic = l.series[[len]]
r.selic = c(min(selic),max(selic))
id = 1

for(i in 2:n){
  
  combs = combn(names(l.series)[-len],i)
  
  for(j in 1:ncol(combs)){
    
    print(paste("-- Vars:", paste0(combs[,j],collapse = ", ")))
    
    range = data.frame(matrix(ncol = i + 1, nrow = 2))
    training = data.frame(matrix(ncol = (i + 1), nrow = nrow(l.series)))
    names(training) = c(combs[,j],"selic.target")
    names(range) = names(training)
    
    for(k in 1:i){
      name = combs[k,j]
      series = l.series[,name]
      range[,k] = c(min(series),max(series)) 
      training[,k] = series
    }

    range[,i+1] = r.selic
    training[,i+1] = selic
    
    results[[id]]$vars = paste0(combs[,j], collapse = ", ")
    results[[id]]$id = id
    
    nrules.vec = seq(3,7,2)
    rmse = 1.797693e+308
    
    for(nrules in nrules.vec){
      
      tryCatch({
        
        acc = 0
        for(p in 1:30){
          
          shuffled = training[sample(nrow(training)), ]
          train <- shuffled[1:(rows-10), ]
          test <- shuffled[(rows-9):rows, 1:i]
          actual <- matrix(shuffled[(rows-9):rows, i+1], ncol = 1)
          
          fsi <- frbs.learn(train, range.data = range, method.type = "WM", control = list(num.labels = nrules, type.mf = "GAUSSIAN", type.defuz = "COG"))
          predicted = stats::predict(fsi , test)
          acc = acc + forecast::accuracy(ts(predicted),ts(actual))[2]
        }
        
        acc = acc/30 
        
        if(acc < rmse){
          
          rmse = acc
          
          results[[id]]$rmse = rmse
          results[[id]]$nlabels = nrules
          results[[id]]$training = shuffled
          results[[id]]$actual = actual
          results[[id]]$predicted = predicted
          results[[id]]$fsi = fsi
        }
      },
      
      error = function(e) {
        
      })
    }
   
    print(paste0("--- Completed ",round(100*id/sum,3),"%"))
    id = id + 1
  }
}

rankm = data.frame(matrix(ncol = 3, nrow = length(results)))

for(i in 1:length(results)){
  rankm[i,1] = results[[i]]$rmse
  rankm[i,3] = results[[i]]$vars
  rankm[i,2] = results[[i]]$nlabels
}

rankm[order(rankm[,1],rankm[,2]),]

# hm = meirelles_results[[22]]$best
# hm_rules = meirelles_results[[22]]$object[22][[1]]$rule
# par(mar=c(2,2,2,2))
# plotMF(hm_rules)

