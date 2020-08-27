1. (2009-10-16 modify by wyang) to close the verbose in console 

Retrieving document at http://euro2008.dataaccess.eu/footballpoolwebservice.wso?WSDL'.

ServiceDiscoveryHelper.java

please @see talend-ws-1.0.jar\org\talend\ws\helper\ServiceDiscoveryHelper.java

2.(2009-10-20 modify by bchen) to work for this webservice: 'http://euro2008.dataaccess.eu/footballpoolwebservice.wso?WSDL'

ListPropertyMapper.java
SimplePropertyMapper.java

please @see talend-ws-1.0.jar\org\talend\ws\mapper\ListPropertyMapper.java
	    	talend-ws-1.0.jar\org\talend\ws\mapper\SimplePropertyMapper.java
	    	
3.(2009-11-18 modified by bchen) fixed bug 9900

for wrap type of webservice: update to cxf-2.2.4.jar, jaxb-xjc-2.1.12.jar and jaxb-impl-2.1.12.jar.
						     modified ListPropertyMapper.java
for bare type of webservice: modified ServiceInvokerHelper.java

please @see talend-ws-1.0.jar\org\talend\ws\mapper\ListPropertyMapper.java
			talend-ws-1.0.jar\org\talend\ws\helper\ServiceInvokerHelper.java 
			
4.(2009-11-18 modified by bchen) support for this webservice:http://www.ebi.ac.uk/intact/binary-search-ws/binarysearch?wsdl
consider the situation about no targetnamespace in schema

modified ServiceInvokerHelper.java

please @see talend-ws-1.0.jar\org\talend\ws\helper\ServiceInvokerHelper.java 

5.(2009-11-23 modified by bchen) fiexd bug 8674

please @see org\talend\ws\helper\ServiceDiscoveryHelper.java
			org\talend\ws\helper\ServiceInvokerHelper.java
			
6.(2010-01-05 modified by bchen) fiexd bug 8674

please @see org\talend\ws\helper\ServiceDiscoveryHelper.java
			org\talend\ws\helper\ServiceInvokerHelper.java
			
7.(2010-02-01 modified by bchen) fixed bug 11351

8.(2010-02-23 modified by bchen) fixed bug 11351 add an ability to config temporary path for wsdl file.

9.(2010-03-16 modified by bchen) fixed bug 11917 
please @see org\talend\ws\helper\ServiceInvokerHelper.java
			org\talend\ws\mapper\MapperFactory.java
			
10.(2019-01-18 modified by dchmyga) fixed TDI-41647

11.(2020-08-24 modified by ozhelezniak) updated commons-codec to 1.14 in scope of TDI-44145