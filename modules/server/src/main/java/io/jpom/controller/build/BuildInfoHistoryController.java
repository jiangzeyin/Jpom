/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 码之科技工作室
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.jpom.controller.build;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.Page;
import cn.hutool.extra.servlet.ServletUtil;
import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.common.validator.ValidatorConfig;
import cn.jiangzeyin.common.validator.ValidatorItem;
import cn.jiangzeyin.common.validator.ValidatorRule;
import io.jpom.build.BuildUtil;
import io.jpom.common.BaseServerController;
import io.jpom.model.BaseEnum;
import io.jpom.model.PageResultDto;
import io.jpom.model.data.BuildInfoModel;
import io.jpom.model.data.UserModel;
import io.jpom.model.enums.BuildStatus;
import io.jpom.model.log.BuildHistoryLog;
import io.jpom.model.vo.BuildHistoryLogVo;
import io.jpom.plugin.ClassFeature;
import io.jpom.plugin.Feature;
import io.jpom.plugin.MethodFeature;
import io.jpom.service.dblog.BuildInfoService;
import io.jpom.service.dblog.DbBuildHistoryLogService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * new version for build info history controller
 *
 * @author Hotstrip
 * @since 2021-08-26
 */
@RestController
@Feature(cls = ClassFeature.BUILD)
public class BuildInfoHistoryController extends BaseServerController {

	@Resource
	private BuildInfoService buildInfoService;
	@Resource
	private DbBuildHistoryLogService dbBuildHistoryLogService;

	/**
	 * 下载构建物
	 *
	 * @param logId 日志id
	 */
	@RequestMapping(value = "/build/history/download_file.html", method = RequestMethod.GET)
	@Feature(method = MethodFeature.DOWNLOAD)
	public void downloadFile(@ValidatorConfig(@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "没有数据")) String logId) {
		BuildHistoryLog buildHistoryLog = dbBuildHistoryLogService.getByKey(logId);
		if (buildHistoryLog == null) {
			return;
		}
		BuildInfoModel item = buildInfoService.getByKey(buildHistoryLog.getBuildDataId());
		if (item == null) {
			return;
		}
		File logFile = BuildUtil.getHistoryPackageFile(item.getId(), buildHistoryLog.getBuildNumberId(), buildHistoryLog.getResultDirFile());
		if (!FileUtil.exist(logFile)) {
			return;
		}
		if (logFile.isFile()) {
			ServletUtil.write(getResponse(), logFile);
		} else {
			File zipFile = BuildUtil.isDirPackage(logFile);
			assert zipFile != null;
			ServletUtil.write(getResponse(), zipFile);
		}
	}


	@RequestMapping(value = "/build/history/download_log.html", method = RequestMethod.GET)
	@ResponseBody
	@Feature(method = MethodFeature.DOWNLOAD)
	public void downloadLog(@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "没有数据") String logId) throws IOException {
		BuildHistoryLog buildHistoryLog = dbBuildHistoryLogService.getByKey(logId);
		Objects.requireNonNull(buildHistoryLog);
		BuildInfoModel item = buildInfoService.getByKey(buildHistoryLog.getBuildDataId());
		Objects.requireNonNull(item);
		File logFile = BuildUtil.getLogFile(item.getId(), buildHistoryLog.getBuildNumberId());
		if (!FileUtil.exist(logFile)) {
			return;
		}
		if (logFile.isFile()) {
			ServletUtil.write(getResponse(), logFile);
		}
	}

	@RequestMapping(value = "/build/history/history_list.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.LOG)
	public String historyList(String status,
							  @ValidatorConfig(value = {
									  @ValidatorItem(value = ValidatorRule.POSITIVE_INTEGER, msg = "limit error")
							  }, defaultVal = "10") int limit,
							  @ValidatorConfig(value = {
									  @ValidatorItem(value = ValidatorRule.POSITIVE_INTEGER, msg = "page error")
							  }, defaultVal = "1") int page,
							  String buildDataId) {
		Page pageObj = new Page(page, limit);
		Entity entity = Entity.create();
		//
		this.doPage(pageObj, entity, "startTime");
		BaseEnum anEnum = null;
		if (StrUtil.isNotEmpty(status)) {
			Integer integer = Convert.toInt(status);
			if (integer != null) {
				anEnum = BaseEnum.getEnum(BuildStatus.class, integer);
			}
		}

		if (anEnum != null) {
			entity.set("status", anEnum.getCode());
		}
		UserModel userModel = getUser();
		if (userModel.isSystemUser()) {
			if (StrUtil.isNotBlank(buildDataId)) {
				entity.set("buildDataId", buildDataId);
			}
		} else {
			Set<String> dataIds = this.getDataIds();
			if (StrUtil.isNotBlank(buildDataId)) {
				if (CollUtil.contains(dataIds, buildDataId)) {
					entity.set("buildDataId", buildDataId);
				} else {
					entity.set("buildDataId", StrUtil.DASHED);
				}
			} else {
				entity.set("buildDataId", dataIds);
			}
		}
		PageResultDto<BuildHistoryLog> pageResultTemp = dbBuildHistoryLogService.listPage(entity, pageObj);
		//
		PageResultDto<BuildHistoryLogVo> pageResult = new PageResultDto<>(pageResultTemp);
		List<BuildHistoryLogVo> result = pageResult.getResult();
		if (result != null) {
			List<BuildHistoryLogVo> collect = result.stream().map(buildHistoryLog -> {
				BuildHistoryLogVo buildHistoryLogVo = new BuildHistoryLogVo();
				BeanUtil.copyProperties(buildHistoryLog, buildHistoryLogVo);
				//
				if (StrUtil.isEmpty(buildHistoryLog.getBuildName())) {
					String dataId = buildHistoryLog.getBuildDataId();
					BuildInfoModel item = buildInfoService.getByKey(dataId);
					if (item != null) {
						buildHistoryLogVo.setBuildName(item.getName());
					}
				}
				return buildHistoryLogVo;
			}).collect(Collectors.toList());
			pageResult.setResult(collect);
		}
		return JsonMessage.getString(200, "获取成功", pageResult);
	}

	private Set<String> getDataIds() {
		Entity where = Entity.create();
		Page pageReq = new Page();
		List<BuildInfoModel> list = buildInfoService.listPageOnlyResult(where, pageReq);
		if (CollUtil.isEmpty(list)) {
			return new HashSet<>();
		} else {
			return list.stream().map(BuildInfoModel::getId).collect(Collectors.toSet());
		}
	}

	/**
	 * 构建
	 *
	 * @param logId id
	 * @return json
	 */
	@RequestMapping(value = "/build/history/delete_log.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@Feature(method = MethodFeature.DEL_LOG)
	public String delete(@ValidatorConfig(@ValidatorItem(value = ValidatorRule.NOT_BLANK, msg = "没有数据")) String logId) {
		BuildHistoryLog buildHistoryLog = dbBuildHistoryLogService.getByKey(logId);
		Objects.requireNonNull(buildHistoryLog);

		if (!CollUtil.contains(this.getDataIds(), buildHistoryLog.getBuildDataId())) {
			return JsonMessage.getString(405, "没有权限");
		}
		JsonMessage<String> jsonMessage = dbBuildHistoryLogService.deleteLogAndFile(logId);
		return jsonMessage.toString();
	}
}
