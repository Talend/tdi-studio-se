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
package org.talend.spark.function;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.talend.spark.utils.twitter.TwitterParameter;
import org.talend.spark.utils.twitter.TwitterUtil;

import twitter4j.Status;

public class LoadTwitterFunction implements Function<Status, List<Object>> {

	private static final long serialVersionUID = 1L;
	private List<TwitterParameter> twitterParameters;

	public LoadTwitterFunction(List<TwitterParameter> twitterParameters) {
		this.twitterParameters = twitterParameters;
	}

	public List<Object> call(Status status) throws Exception {
		List<Object> list = new ArrayList<Object>();
		for (TwitterParameter parameter : twitterParameters) {
			list.add(TwitterUtil.parse(parameter, status));
		}

		return list;
	}

}
