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

import com.liferay.featurebuilder.model.Build;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.Random;

import org.eclipse.jgit.api.errors.GitAPIException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
@Service
public class FeatureBuilder {

	public void build(Build build) throws GitAPIException, IOException {
		String branchName = _getBranchName(build.getUserName());

		File dir = _getCloneDir(branchName);

		File patch = _getPatch(build.getFeatureId(), build.getDevOptionId());

		BuildExecution buildExecution = new BuildExecution(
			build, branchName, dir, patch, _credentialsManager, _githubRepo,
			_githubRepositoryCloneURL);

		try {
			buildExecution.execute();
		}
		catch (Exception e) {
			_log.error("Error building the feature", e);
		}
		finally {
			buildExecution.cleanUp();
		}
	}

	private String _getBranchName(String userName) {
		return _branchPrefix + "-" + userName + "-" + _random.nextLong();
	}

	private File _getCloneDir(String branch) {
		String path = "/tmp/" + branch + String.valueOf(_random.nextLong());

		return new File(path);
	}

	private File _getPatch(String featureId, String devOptionId) {
		ClassLoader classLoader = getClass().getClassLoader();

		URL resource = classLoader.getResource("change_button.patch");

		return new File(resource.getFile());
	}

	private static final Logger _log = LoggerFactory.getLogger(
		FeatureBuilder.class);

	@Value("${branch.prefix}")
	private String _branchPrefix;

	@Autowired
	private CredentialsManager _credentialsManager;

	@Value("${github.repository}")
	private String _githubRepo;

	@Value("${github.repository.clone.url}")
	private String _githubRepositoryCloneURL;

	private Random _random = new Random();

}