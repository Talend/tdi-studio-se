// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.datastewardship.server.task.creation;


/**
 * DOC Starkey  class global comment. Detailled comment
 */
public class TaskLoadClientTools {

    public static void main(String[] args) {

        if(args.length!=5) {
            usage();
            return;
        }

        String host= args[0];
        String port=args[1];
        String username=args[2];
        String password= args[3];
        String taskdata= args[4];

        String url = getUrl(host, port, true);

        TaskLoadClient taskLoadClient=new TaskLoadClient(url,username,password);
        try {
            boolean result=taskLoadClient.doLoad(taskdata);
            if(result)System.out.println("Tasks loaded successfully! ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUrl(String context,String host, String port, boolean isNewServer) {
		if (context == null || context.length() == 0) {
			if(isNewServer){
				context = "/talendmdm";
			} else {
				context = "/org.talend.datastewardship";
			}
		}

		String url="http://"+host+":"+port+context+"/dataloader";
		if(isNewServer){
			url="http://"+host+":"+port+context+"/services/dsctaskloader";
		} else {
			url="http://"+host+":"+port+context+"/dataloader";
		}

        return url;
    }

    public static String getUrl(String host, String port, boolean isNewServer) {
        return getUrl(null,host,port, isNewServer);
    }

    private static void usage() {
        String usage="Usage:\n"+
        "\t java -jar taskloadclient.jar <host> <port> <username> <password> <taskdata> \n"+
        "\t example: java -jar taskloadclient.jar localhost 8080 admin admin \"<Tasks><Task><taskType>1</taskType><createdBy>tester</createdBy><owner>tester</owner></Task></Tasks>\"";

        System.out.println(usage);
    }

}
