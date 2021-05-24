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
package org.talend.spark.operation;

import java.util.List;

import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import org.talend.spark.function.LoadTwitterFunction;
import org.talend.spark.utils.twitter.TwitterParameter;

import twitter4j.Status;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterLoad<T> {

	public static JavaDStream<List<Object>> twitterStream(JavaStreamingContext ctx, String username, String password, String accessToken, String secretToken, String[] filters, List<TwitterParameter> twitterParameters) {
		twitter4j.conf.ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthAccessToken(accessToken);
		builder.setOAuthAccessTokenSecret(secretToken);
		builder.setOAuthConsumerKey(username);
		builder.setOAuthConsumerSecret(password);
		JavaDStream<Status> inputDStream = null;
		if(filters.length>0) {
			if(filters.length==1 && filters[0].equals("")) inputDStream = TwitterUtils.createStream(ctx, new OAuthAuthorization(builder.build()));
			else inputDStream = TwitterUtils.createStream(ctx, new OAuthAuthorization(builder.build()), filters);

			return inputDStream.map(new LoadTwitterFunction(twitterParameters));
		}
		return null;
	}
}
