# limpar diretorios temporarios (/temp e /Test)
# executar comando de batch em background
# trocar diretorio temporario apos aprox 10 microseg
# comecar a ler dados em loop, sem fechar os arquivos, gravando saída (correlacoes) em outro arquivo

setwd("/tmp")
system("rm -f -r /tmp/$(ls -I '*rsession*')")
setwd("/home/talithafs/Dropbox/Dissertação/Modelo/abm/output/")
system("rm -r -f /home/talithafs/Dropbox/Dissertação/Modelo/abm/output/*")
setwd("~/Dropbox/Dissertação/Modelo/calibration/tmp")
system("rm -r /home/talithafs/Dropbox/Dissertação/Modelo/calibration/tmp/*")

setwd("/home/talithafs/Dropbox/Dissertação/Modelo/abm")
cmd <- "java -cp $CLASSPATH:/home/talithafs/opt/eclipse/plugins/repast.simphony.runtime_2.5.0/bin/*:/home/talithafs/opt/eclipse/plugins/repast.simphony.runtime_2.5.0/bin/:/home/talithafs/opt/eclipse/plugins/repast.simphony.runtime_2.5.0/lib/*:/home/talithafs/opt/eclipse/plugins/repast.simphony.batch_2.5.0/bin/:/home/talithafs/opt/eclipse/plugins/repast.simphony.distributed.batch.ui_2.5.0/bin-standalone/:/home/talithafs/opt/eclipse/plugins/repast.simphony.distributed.batch_2.5.0/bin/:/home/talithafs/opt/eclipse/plugins/repast.simphony.core_2.5.0/lib/*:/home/talithafs/opt/eclipse/plugins/com.jcraft.jsch_0.1.54.v20170116-1932.jar:/home/talithafs/opt/eclipse/plugins/repast.simphony.bin_and_src_2.5.0/repast.simphony.bin_and_src.jar:lib/opencsv-4.1.jar:./bin/ repast.simphony.batch.standalone.StandAloneMain -hl -b batch/batch_params.xml -c batch/batch_configuration.properties -r"
system(cmd, wait = F)
Sys.sleep(10)

fileName <- "/home/talithafs/Dropbox/Dissertação/Modelo/abm/output/config.props"
con <- file(fileName,open="r")
line <- readLines(con)
line[9] <- "local.0.working_directory = /home/talithafs/Dropbox/Dissertação/Modelo/calibration/tmp"
writeLines(line, fileName)
close(con)

Sys.sleep(80)

setwd("~/Dropbox/Dissertação/Modelo/calibration/tmp")
out.dir <- paste0(getwd(), "/", system("ls",intern = T))
setwd(out.dir)
done <- paste0(out.dir,"/status_output.properties")

folder <- "instance_1"
cmd <- paste0("ls ", folder," | grep -v 'batch' | grep '.csv'| grep -v 'business'")
csvs <- system(cmd, intern = T)
cmd <- paste0("ls ", folder," | grep 'batch' | grep -v 'business'")
params <- system(cmd,intern = T)

okun <- csvs[grep("*okun*", csvs)]
beveridge <- csvs[grep("*beveridge*",csvs)]
counter <- csvs[grep("*counter*",csvs)]

names.okun <- read.csv(paste0(folder,"/",okun), header=F,nrows=1,stringsAsFactors = F)
names.beveridge <- read.csv(paste0(folder,"/",beveridge), header=F,nrows=1,stringsAsFactors = F)
names.counter <- read.csv(paste0(folder,"/",counter), header=F,nrows=1,stringsAsFactors = F)
names.params <-  as.character(read.csv(paste0(folder,"/",params[1]), header=F,nrows=1,stringsAsFactors = F))
  
sinks <- vector(mode="list",length = 3)
sinks[[1]] <- list(file = okun, names = as.character(names.okun), params = params[3])
sinks[[2]] <- list(file = beveridge, names =  as.character(names.beveridge),  params = params[1])
sinks[[3]] <- list(file = counter, names =  as.character(names.counter), params = params[2])
names(sinks) <-  c("okun", "beveridge","counter.cycle")

n.insts <- 6
n.ticks <- 72

last.line <- vector(mode="list",length = n.insts)
last.line <- lapply(last.line, function(x){x <- c(1,1,1)})
last.run <- vector(mode="list",length = n.insts)
last.run <- lapply(last.run, function(x){x <- c(1,1,1)})
#last.tick <- vector(mode="list",length = n.insts)
#last.tick <- lapply(last.tick, function(x){x <- c(0,0,0)})

frames <- vector(mode="list",length=n.insts)
frames <- lapply(frames, function(x){
  x <- list(okun = data.frame(matrix(ncol=length(names.okun),nrow=0)), 
            beveridge = data.frame(matrix(ncol=length(names.beveridge),nrow=0)), 
            counter.cycle = data.frame(matrix(ncol=length(names.counter),nrow=0)))
})


time <- gsub(":| |-","_",Sys.time())

while(TRUE){
  
  no.change <- TRUE
  instance <- "instance_"
  
  for(i in 1:n.insts){
    folder <- paste0(instance,i,"/")
    
    for(j in 1:3){
      
      sink <- sinks[[j]]$file
      cols <- sinks[[j]]$names
      params <- sinks[[j]]$params
      
      line <- tryCatch({
        read.csv(paste0(folder,sink),header=F, skip=last.line[[i]][j],stringsAsFactors = F, nrows = 1)
      }, error = function(e) {
        return(NULL)
      })
      
      if(!is.null(line)){
        no.change <- FALSE
        names(line) <- cols
        tick <- line$tick
        
        if(nrow(frames[[i]][[j]]) == 0){
          names(frames[[i]][[j]]) <- cols
        }
        
        last.line[[i]][j] <- last.line[[i]][j] + 1
        frames[[i]][[j]] <- rbind(frames[[i]][[j]],data.frame(line))
        
        if(tick == n.ticks){
          run <- line$run
          print(paste0("Run number ", run, ": ", get.type(j)))
          pars <- read.csv(paste0(folder,params),header=F,skip=last.run[[i]][j],stringsAsFactors = F, nrows = 1)
          corrs <- calc.corrs(frames[[i]][[j]], j)
          pars <- cbind(pars,corrs)
          names(pars) <- c(names.params,names(corrs))
          write.corrs(pars,j,time)
          frames[[i]][[j]] <- data.frame(matrix(nrow=0,ncol=length(line)))
          last.run[[i]][j] <- last.run[[i]][j] + 1
        }
      }
    }
      
  }
  
  if(file.exists(done) && no.change){
    break 
  }
}

