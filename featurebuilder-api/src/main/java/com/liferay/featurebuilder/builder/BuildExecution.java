/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 * <p>
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.featurebuilder.builder;

import static java.util.Collections.singleton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julio Camarero
 */
public class BuildExecution {

	public BuildExecution(
		String branch, String devOptionId, File directory, String featureId,
		File patch, String userName, CredentialsManager credentialsManager,
		String githubRepo, String githubRepoCloneURL) {

		_branch = branch;
		_devOptionId = devOptionId;
		_directory = directory;
		_featureId = featureId;
		_patch = patch;
		_userName = userName;
		_credentialsManager = credentialsManager;
		_githubRepo = githubRepo;
		_githubRepoCloneURL = githubRepoCloneURL;
	}

	public void cleanUp() throws IOException {
		FileUtils.deleteDirectory(_directory);

		if (_git != null) {
			_git.close();
		}
	}

	public String execute() throws GitAPIException, IOException {
		_cloneRepo();

		_checkoutNewBranch();

		_applyPatch();

		_push();

		PullRequest pullRequest = _createPullRequest();

		String pullRequestURL = pullRequest.getHtmlUrl();

		_log.debug("Pull request successfully sent at: " + pullRequestURL);

		return pullRequestURL;
	}

	public void setDefaultBranch(String defaultBranch) {
		_defaultBranch = defaultBranch;
	}

	private void _applyPatch() throws FileNotFoundException, GitAPIException {
		//File patch = _getPatch(featureId, devOptionId);

		_log.debug("Applying patch: " + _patch.getName());

		_git.apply()
			.setPatch(new FileInputStream(_patch))
			.call();

		_git.add()
			.addFilepattern(".")
			.call();

		_git.commit()
			.setAll(true)
			.setCommitter(
				_credentialsManager.getGithubUser(),
				_credentialsManager.getGithubUserEmail())
			.setMessage(
				"Adding Feature " + _featureId + " - Option: " + _devOptionId)
			.call();
	}

	private void _checkoutNewBranch() throws GitAPIException {
		_log.debug("Creating a new branch called " + _branch);

		_git.checkout()
			.setCreateBranch(true)
			.setName(_branch)
			.call();
	}

	private void _cloneRepo() throws GitAPIException {
		_log.debug(
			"Clonning git repo " + _githubRepoCloneURL + " in dir " +
				_directory.getAbsolutePath());

		String defaultBranch = "master";

		_git = Git.cloneRepository()
			.setURI(_githubRepoCloneURL)
			.setDirectory(_directory)
			.setBranchesToClone(singleton(_REF_PREFIX + defaultBranch))
			.setBranch(_REF_PREFIX + defaultBranch)
			.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(
					_credentialsManager.getGithubUser(),
					_credentialsManager.getGithubPassword()))
			.call();
	}

	private PullRequest _createPullRequest() throws IOException {
		_log.debug("Sending pull request to github...");

		GitHubClient gitHubClient = new GitHubClient();

			gitHubClient.setCredentials(
				_credentialsManager.getGithubUser(),
				_credentialsManager.getGithubPassword());

		PullRequestService pullRequestService = new PullRequestService(
			gitHubClient);

		PullRequest pullRequest = new PullRequest();

		pullRequest.setTitle(
			_userName + " - Changes adding feature: " + _featureId);
		pullRequest.setBody(
			"We are building feature: " + _featureId + " using the option " +
			_devOptionId);

		PullRequestMarker base = new PullRequestMarker();

		base.setLabel(_defaultBranch);
		pullRequest.setBase(base);

		PullRequestMarker head = new PullRequestMarker();

		head.setLabel(_branch);

		pullRequest.setHead(head);

		return pullRequestService.createPullRequest(_getRepo(), pullRequest);
	}

	private Repository _getRepo() {
		String[] repoNameParts = _githubRepo.split("/");

		Repository repo = new Repository();

		User user = new User();

		user.setLogin(repoNameParts[0]);

		repo.setOwner(user);
		repo.setName(repoNameParts[1]);

		return repo;
	}

	private void _push() throws GitAPIException {
		_log.debug("Pushing code to github...");

		_git.push()
			.setRefSpecs(new RefSpec(_REF_PREFIX + _branch))
			.setCredentialsProvider(
				new UsernamePasswordCredentialsProvider(
					_credentialsManager.getGithubUser(),
					_credentialsManager.getGithubPassword()))
			.call();
	}

	private static final String _REF_PREFIX = "refs/heads/";

	private static final Logger _log = LoggerFactory.getLogger(
		BuildExecution.class);

	private String _branch;
	private CredentialsManager _credentialsManager;
	private String _defaultBranch = "master";
	private String _devOptionId;
	private File _directory;
	private String _featureId;
	private Git _git;
	private String _githubRepo;
	private String _githubRepoCloneURL;
	private File _patch;
	private String _userName;

}