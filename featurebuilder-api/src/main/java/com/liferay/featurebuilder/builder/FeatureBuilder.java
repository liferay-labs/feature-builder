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

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.util.Collections.singleton;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

/**
 *
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
@Service
public class FeatureBuilder {

	public String build(
			String userName, String featureId, String devOptionId)
		throws GitAPIException, IOException {

		String branch = _getBranchName(userName);

		File dir = _getCloneDir(branch);

		Git git = null;

		try {
			git = _cloneRepo(dir, branch, featureId, devOptionId);
		}
		finally {
			FileUtils.deleteDirectory(dir);

			if (git != null) {
				git.close();
			}
		}

		String pullRequestURL = "";

		return pullRequestURL;
	}

	private Git _cloneRepo(
			File cloneDir, String branch, String featureId, String devOptionId)
		throws GitAPIException, FileNotFoundException {

		_log.debug(
			"Clonning git repo " + _githubRepositoryCloneURL + " in dir " +
				cloneDir.getAbsolutePath());

		String defaultBranch = "master";

		Git git = Git.cloneRepository()
			.setURI(_githubRepositoryCloneURL)
			.setDirectory(cloneDir)
			.setBranchesToClone(singleton(_REF_PREFIX + defaultBranch))
			.setBranch(_REF_PREFIX + defaultBranch)
			.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(
					_credentialsManager.getGithubUser(),
					_credentialsManager.getGithubPassword()))
			.call();

		_log.debug("Listing branches: ");

		git.branchList()
			.call();

		_log.debug("Creating a new branch called " + branch);

		git.checkout()
			.setCreateBranch(true)
			.setName(branch)
			.call();


		File patch = _getPatch(featureId, devOptionId);

		_log.debug("Applying patch: " + patch.getName());

		git.apply()
			.setPatch(new FileInputStream(patch))
			.call();

		git.add()
			.addFilepattern(".")
			.call();

		git.commit()
			.setAll(true)
			.setCommitter(
				_credentialsManager.getGithubUser(),
				_credentialsManager.getGithubUserEmail())
			.setMessage(
				"Adding Feature " + featureId + " - Option: " + devOptionId)
			.call();

		_log.debug("Pushing code to github...");

		git.push()
			.setRefSpecs(new RefSpec(_REF_PREFIX + branch))
			.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(
					_credentialsManager.getGithubUser(),
					_credentialsManager.getGithubPassword()))
			.call();

		return git;
	}

	private File _getPatch(String featureId, String devOptionId) {
		ClassLoader classLoader = getClass().getClassLoader();

		URL resource = classLoader.getResource("change_button.patch");

		return new File(resource.getFile());
	}

	private File _getCloneDir(String branch) {
		String path = "/tmp/" + branch + String.valueOf(_random.nextLong());

		return new File(path);
	}

	private String _getBranchName(String userName) {
		return _branchPrefix + "-" + userName + "-" + _random.nextLong();
	}

	private static final String _REF_PREFIX = "refs/heads/";

	private Random _random = new Random();

	@Autowired
	private CredentialsManager _credentialsManager;

	@Value("${github.respository.clone.url}")
	private String _githubRepositoryCloneURL;

	@Value("${branch.prefix}")
	private String _branchPrefix;

	private static final Logger _log = LoggerFactory.getLogger(
		FeatureBuilder.class);
}
