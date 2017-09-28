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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * Manager that handles tickets so that requests can be dispatched faster.
 *
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
@Service
public class BuildManager {

	public void add(Build build) {
		_builds.put(build.getBuildId(), build);
	}

	public Build get(String buildId) {
		if (!_builds.containsKey(buildId)) {
			_log.debug("Request for non existing build: " + buildId);

			return null;
		}

		return _builds.get(buildId);
	}

	private static final Logger _log = LoggerFactory.getLogger(
		BuildManager.class);

	private Map<String, Build> _builds = new HashMap<>();

}