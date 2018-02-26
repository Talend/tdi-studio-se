$fileDir = Split-Path -Parent $MyInvocation.MyCommand.Path
cd $fileDir
java ${talend.job.jvmargs.ps1} -cp ${talend.job.ps1.classpath} ${talend.job.class} ${talend.job.bat.addition} %* 