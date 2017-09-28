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

import static java.util.Collections.singleton;

import com.liferay.featurebuilder.model.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import java.text.Normalizer;

import java.util.Random;

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
public class BuildExecution implements Runnable {

	public BuildExecution(
		Build build, String branchPrefix, CredentialsManager credentialsManager,
		String githubRepo, String githubRepoCloneURL) {

		_build = build;
		_branchPrefix = branchPrefix;
		_credentialsManager = credentialsManager;
		_githubRepo = githubRepo;
		_githubRepoCloneURL = githubRepoCloneURL;

		_build.addLog("Execution waiting in Queue.");
	}

	public void run() {
		try {
			_initialize();

			_cloneRepo();

			_checkoutNewBranch();

			_applyPatch();

			_push();

			_createPullRequest();

			_status = "OK";
		}
		catch (GitAPIException gapie) {
			_build.addLog("Error - Exception from Git Client.");

			_log.error("Exception from Git Client.", gapie);

			_status = "The job can't be executed: Exception from Git Client.";
		}
		catch (FileNotFoundException fnfe) {
			_build.addLog("Error - Exception trying to read patch file.");

			_log.error("Unable to find a File.", fnfe);

			_status = "The job can't be executed: Unable to find a File.";
		}
		catch (IOException ioe) {
			_build.addLog("Error - Exception trying to write a file.");

			_log.error("Unable to write to a File.", ioe);

			_status = "The job can't be executed: Unable to write to a File.";
		}
		catch (Exception e) {
			_build.addLog("System Error");

			_log.error("System Error.", e);

			_status = "The job can't be executed: System Error.";
		}
		finally {
			_build.setFinished(true);

			try {
				_cleanUp();
			}
			catch (IOException ioe) {
				_log.error("Unable to clean up after execution.", ioe);
			}
		}
	}

	public void setDefaultBranch(String defaultBranch) {
		_defaultBranch = defaultBranch;
	}

	private void _applyPatch() throws FileNotFoundException, GitAPIException {
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
				"Adding Feature " + _build.getFeatureId() + " - Option: " +
					_build.getDevOptionId())
			.call();

		_build.addLog(
			"Developed Feature: " + _build.getFeatureId() + " - Option: " +
				_build.getDevOptionId());
	}

	private void _checkoutNewBranch() throws GitAPIException {
		_log.debug("Creating a new branch called " + _branch);

		_git.checkout()
			.setCreateBranch(true)
			.setName(_branch)
			.call();

		_build.addLog("Checked Branch: " + _branch);
	}

	private void _cleanUp() throws IOException {
		FileUtils.deleteDirectory(_directory);

		if (_git != null) {
			_git.close();
		}
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

		_build.addLog("Clonned Repo: " + _githubRepo);
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
			_build.getUserName() + " - Changes adding feature: " +
				_build.getFeatureId());
		pullRequest.setBody(
			"We are building feature: " + _build.getFeatureId() +
				" using the option " + _build.getDevOptionId());

		PullRequestMarker base = new PullRequestMarker();

		base.setLabel(_defaultBranch);
		pullRequest.setBase(base);

		PullRequestMarker head = new PullRequestMarker();

		head.setLabel(_branch);

		pullRequest.setHead(head);

		pullRequest = pullRequestService.createPullRequest(
			_getRepo(), pullRequest);

		_log.debug(
			"Pull request successfully sent at: " + pullRequest.getHtmlUrl());

		_build.addLog("Pull request sent: " + pullRequest.getNumber());
		_build.setPullRequestURL(pullRequest.getHtmlUrl());

		return pullRequest;
	}

	private File _getPatch(Build build) throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();

		StringBuilder sb = new StringBuilder();

		sb.append("feature-");
		sb.append(build.getFeatureId());
		sb.append("-option-");
		sb.append(build.getDevOptionId());
		sb.append(".patch");

		URL resource = classLoader.getResource(sb.toString());

		if (resource == null) {
			throw new IOException("Can't find file " + sb.toString());
		}

		return new File(resource.getFile());
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

	private void _initialize() throws IOException {
		_build.addLog("Starting execution...");

		_random = new Random();

		_patch = _getPatch(_build);

		// Remove non ascii characters

		String normalizedUserName =
			Normalizer
				.normalize(_build.getUserName(), Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");

		// Remove whitespaces

		normalizedUserName = normalizedUserName.replaceAll("\\s+", "");

		_branch =
			_branchPrefix + "-" + normalizedUserName + "-" + _random.nextLong();

		_directory = new File(
			"/tmp/" + _branch + String.valueOf(_random.nextLong()));
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

		_build.addLog("Code pushed to Github");
	}

	private static final String _REF_PREFIX = "refs/heads/";

	private static final Logger _log = LoggerFactory.getLogger(
		BuildExecution.class);

	private String _branch;
	private String _branchPrefix;
	private Build _build;
	private CredentialsManager _credentialsManager;
	private String _defaultBranch = "master";
	private File _directory;
	private Git _git;
	private String _githubRepo;
	private String _githubRepoCloneURL;
	private File _patch;
	private Random _random;
	private String _status;

}