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

package com.liferay.featurebuilder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Julio Camarero
 * @author Cristina Gonzalez
 */
public class Build {

	public Build(String userName, String featureId, String devOptionId) {
		_userName = userName;
		_featureId = featureId;
		_devOptionId = devOptionId;
		_logs = new ArrayList<>();
		_finishStatus = "OK";

		_buildId = UUID.randomUUID().toString();
	}

	public void addLog(String log) {
		_logs.add(log);
	}

	public String getBuildId() {
		return _buildId;
	}

	public String getDevOptionId() {
		return _devOptionId;
	}

	public String getFeatureId() {
		return _featureId;
	}

	public String getFinishStatus() {
		return _finishStatus;
	}

	public List<String> getLogs() {
		return _logs;
	}

	public String getPullRequestURL() {
		return _pullRequestURL;
	}

	public String getUserName() {
		return _userName;
	}

	public boolean isFinished() {
		return _finished;
	}

	public void setFinished(boolean finished) {
		_finished = finished;
	}

	public void setFinishStatus(String finishStatus) {
		_finishStatus = finishStatus;
	}

	public void setPullRequestURL(String pullRequestURL) {
		_pullRequestURL = pullRequestURL;
	}

	private String _buildId;
	private String _devOptionId;
	private String _featureId;
	private boolean _finished = false;
	private String _finishStatus;
	private List<String> _logs;
	private String _pullRequestURL;
	private String _userName;

}