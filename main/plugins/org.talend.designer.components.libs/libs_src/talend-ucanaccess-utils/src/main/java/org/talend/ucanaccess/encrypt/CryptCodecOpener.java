package org.talend.ucanaccess.encrypt;

import java.io.File;
import java.io.IOException;

import net.ucanaccess.jdbc.JackcessOpenerInterface;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;

public class CryptCodecOpener implements JackcessOpenerInterface {
	public Database open(File fl, String pwd) throws IOException {
		DatabaseBuilder dbd = new DatabaseBuilder(fl);
		//dbd.setAutoSync(false);
		dbd.setCodecProvider(new CryptCodecProvider(pwd));
		dbd.setReadOnly(false);
		return dbd.open();
	}
}