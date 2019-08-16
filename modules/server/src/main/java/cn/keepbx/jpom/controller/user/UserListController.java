package cn.keepbx.jpom.controller.user;

import cn.jiangzeyin.common.JsonMessage;
import cn.keepbx.jpom.common.BaseServerController;
import cn.keepbx.jpom.model.data.UserModel;
import cn.keepbx.jpom.service.user.UserService;
import cn.keepbx.plugin.ClassFeature;
import cn.keepbx.plugin.Feature;
import cn.keepbx.plugin.MethodFeature;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户列表
 *
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/user")
@Feature(cls = ClassFeature.USER)
public class UserListController extends BaseServerController {

    @Resource
    private UserService userService;

    /**
     * 展示用户列表
     */
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @Feature(method = MethodFeature.LIST)
    public String list() {
        return "user/list";
    }


    /**
     * 查询所有用户
     */
    @RequestMapping(value = "getUserList", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    @Feature(method = MethodFeature.LIST)
    public String getUserList() {
        UserModel userName = getUser();
        List<UserModel> userList = userService.list();
        if (userList != null) {
            userList = userList.stream().filter(userModel -> {
                // 不显示自己的信息
                return !userModel.getId().equals(userName.getId());
            }).collect(Collectors.toList());
        }
        return JsonMessage.getString(200, "", userList);
    }
}
