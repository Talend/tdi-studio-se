%~d0
cd %~dp0
java -Dtalend.component.manager.m2.repository=%cd%/../lib ${talend.job.jvmargs} -cp ${talend.job.bat.classpath} ${talend.job.class} ${talend.job.bat.addition} %*