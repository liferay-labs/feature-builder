/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.featurebuilder.builder;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * @author Julio Camarero
 */
@Service
public class CredentialsManager {

	public String getGithubPassword() {
		String githubPassword = System.getenv("githubPassword");

		if (Objects.isNull(githubPassword)) {
			_log.error("Missing githubPassword environment variable");
		}

		return githubPassword;
	}

	public String getGithubUser() {
		String githubUser = System.getenv("githubUser");

		if (Objects.isNull(githubUser)) {
			_log.error("Missing githubUser environment variable");
		}

		return githubUser;
	}

	public String getGithubUserEmail() {
		String githubUserEmail = System.getenv("githubUserEmail");

		if (Objects.isNull(githubUserEmail)) {
			_log.error("Missing githubUserEmail environment variable");
		}

		return githubUserEmail;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		CredentialsManager.class);

}