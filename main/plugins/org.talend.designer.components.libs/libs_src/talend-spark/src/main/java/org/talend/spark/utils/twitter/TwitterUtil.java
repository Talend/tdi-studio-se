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
package org.talend.spark.utils.twitter;

import twitter4j.HashtagEntity;
import twitter4j.Status;

public class TwitterUtil {

	public static Object parse(TwitterParameter parameter, Status status) {
		if (parameter == TwitterParameter.USERNAME) {
			return status.getUser().getName();
		} else if (parameter == TwitterParameter.TEXT) {
			return status.getText();
		} else if (parameter == TwitterParameter.SOURCE) {
			return status.getSource();
		} else if (parameter == TwitterParameter.ACCESSLEVEL) {
			return status.getAccessLevel();
		} else if (parameter == TwitterParameter.DATE) {
			return status.getCreatedAt();
		} else if (parameter == TwitterParameter.ID) {
			return status.getId();
		} else if (parameter == TwitterParameter.GEOLOCATION_LATITUDE) {
			return status.getGeoLocation().getLatitude();
		} else if (parameter == TwitterParameter.GEOLOCATION_LONGITUDE) {
			return status.getGeoLocation().getLongitude();
		} else if (parameter == TwitterParameter.HASHTAG) {
			String hashTags = "";
			HashtagEntity[] hashTagArray = status.getHashtagEntities();
			for (int i = 0; i < hashTagArray.length; i++) {
				if (hashTags.equals("")) {
					hashTags = hashTagArray[i].getText();
				} else {
					hashTags = hashTags + "," + hashTagArray[i].getText();
				}
			}
			return hashTags;
		}
		return null;
	}

}
