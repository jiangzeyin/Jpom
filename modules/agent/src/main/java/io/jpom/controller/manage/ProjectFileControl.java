package io.jpom.controller.manage;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import cn.jiangzeyin.common.JsonMessage;
import cn.jiangzeyin.controller.multipart.MultipartFileBuilder;
import com.alibaba.fastjson.JSONArray;
import io.jpom.common.BaseAgentController;
import io.jpom.common.commander.AbstractProjectCommander;
import io.jpom.model.AfterOpt;
import io.jpom.model.BaseEnum;
import io.jpom.model.data.ProjectInfoModel;
import io.jpom.service.manage.ConsoleService;
import io.jpom.socket.ConsoleCommandOp;
import io.jpom.system.AgentConfigBean;
import io.jpom.util.FileUtils;
import io.jpom.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 项目文件管理
 *
 * @author jiangzeyin
 * @date 2019/4/17
 */
@RestController
@RequestMapping(value = "/manage/file/")
public class ProjectFileControl extends BaseAgentController {
	@Resource
	private ConsoleService consoleService;

	@RequestMapping(value = "getFileList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String getFileList(String id, String path) {
		// 查询项目路径
		ProjectInfoModel pim = projectInfoService.getItem(id);
		if (pim == null) {
			return JsonMessage.getString(500, "查询失败：项目不存在");
		}
		String lib = pim.allLib();
		File fileDir;
		if (!StrUtil.isEmptyOrUndefined(path)) {
			fileDir = FileUtil.file(lib, path);
		} else {
			fileDir = new File(lib);
		}
		if (!fileDir.exists()) {
			return JsonMessage.getString(500, "目录不存在");
		}
		File[] filesAll = fileDir.listFiles();
		if (filesAll == null) {
			return JsonMessage.getString(500, "目录是空");
		}
		JSONArray arrayFile = FileUtils.parseInfo(filesAll, false, lib);
		return JsonMessage.getString(200, "查询成功", arrayFile);
	}


	@RequestMapping(value = "upload", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String upload() throws Exception {
		ProjectInfoModel pim = getProjectInfoModel();
		MultipartFileBuilder multipartFileBuilder = createMultipart()
				.addFieldName("file");
		// 压缩文件
		String type = getParameter("type");
		// 是否清空
		String clearType = getParameter("clearType");
		String levelName = getParameter("levelName");
		File lib;
		if (StrUtil.isEmpty(levelName)) {
			lib = new File(pim.allLib());
		} else {
			lib = FileUtil.file(pim.allLib(), levelName);
		}

		if ("unzip".equals(type)) {
			multipartFileBuilder.setFileExt(StringUtil.PACKAGE_EXT);
			multipartFileBuilder.setSavePath(AgentConfigBean.getInstance().getTempPathName());
			String path = multipartFileBuilder.save();
			// 判断是否需要清空
			if ("clear".equalsIgnoreCase(clearType)) {
				if (!FileUtil.clean(lib)) {
					FileUtil.del(lib.toPath());
					//return JsonMessage.getString(500, "清除旧lib失败");
				}
			}
			// 解压
			File file = new File(path);
			try {
				CompressUtil.createExtractor(null, file).extract(lib);
//                if (names == null || names.isEmpty()) {
//                    return JsonMessage.getString(500, "没有解压出任何文件");
//                }
			} finally {
				if (!file.delete()) {
					DefaultSystemLog.getLog().error("删除文件失败：" + file.getPath());
				}
			}
		} else {
			multipartFileBuilder.setSavePath(FileUtil.getAbsolutePath(lib))
					.setUseOriginalFilename(true);
			// 保存
			multipartFileBuilder.save();
		}
		// 修改使用状态
		pim.setUseLibDesc("upload");
		projectInfoService.updateItem(pim);
		//
		String after = getParameter("after");
		if (StrUtil.isNotEmpty(after)) {
			//
			List<ProjectInfoModel.JavaCopyItem> javaCopyItemList = pim.getJavaCopyItemList();
			//
			AfterOpt afterOpt = BaseEnum.getEnum(AfterOpt.class, Convert.toInt(after, AfterOpt.No.getCode()));
			if ("restart".equalsIgnoreCase(after) || afterOpt == AfterOpt.Restart) {
				String result = consoleService.execCommand(ConsoleCommandOp.restart, pim, null);
				// 自动处理副本集
				if (javaCopyItemList != null) {
					ThreadUtil.execute(() -> javaCopyItemList.forEach(javaCopyItem -> {
						try {
							consoleService.execCommand(ConsoleCommandOp.restart, pim, javaCopyItem);
						} catch (Exception e) {
							DefaultSystemLog.getLog().error("重启副本集失败", e);
						}
					}));
				}
				return JsonMessage.getString(200, "上传成功并重启：" + result);
			}
			if (afterOpt == AfterOpt.Order_Restart || afterOpt == AfterOpt.Order_Must_Restart) {
				boolean restart = this.restart(pim, null, afterOpt);
				if (javaCopyItemList != null) {
					ThreadUtil.execute(() -> {
						// 副本
						for (ProjectInfoModel.JavaCopyItem javaCopyItem : javaCopyItemList) {
							if (!this.restart(pim, javaCopyItem, afterOpt)) {
								return;
							}
							// 休眠30秒 等待之前项目正常启动
							try {
								TimeUnit.SECONDS.sleep(30);
							} catch (InterruptedException ignored) {
							}
						}
					});
				}
			}
		}
		return JsonMessage.getString(200, "上传成功");
	}


	private boolean restart(ProjectInfoModel projectInfoModel, ProjectInfoModel.JavaCopyItem javaCopyItem, AfterOpt afterOpt) {
		try {
			String result = consoleService.execCommand(ConsoleCommandOp.restart, projectInfoModel, javaCopyItem);
			int pid = AbstractProjectCommander.parsePid(result);
			if (pid <= 0) {
				// 完整重启，不再继续剩余的节点项目
				return afterOpt != AfterOpt.Order_Must_Restart;
			}
			return true;
		} catch (Exception e) {
			DefaultSystemLog.getLog().error("重复失败", e);
			// 完整重启，不再继续剩余的节点项目
			return afterOpt != AfterOpt.Order_Must_Restart;
		}
	}

	@RequestMapping(value = "deleteFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String deleteFile(String filename, String type, String levelName) {
		ProjectInfoModel pim = getProjectInfoModel();
		if ("clear".equalsIgnoreCase(type)) {
			// 清空文件
			File file = new File(pim.allLib());
			if (FileUtil.clean(file)) {
				return JsonMessage.getString(200, "清除成功");
			}
			if (pim.tryGetStatus()) {
				return JsonMessage.getString(501, "文件被占用，请先停止项目");
			}
			return JsonMessage.getString(500, "删除失败：" + file.getAbsolutePath());
		} else {
			// 删除文件
			String fileName = pathSafe(filename);
			if (StrUtil.isEmpty(fileName)) {
				return JsonMessage.getString(405, "非法操作");
			}
			File file;
			if (StrUtil.isEmpty(levelName)) {
				file = FileUtil.file(pim.allLib(), fileName);
			} else {
				file = FileUtil.file(pim.allLib(), levelName, fileName);
			}
			if (file.exists()) {
				if (FileUtil.del(file)) {
					return JsonMessage.getString(200, "删除成功");
				}
			} else {
				return JsonMessage.getString(404, "文件不存在");
			}
			return JsonMessage.getString(500, "删除失败");
		}
	}


	@RequestMapping(value = "readFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String readFile(String filePath, String filename, String type, String levelName) {
		ProjectInfoModel pim = getProjectInfoModel();
		if (filePath != "") {
			filePath += File.separator;
		}
		File file = FileUtil.file(pim.allLib(), filePath + filename);
		String ymlString = FileUtil.readString(file, "Utf-8");
		return JsonMessage.getString(200, ymlString);
	}

	@RequestMapping(value = "updateConfigFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String updateConfigFile(String filePath, String filename, String type, String levelName, String fileText) {
		ProjectInfoModel pim = getProjectInfoModel();
		if (filePath != "") {
			filePath += File.separator;
		}
		FileUtil.writeFromStream(new ByteArrayInputStream(fileText.getBytes()), FileUtil.file(pim.allLib(), filePath + filename));
		return JsonMessage.getString(200, "文件写入成功");
	}


	@RequestMapping(value = "download", method = RequestMethod.GET)
	public String download(String id, String filename, String levelName) {
		String safeFileName = pathSafe(filename);
		if (StrUtil.isEmpty(safeFileName)) {
			return JsonMessage.getString(405, "非法操作");
		}
		try {
			ProjectInfoModel pim = projectInfoService.getItem(id);
			File file;
			if (StrUtil.isEmpty(levelName)) {
				file = FileUtil.file(pim.allLib(), filename);
			} else {
				file = FileUtil.file(pim.allLib(), levelName, filename);
			}
			if (file.isDirectory()) {
				return "暂不支持下载文件夹";
			}
			ServletUtil.write(getResponse(), file);
		} catch (Exception e) {
			DefaultSystemLog.getLog().error("下载文件异常", e);
		}
		return "下载失败。请刷新页面后重试";
	}

}
