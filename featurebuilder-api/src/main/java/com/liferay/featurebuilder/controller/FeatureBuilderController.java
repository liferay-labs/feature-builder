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

import com.liferay.featurebuilder.builder.FeatureBuilder;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * API for building features in a github project.
 *
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
@Controller
@RequestMapping("/build")
public class FeatureBuilderController {

	@GetMapping("/test")
	@ResponseBody
	public String test() {
		return "hello";
	}

	/**
	 * Builds a Feature for a project and creates a pull request with the
	 * changes. It automatically redirects to the pull request.
	 *
	 * @param userName - the name of the user building the feature
	 * @param featureId - the ID of the feature to build
	 * @param devOptionId - the ID of the option chosen to build the feature
	 */
	@PostMapping
	@ResponseBody
	public String build(
		@RequestParam(required = false, defaultValue = "pepe") String userName,
		@RequestParam String featureId,
		@RequestParam String devOptionId) throws GitAPIException, IOException {

		_log.debug(
			"Processing build from " + userName + ". Feature: " + featureId +
			" - Option: " + devOptionId);

		String pullRequestURL = _featureBuilder.build(
			userName, featureId, devOptionId);;

		return "redirect:" + pullRequestURL;
	}

	@Autowired
	private FeatureBuilder _featureBuilder;

	private static final Logger _log = LoggerFactory.getLogger(
		FeatureBuilderController.class);

}