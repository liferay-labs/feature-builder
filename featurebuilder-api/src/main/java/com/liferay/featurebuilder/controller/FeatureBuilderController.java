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

package com.liferay.featurebuilder.controller;

import com.liferay.featurebuilder.builder.BuildExecution;
import com.liferay.featurebuilder.builder.BuildManager;
import com.liferay.featurebuilder.builder.CredentialsManager;
import com.liferay.featurebuilder.model.Build;

import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.eclipse.jgit.api.errors.GitAPIException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * API for building features in a github project.
 *
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
@Controller
@RequestMapping("/build")
public class FeatureBuilderController {

	/**
	 * Builds a Feature for a project and creates a pull request with the
	 * changes. It automatically redirects to the pull request.
	 *
	 * @param userName - the name of the user building the feature
	 * @param featureId - the ID of the feature to build
	 * @param devOptionId - the ID of the option chosen to build the feature
	 */
	@CrossOrigin
	@PostMapping
	@ResponseBody
	public String build(
			@RequestParam(defaultValue = "pepe", required = false)
				String userName,
			@RequestParam String featureId, @RequestParam String devOptionId)
		throws GitAPIException, IOException {

		_log.debug(
			"Processing build from " + userName + ". Feature: " + featureId +
				" - Option: " + devOptionId);

		Build build = new Build(userName, featureId, devOptionId);

		_buildManager.add(build);

		BuildExecution buildExecution = new BuildExecution(
			build, _branchPrefix, _credentialsManager, _githubRepo,
			_githubRepositoryCloneURL);

		_executorService.submit(buildExecution);

		return build.getBuildId();
	}

	@CrossOrigin
	@GetMapping("/{buildId}")
	@ResponseBody
	public Build pullRequestURL(@PathVariable String buildId) {
		return _buildManager.get(buildId);
	}

	@GetMapping("/test")
	@ResponseBody
	public String test() {
		return "hello";
	}

	@PostConstruct
	private void initializeExecutorService() {
		int processors = Runtime.getRuntime().availableProcessors();

		_log.debug(
			"Initializing Executor Service with a Thred Pool of " + processors +
				" Thread Workers.");

		_executorService = Executors.newFixedThreadPool(processors);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		FeatureBuilderController.class);

	@Value("${branch.prefix}")
	private String _branchPrefix;

	@Autowired
	private BuildManager _buildManager;

	@Autowired
	private CredentialsManager _credentialsManager;

	private ExecutorService _executorService;

	@Value("${github.repository}")
	private String _githubRepo;

	@Value("${github.repository.clone.url}")
	private String _githubRepositoryCloneURL;

}