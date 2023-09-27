export default {
    node_layout: {
        nginx: {
            cert: {
                alert: "当前功能将择机下架，请提前使用服务端证书管理来统一实现证书分发",
                configTemp: "Cert 配置模板",
                deleteContent: "真的要删除该记录么？",
                downloadContent: "正在下载，请稍等...",
            },
            list: {
                alert: "当前功能将择机下架，请提前使用 DSL 项目管理来代替 nginx 管理",
                editNginx: "编辑 Nginx 服务",
                openNginx: "启动 Nginx",
                reloadNginx: "重新加载 Nginx",
                closeNginx: "停止 Nginx",
                nginxManage: "nginx 管理是指在想编辑配置文件，并自动重新加载(reload)",
                li_1: "linux 服务器默认执行 nginx -s reload 、service xxxx start、service xxxx top",
                li_2: "linux 服务器如果为编译安装则需要将 nginx 服务名称配置到 nginx执行文件的绝对路径，如",
                li_3: "windows 服务器是需要提前安装 nginx 并配置服务,默认执行 net start xxxx、net stop xxxx、net、sc query xxxx",
                handleEdit: "`名称：${text}  server 节点数 ${record.serverCount}`",
                editNginxConfig: "编辑 Nginx 配置文件",
                whitePath: "白名单路径",
                chooseWhitePath: "请选择白名单路径",
                fileNameMessage: "需要以 .conf 结尾",
                editMessage: "编辑 Nginx 服务名称",
                serviceNameMessage: "请输入 Nginx 服务名称",
                reallyWant: "真的要",
                ifPermDelete: "永久删除文件么？",
                ifRestoreConfig: "还原配置文件么？",
                ifDelete: "删除文件么？"
            }
        },
        other: {
            script_console: {
                addArgs: "添加运行参数",
                paramsHelp: '所有参数将拼接成字符串以空格分隔形式执行脚本,需要注意参数顺序和未填写值的参数将自动忽略',
                paramsBefore: "`参数${index + 1}值`",
                paramsAfter: "`参数值 ${item.desc ? ',' + item.desc : ''}`",
                socketErrMessage: "web socket 错误,请检查是否开启 ws 代理",
                socketOncloseMessage: "会话已经关闭[node-script-consloe]",

            },
            script_list: {
                clearCache: "清除服务端缓存节点所有的脚步模版信息并重新同步",
                serverSync: '服务端分发同步的脚本不能直接删除,需要到服务端去操作',
                editScript: "编辑 Script",
                serverSyncMessage: "服务端同步的脚本不能在此修改",
                scriptName: "Script 名称",
                scriptContent: "Script 内容",
                inputCron: "如果需要定时自动执行则填写,cron 表达式.默认未开启秒级别,需要去修改配置文件中:[system.timerMatchSecond]）",
                inputScriptName: "请输入脚本名称",
                inputScriptContent: "请输入脚本内容",
                syncCantMod: "服务端同步的脚本不能在此修改",
                ifDeleteScript: "真的要删除脚本么？",
                inputParamDesc1: "请填写第",
                inputParamDesc2: "个参数的描述"
            },
            script_log: {
                execStart: "执行时间开始",
                execEnd: "执行时间结束",
                scriptContent: "脚本模版是存储在节点(插件端),执行也都将在节点里面执行,服务端会定时去拉取执行日志,拉取频率为 100 条/分钟",
                timeDelay: "数据可能出现一定时间延迟",
                ifDeleteRec: "真的要删除执行记录么？",
            },
        },
        project: {
            console: {
                noCopyFound: "没有找到副本",
                noCopy: "没有副本",
                socketErr: "web socket 错误,请检查是否开启 ws 代理",
                sessionClosed: "会话已经关闭[project-console]",
                ifRestart: "真的要重启项目么？",
                ifStop: "真的要停止项目么？",

            },
            file: {
                uploadDecom: "上传压缩包并自动解压",
                configWhiteList: "通过 URL 下载远程文件到项目文件夹,需要到节点系统配置->白名单配置中配置允许的 HOST 白名单",
                refreshFile:"刷新文件表格",
                clearFile:"清空当前目录文件",
                supportRightClick:"文件名栏支持右键菜单",
                cannotModify:"目录不能编辑",
                cannotDownload:"不能下载目录",
                configFiles:"需要到 节点管理中的【插件端配置】的白名单配置中配置允许编辑的文件后缀",
                uploadProFile:"上传项目文件",
                selectFiles:"选择文件",
                uploadCompFiles:"上传压缩文件",
                selectCompFiles:"选择压缩文件",
                clearCover:"清空覆盖",
                notClear:"不清空",
                clearRedundantFolder:"解压时候自动剔除压缩包里面多余的文件夹名",
                saveAndClose:"保存并关闭",
                remoteDownloadFiles:"远程下载文件",
                remoteDownloadURL:"远程下载URL",
                remoteDownloadAddress:"远程下载地址",
                whetherCompress:"是否为压缩包",
                removeFolder:"剔除文件夹",
                inputFile:"输入文件或者文件夹名",
                inputNewName:"输入新名称",
                handRefresh:"目录创建成功后需要手动刷新右边树才能显示出来哟",
                catalog:"目录：",
                chooseANode:"请选择一个节点",

                remoteURLNotNull:"远程下载URL不为空",
                reallyClear:"真的要清空【",
                reallyDelete:"真的要删除【",
                folders:"】文件夹么？",
                files:"】文件么？",
                fileAndFolder:"】目录和文件么？",
                reallyClearProject:"真的要清空项目目录和文件么？",
                downloading:"正在下载，请稍等...",
            },
            file_backup: {},
            file_read: {},
            list: {},
            log: {},
            replica: {},
            recover_list: {}
        }
    }
}
