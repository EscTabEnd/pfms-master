package cn.org.chenxh.controller;

import cn.org.chenxh.annotation.Login;
import cn.org.chenxh.constant.FileTypeEnum;
import cn.org.chenxh.entity.Department;
import cn.org.chenxh.entity.FileRecord;
import cn.org.chenxh.entity.Logs;
import cn.org.chenxh.entity.User;
import cn.org.chenxh.service.DepartServers;
import cn.org.chenxh.service.FileRecordServers;
import cn.org.chenxh.service.LogsServers;
import cn.org.chenxh.service.UserServers;
import cn.org.chenxh.util.CacheUtil;
import cn.org.chenxh.util.FileTypeUtil;
import cn.org.chenxh.util.FileUtil;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @description 文件服务器
 * @author chenxh
 * @date 2019-1-21
 */
@Slf4j
@CrossOrigin
@Controller
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String SLASH = "/";

    @Value("${fs.dir}")
    private String fileDir;

    @Value("${fs.uuidName}")
    private Boolean uuidName;

    @Value("${fs.useSm}")
    private Boolean useSm;

    @Value("${fs.useNginx}")
    private Boolean useNginx;

    @Value("${fs.nginxUrl}")
    private String nginxUrl;

    @Value("${admin.uname}")
    private String uname;

    @Value("${admin.pwd}")
    private String pwd;

    @Value("${domain}")
    private String domain;

    @Value("${fs.dir.prefix}")
    private String file_prefix;

    @Autowired
    UserServers userServers;
    @Autowired
    LogsServers logsServers;
    @Autowired
    DepartServers departServers;
    @Autowired
    FileRecordServers fileRecordServers;
    /**
     * 登录页
     *
     * @return
     */
    @RequestMapping("/login")
    public String loginPage() {
        return "login.html";
    }

    /**
     * 登录提交认证
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/auth")
    public String auth(User user, HttpSession session) {
        User loginuser = userServers.findByUnameAndPwd(user.getUname(),user.getPwd());
        if (loginuser != null && "1".equals(loginuser.getStatus())) {
            session.setAttribute( "LOGIN_USER", loginuser );
            return "redirect:/";
        }
        return "redirect:/login";
    }


    /**
     * 登录异步认证
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/loginAuth")
    @ResponseBody
    public ModelMap loginAuth(User user, HttpSession session) {
        ModelMap m = new ModelMap();
        User loginuser = userServers.findByUnameAndPwd(user.getUname(),user.getPwd());
        if(loginuser != null ){
            if("0".equals(loginuser.getStatus())){
                m.put("code","-1");
                m.put("msg","账号已被禁用，请联系管理员");
            }else{
                m.put("code","1");
                m.put("msg","登录成功");
            }
        }else{
            m.put("code","-1");
            m.put("msg","账号或密码错误");
        }
        return m;
    }

    /**
     * 人员管理
     *
     * @param session
     * @return
     */
    @RequestMapping("/user")
    public String employeeManage(HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return "redirect:/login";
        }
        return "userMange.html";
    }

    /**
     * 人员管理
     *
     * @param session
     * @return
     */
    @RequestMapping("/treeframe")
    public String treeFrame(String url,HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return "redirect:/login";
        }

        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User){
            User tmp = (User)o;
            if(!"root".equals(tmp.getUname())){
                Department department = departServers.findById(tmp.getDid());
                if(department != null && !StringUtils.isEmpty(department.getUrl()) && !"/".equals(department.getUrl())){
                    session.setAttribute("target_path",department.getUrl()+SLASH+tmp.getUname());
                }else{
                    session.setAttribute("target_path","");
                }
            }else{
                session.setAttribute("target_path","");
            }
        }
        session.setAttribute( "old_path", url );
        return "treeframe.html";
    }

    /**
     * 部门人员管理
     *
     * @param session
     * @return
     */
    @RequestMapping("/public")
    public String publicDesktop(HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return "redirect:/login";
        }

        return "public.html";
    }
    /**
     * 部门人员管理
     *
     * @param session
     * @return
     */
    @RequestMapping("/department")
    public String departmentManage(HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return "redirect:/login";
        }
        return "department.html";
    }

    @RequestMapping("api/departmentQuery")
    @ResponseBody
    public List<ModelMap> departmentQuery(HttpSession session) {
        List<Department> departments = departServers.findAll();
        List<ModelMap> list = new ArrayList<>();
         for(Department department: departments){
             if (department.getPid() == -1) {  //获取根节点目录，在数据库中加第一行数据，即根目录的数据，pid设为0
                 ModelMap treeObj = new ModelMap();
                 treeObj.put("id", department.getId());
                 treeObj.put("name", department.getDname());
                 treeObj.put("pid", department.getPid());
                 treeObj.put("spread",true); //设置默认展开
                 treeObj.put("children", getChildren(department.getId(),departments)); //查询叶子节点
                 if(treeObj.get("children") == null){
                     treeObj.remove("children");
                 }
                 list.add(treeObj);
             }
         }
        return list;

    }

    @RequestMapping("api/fileUrlQuery")
    @ResponseBody
    public List<ModelMap> fileUrlQuery(String curPos,HttpSession session) {
        List<ModelMap> list = new ArrayList<>();
        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User) {
            User tmp = (User) o;
            String target_path = (String)session.getAttribute("target_path");
            if(!StringUtils.isEmpty(target_path) || (StringUtils.isEmpty(target_path)&& "root".equals(tmp.getUname()))){
               try{
                   String path = fileDir+target_path;
                   File file = new File(path);
                   ModelMap modelMap = null;
                   if (file.isDirectory()){
                       modelMap = new ModelMap();
                       modelMap.put("id",file.getName().hashCode());
                       modelMap.put("name","root".equals(file.getName())?"根目录":file.getName());
                       modelMap.put("url",file.getPath());
                       modelMap.put("spread",true); //设置默认展开
                       modelMap.put("children",FileUtil.traverseDirTreeData(file)); //设置默认展开
                       list.add(modelMap);
                   }
               }catch (Exception e){

               }
            }
        }
        return list;
    }

    @RequestMapping("api/copyfile")
    @ResponseBody
    public ModelMap copyfile(String target_url,HttpSession session) {
        ModelMap modelMap = new ModelMap();
        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User) {
            User tmp = (User) o;
            String oldpath = (String)session.getAttribute( "old_path");
            String prefix =  target_url;
            if(!StringUtils.isEmpty(target_url) && !StringUtils.isEmpty(oldpath)){

                try {
                    File file = new File(fileDir+(oldpath.startsWith("/")?oldpath:SLASH+oldpath));
                    String dirname = oldpath.substring(oldpath.lastIndexOf("/")+1,oldpath.length());
                    if(file.isDirectory()){//如果是目录，将目录名称获取到，拼接到目标位置后
                        target_url+=SLASH+dirname;
                        //校验如果目录是部门或者用户，不允许移动
                        User user = userServers.findByUname(dirname);
                        if(user != null){
                            modelMap.put("code","-1");
                            modelMap.put("msg","该文件为用户文件夹不能移动");
                            return modelMap;
                        }
                        Department department = departServers.findByDname(dirname);
                        if(department != null){
                            modelMap.put("code","-1");
                            modelMap.put("msg","该文件为部门文件夹不能移动");
                            return modelMap;
                        }
                    }
                    FileUtil.copyDir(fileDir+(oldpath.startsWith("/")?oldpath:SLASH+oldpath),target_url);
                    Logs logs = new Logs();
                    if(file.isDirectory()){
                        logs.setLog(tmp.getUname()+"将文件夹【"+oldpath+"】移动到【"+target_url+"】");
                        //更新文件的属性信息，
                        List<FileRecord> fileRecords = fileRecordServers.findByUrlLike(oldpath);
                        String subfix = "";
                        for(FileRecord fileRecord :fileRecords){
                            subfix = fileRecord.getUrl().substring(fileRecord.getUrl().lastIndexOf(dirname)+dirname.length(),fileRecord.getUrl().length());
                            prefix = prefix.replaceAll("\\\\","/");
                            fileRecord.setUrl(prefix+(StringUtils.isEmpty(subfix)?SLASH+fileRecord.getCurname():SLASH+dirname+subfix));
                            fileRecordServers.save(fileRecord);
                        }
                    }else{
                        List<FileRecord> fileRecords = fileRecordServers.findByUrlLike(oldpath);
                        for(FileRecord fileRecord :fileRecords){
                            target_url = target_url.replaceAll("\\\\","/");
                            fileRecord.setUrl(target_url+SLASH+dirname);
                            fileRecordServers.save(fileRecord);
                        }
                        logs.setLog(tmp.getUname()+"将文件【"+oldpath+"】移动到【"+target_url+"】");
                    }
                    String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    logs.setTime(date);
                    logs.setUserid(tmp.getId()+"");
                    logs.setUname(tmp.getUname());
                    logsServers.saveLogs(logs);


                    //最后删除文件夹或者文件夹
                    FileUtil.deleteFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    modelMap.put("code","-1");
                    modelMap.put("msg","移动失败，服务器异常");
                }
            }
        }
        modelMap.put("code","1");
        modelMap.put("msg","移动成功");
        return modelMap;
    }
    /**
     * 获取叶子节点目录，递归遍历
     * */
    public List<ModelMap> getChildren(Integer Id,List<Department> departments){
        List<ModelMap> list = new ArrayList<>();
        for (Department department : departments) {
            if(department.getPid() == Id){
                ModelMap obj = new ModelMap();
                obj.put("id", department.getId());
                obj.put("name", department.getDname());
                obj.put("pid", department.getPid());
                obj.put("children", getChildren(department.getId(),departments));
                if(obj.get("children") == null){
                    obj.remove("children");
                }
                list.add(obj);
            }
        }
        return list;
    }

    @RequestMapping("api/departmentAdd")
    @ResponseBody
    public ModelMap departmentAdd(HttpSession session, Department department) {
        ModelMap modelMap = new ModelMap();
        if(department != null && department.getDname() != null && department.getPid() >= 0){
            //检查部门名称是不是重复
            Department exists = departServers.findByDname(department.getDname());
            if(exists == null){
                    if("0".equals(department.getPid()) || 0 == department.getPid()){//如果是从跟节点新增的部门，路径都是从 /开始 + 新增的部门名称。
                        department.setUrl("/"+department.getDname());
                    }else{
                        //如果不是从根节点开始，则先查询父部门的url是多少，用负部门的url加上自己的部门名称
                        Department parent = departServers.findById(department.getPid());
                        department.setUrl(parent.getUrl()+"/"+department.getDname());

                    }
                    Department newDepart = departServers.saveDepartment(department);
                    modelMap.put("code","1");
                    modelMap.put("msg","新增成功");
                    modelMap.put("id",newDepart.getId());
                    modelMap.put("pid",newDepart.getPid());
                    modelMap.put("name",newDepart.getDname());
                    //创建文件夹
                    if (fileDir == null) {
                        fileDir = SLASH;
                    }
                    if (!fileDir.endsWith(SLASH)) {
                        fileDir += SLASH;
                    }
                    if (!StringUtils.isEmpty(department.getId())) {
                        String dirPath = fileDir + department.getUrl() ;
                        File f = new File(dirPath);//创建目录
                        if (!f.exists() && f.mkdir()) {

                        }
                    }

            }else{
                modelMap.put("msg","新增失败，已存在相同部门名称");
                modelMap.put("code","-1");
            }

        }
        return modelMap;
    }

    @RequestMapping("api/departmentEdit")
    @ResponseBody
    public ModelMap departmentEdit(HttpSession session, Department department,@RequestParam  String newFile) {
        ModelMap modelMap = new ModelMap();
        String oldFileUrl = "";
        if(department != null && department.getDname() != null && department.getId() > 0){
            //检查部门名称是不是重复
            Department exists = departServers.findByDname(newFile);
            if(exists == null){
                if("0".equals(department.getPid())  || 0 == department.getPid()){//如果是从跟节点新增的部门，路径都是从 /开始 + 新增的部门名称。
                    department.setUrl("/"+newFile);
                    oldFileUrl = department.getDname();
                }else{
                    //如果不是从根节点开始，则先查询父部门的url是多少，用负部门的url加上自己的部门名称
                    Department parent = departServers.findById(department.getPid());
                    oldFileUrl = parent.getUrl()+"/"+department.getDname();
                    department.setUrl(parent.getUrl()+"/"+newFile);
                    department.setDname(newFile);

                }
                Department newDepart = departServers.saveDepartment(department);
                modelMap.put("code","1");
                modelMap.put("msg","重命名成功");
                if (fileDir == null) {
                    fileDir = SLASH;
                }
                if (!fileDir.endsWith(SLASH)) {
                    fileDir += SLASH;
                }
                if (!StringUtils.isEmpty(department.getDname()) && !StringUtils.isEmpty(newFile)) {
                    File f = new File(fileDir + oldFileUrl );
                    File smF = new File(fileDir + "sm/" + oldFileUrl );
                    File nFile = new File(fileDir + department.getUrl() );
                    File nsmFile = new File(fileDir + "sm/" + department.getUrl());
                    if (f.renameTo(nFile)) {
                        if (smF.exists()) {
                            smF.renameTo(nsmFile);
                        }
                        return modelMap;
                    }
                }

            }else{
                modelMap.put("msg","已存在相同部门名称");
                modelMap.put("code","-1");
            }

        }
        return modelMap;
    }

    /**
     * 登出
     * @param session
     * @return
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session){
        session.removeAttribute("LOGIN_USER");
        return "redirect:/login";
    }
    /**
     * 登出
     * @param session
     * @return
     */
    @RequestMapping("/logs")
    public String logs(HttpSession session){
        if (session.getAttribute("LOGIN_USER") == null) {
            return "redirect:/login";
        }
        return "logs.html";
    }
    /**
     * 获取人员列表
     *
     * @param session
     * @return
     */
    @RequestMapping("/user/list")
    @ResponseBody
    public Map<String,Object> employees(HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        List<User> users = userServers.findUserAll();
        result.put("code", 0);
        result.put("msg", "");
        result.put("total", users.size());
        result.put("data", users.toArray());
        return result;
    }

    /**
     * 获取日志列表
     *
     * @param session
     * @return
     */
    @RequestMapping("/api/logs")
    @ResponseBody
    public Map<String,Object> getlogs(HttpSession session) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        List<Logs> logs = logsServers.findLogsAll();
        result.put("code", 0);
        result.put("msg", "");
        result.put("total", logs.size());
        result.put("data", logs.toArray());
        return result;
    }
    /**
     * 获取日志列表
     *
     * @param session
     * @return
     */
    @RequestMapping("/api/searchLog")
    @ResponseBody
    public Map<String,Object> searchLog(HttpSession session,@RequestParam String search) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        List<Logs> logs = logsServers.findByUname(search);
        result.put("code", 0);
        result.put("msg", "");
        result.put("total", logs.size());
        result.put("data", logs.toArray());
        return result;
    }
    /**
     * 获取指定人员
     *
     * @param session
     * @return
     */
    @RequestMapping("/user/searchUser")
    @ResponseBody
    public Map<String,Object> searchUser(HttpSession session,@RequestParam String search,@RequestParam  Integer did,@RequestParam  String dname) {
        if (session.getAttribute("LOGIN_USER") == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        if(!StringUtils.isEmpty(search)){
            List<User> users = new ArrayList<>();
            User user = userServers.findByUname(search);
                result.put("code", "0");
            if(user != null){
                users.add(user);
                result.put("total", "1");
                result.put("msg", "");
                result.put("data", users.toArray());
            }else{
                result.put("msg", "未查询到此用户信息");
                result.put("total", "0");
                result.put("data", users.toArray());
            }

        }else if(did>0){
            List<User> users = userServers.findByDid(did);
            result.put("code", 0);
            result.put("msg", "");
            result.put("did", did);
            result.put("dname", dname);
            result.put("total", users.size());
            result.put("data", users.toArray());
        }else{
            List<User> users = userServers.findUserAll();
            result.put("code", 0);
            result.put("msg", "");
            result.put("total", users.size());
            result.put("data", users.toArray());
        }
        return result;
    }

    @RequestMapping("/user/add")
    @ResponseBody
    public ModelMap addUser(User user)throws Exception{
        User t = null;
        ModelMap modelMap = new ModelMap();
        if(user != null && user.getUname() != null && user.getPwd() != null && user.getRank() != null
        &&  user.getUname() !="" &&  user.getPwd() != "" && user.getRank() != ""){
            user.setStatus("1");
            User user1 = userServers.findByUname(user.getUname());
            if(user1 == null){
                Department department = departServers.findById(user.getDid());
                if(department == null){
                    modelMap.put("msg","未获取到部门，请核实");
                    modelMap.put("code","-1");
                    return modelMap;
                }
                user.setDname(department.getDname());
                t= userServers.saveUser(user);
                //新增成功的时候要创建文件目录
                //获取人员所在部门路径
                if(department != null && !"/".equals(department.getUrl())){
                    if (fileDir == null) {
                        fileDir = SLASH;
                    }
                    if (!fileDir.endsWith(SLASH)) {
                        fileDir += SLASH;
                    }
                    if (!StringUtils.isEmpty(t.getId())) {
                        String dirPath = fileDir + department.getUrl()+SLASH +t.getUname() ;
                        File f = new File(dirPath);//创建目录
                        if (!f.exists() && f.mkdir()) {

                        }
                        modelMap.put("msg","新增成功");
                        modelMap.put("code","1");
                    }
                }else{
                    modelMap.put("msg","未获取到部门，请核实");
                    modelMap.put("code","-1");
                    return modelMap;
                }

            }else{
                modelMap.put("msg","用户名已存在，请核实");
                modelMap.put("code","-1");
                return modelMap;
            }
        }else{
            modelMap.put("msg","用户名，密码，类别不能为空");
            modelMap.put("code","-1");
            return modelMap;
        }
        return modelMap;
    }

    @RequestMapping("/user/del")
    @ResponseBody
    public ModelMap delUser(User user,HttpSession session)throws Exception{
        ModelMap modelMap = new ModelMap();
        if("root".equals(user.getUname())){
            modelMap.put("msg","超级管理员不能禁用和启用");
            modelMap.put("code","-1");
            return modelMap;
        }
        String status = user.getStatus();
        User t = null;
        if(user != null && user.getUname() != null && user.getId() != 0){
            t = userServers.findById(user.getId()+"");
            t.setStatus(user.getStatus());
            userServers.saveUser(t);
            t = userServers.findById(user.getId()+"");
        }
        if(status.equals(t.getStatus())){
            //记录日志
            Object o = session.getAttribute("LOGIN_USER");
            if(o != null && o instanceof User) {
                User tmp = (User) o;
                Logs logs = new Logs();
                logs.setLog(tmp.getUname()+("0".equals(status)?"禁用了":"启用了")+" 用户【"+user.getUname()+"】");
                String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                logs.setTime(date);
                logs.setUserid(tmp.getId()+"");
                logs.setUname(tmp.getUname());
                logsServers.saveLogs(logs);
            }
            modelMap.put("msg","更新成功");
            modelMap.put("code","1");
        }else{
            modelMap.put("msg","更新失败");
            modelMap.put("code","-1");
        }
        return modelMap;
    }

    @RequestMapping("/user/update")
    @ResponseBody
    public ModelMap updateUser(User user,HttpSession session)throws Exception{
        ModelMap modelMap = new ModelMap();
        int flag = 0;
        if(!StringUtils.isEmpty(user.getRank())){
            flag = 1;
        }
        if(user != null && user.getUname() != null && user.getId() != 0 &&  user.getPwd() != null
            && user.getUname() != "" && user.getPwd() != ""){
            User t = null;
            t = userServers.findById(user.getId()+"");
            String newpwd = user.getPwd();
            String oldpwd = t.getPwd();
            String newrank = user.getRank();
            String oldrank = t.getRank();
            t.setPwd(user.getPwd());
            if(flag == 1){
                if("0".equals(user.getRank()) || "0".equals(t.getRank())){
                    modelMap.put("msg","不能变更超级管理员");
                    modelMap.put("code","-1");
                    return modelMap;
                }
                t.setRank(user.getRank());
                switch (newrank){
                    case "1": newrank = "一级员工";break;
                    case "2": newrank = "二员工";break;
                    case "3": newrank = "管理员";break;
                };
                switch (oldrank){
                    case "1": oldrank = "一级员工";break;
                    case "2": oldrank = "二员工";break;
                    case "3": oldrank = "管理员";break;
                }
            }
            userServers.saveUser(t);
            //记录日志
            Object o = session.getAttribute("LOGIN_USER");
            if(o != null && o instanceof User) {
                User tmp = (User) o;
                Logs logs = new Logs();
                String log= "";
                if(tmp.getUname().equals(user.getUname())){
                    log = tmp.getUname()+" 修改了";
                }else{
                    log = tmp.getUname()+" 修改了用户["+user.getUname()+"]的";
                }
                if(flag == 1){
                    logs.setLog(log+"权限，从【"+oldrank+"】改为【"+newrank+"】");
                }else{
                    logs.setLog(log+" 密码，原密码【"+oldpwd+"】新密码【"+newpwd+"】");
                }
                String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                logs.setTime(date);
                logs.setUserid(tmp.getId()+"");
                logs.setUname(tmp.getUname());
                logsServers.saveLogs(logs);
            }
            modelMap.put("msg","修改成功");
            modelMap.put("code","1");
        }else{
            modelMap.put("msg","修改失败");
            modelMap.put("code","-1");
        }
        return modelMap;
    }

    /**
     * 首页
     *
     * @return
     */
    @Login
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param curPos 上传文件时所处的目录位置
     * @return Map
     */
    @Login
    @ResponseBody
    @PostMapping("/file/upload")
    public Map upload(@RequestParam MultipartFile file, @RequestParam String curPos,HttpSession session) {
        curPos = curPos.substring(1) + SLASH;
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        // 文件原始名称
        String originalFileName = file.getOriginalFilename();
        //判断是否为IE浏览器的文件名，IE浏览器下文件名会带有盘符信息
        // Check for Unix-style path
        int unixSep = originalFileName.lastIndexOf('/');
        // Check for Windows-style path
        int winSep = originalFileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int pos = (winSep > unixSep ? winSep : unixSep);
        if (pos != -1)  {
            // Any sort of path separator found...
            originalFileName = originalFileName.substring(pos + 1);
        }else{

        }
        String suffix = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        String prefix = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        // 保存到磁盘
        File outFile;
        String path;
        if (uuidName != null && uuidName) {
            path = curPos + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix;
            outFile = new File(fileDir + path);
        } else {
            int index = 1;
            path = curPos + originalFileName;
            outFile = new File(fileDir + path);
            while (outFile.exists()) {
                path = curPos + prefix + "(" + index + ")." + suffix;
                outFile = new File(fileDir + path);
                index++;
            }
        }
        try {
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            file.transferTo(outFile);
            //记录日志
            Object o = session.getAttribute("LOGIN_USER");
            if(o != null && o instanceof User) {
                User tmp = (User) o;
                Logs logs = new Logs();
                logs.setLog(tmp.getUname()+"上传了文件【"+originalFileName+"】到【"+path+"】");
                String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                logs.setTime(date);
                logs.setUserid(tmp.getId()+"");
                logs.setUname(tmp.getUname());
                logsServers.saveLogs(logs);
                //记录文件上传属性
                FileRecord fileRecord = new FileRecord();
                fileRecord.setUrl(fileDir + path);
                fileRecord.setUname(tmp.getUname());
                fileRecord.setCurname(originalFileName);
                fileRecord.setOrgname(originalFileName);
                fileRecord.setTime(date);
                fileRecord.setLength(outFile.length());
                fileRecord.setType("file");
                fileRecord.setSize(FileUtil.FormetFileSize(outFile.length()));
                fileRecordServers.save(fileRecord);
            }
            Map rs = getRS(200, "上传成功", path );
            //生成缩略图
            if (useSm != null && useSm) {
                // 获取文件类型
                String contentType = null;
                try {
                    contentType = new Tika().detect(outFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (contentType != null && contentType.startsWith( "image/" )) {
                    File smImg = new File(fileDir + "sm/" + path );
                    if (!smImg.getParentFile().exists()) {
                        smImg.getParentFile().mkdirs();
                    }
                    Thumbnails.of(outFile).scale(1f).outputQuality(0.25f).toFile(smImg);
                    rs.put( "smUrl", "sm/" + path );
                }
            }
            return rs;
        } catch (Exception e) {
            logger.info(e.getMessage());
            return getRS(500, e.getMessage());
        }
    }

    /**
     * nginx转发
     *
     * @param filePath
     * @return
     */
    private String useNginx(String filePath) {
        if (nginxUrl == null) {
            nginxUrl = SLASH;
        }
        if (!nginxUrl.endsWith(SLASH)) {
            nginxUrl += SLASH;
        }
        String newName;
        try {
            newName = URLEncoder.encode( filePath, "utf-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            newName = filePath;
        }
        return "redirect:" + nginxUrl + newName;
    }

    /**
     * 获取源文件或者缩略图文件
     *
     * @param p
     * @param download 是否下载
     * @param response
     * @return
     */
    private String getFile(String p, boolean download, HttpServletResponse response) {
        if (useNginx) {
            return useNginx(p);
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        outputFile(fileDir + p, download, response );
        return null;
    }

    /**
     * 查看/下载源文件
     *
     * @param p 文件全路径
     * @param d 是否下载,1-下载
     * @param response
     * @return
     */
    @Login
    @GetMapping("/file")
    public String file(@RequestParam("p") String p,
                       @RequestParam(value = "d", required = true) int d,
                       HttpServletResponse response) {
        return getFile( p, d == 1 ? true : false, response );
    }

    /**
     * 返回分享源文件或其缩略图页面或文件
     *
     * @param sid
     * @param download 是否下载
     * @param modelMap
     * @param response
     * @return
     */
    private String returnShareFileOrSm(String sid, boolean download, ModelMap modelMap, HttpServletResponse response) {
        String url = null;
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    url = (String) CacheUtil.get(sid);
                    // 文件是否存在
                    File existFile = new File(fileDir + url );
                    if (!existFile.exists()) {
                        modelMap.put( "msg", "该文件已不存在~" );
                        return "error.html";
                    }
                } else {
                    modelMap.put( "msg", "分享文件已过期" );
                    return "error.html";
                }
            } else {
                modelMap.put( "msg", "无效的sid" );
                return "error.html";
            }
        }
        return getFile( url, download, response );
    }

    /**
     * 查看/下载分享的源文件
     *
     * @param sid 分享sid
     * @param response
     * @return
     */
    @GetMapping("/share/file")
    public String shareFile(@RequestParam(value = "sid", required = true) String sid,
                            @RequestParam(value = "d", required = true) int d,
                            HttpServletResponse response,
                            ModelMap modelMap) {
        return returnShareFileOrSm( sid, d == 1 ? true : false, modelMap, response );
    }

    /**
     * 分享源文件的缩略图
     *
     * @param sid 分享sid
     * @param response
     * @return
     */
    @GetMapping("/share/file/sm")
    public String shareFileSm(@RequestParam(value = "sid", required = true) String sid,
                              HttpServletResponse response,
                              ModelMap modelMap) {
        return returnShareFileOrSm( sid, false, modelMap, response );
    }

    /**
     * 查看缩略图
     *
     * @param p 文件全名
     * @param response
     * @return
     */
    @Login
    @GetMapping("/file/sm")
    public String fileSm(@RequestParam("p") String p, HttpServletResponse response) {
        return getFile( p,false, response );
    }

    /**
     * 输出文件流
     *
     * @param file
     * @param download 是否下载
     * @param response
     */
    private void outputFile(String file, boolean download, HttpServletResponse response) {
        // 判断文件是否存在
        File inFile = new File(file);
        // 文件不存在
        if (!inFile.exists()) {
            PrintWriter writer = null;
            try {
                response.setContentType("text/html;charset=UTF-8");
                writer = response.getWriter();
                writer.write("<!doctype html><title>404 Not Found</title><link rel=\"shorcut icon\" href=\"assets/images/logo.png\"><h1 style=\"text-align: center\">404 Not Found</h1><hr/><p style=\"text-align: center\">FMS Server</p>");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        // 获取文件类型
        String contentType = null;
        try {
            contentType = new Tika().detect(inFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 图片、文本文件,则在线查看
        logger.info("文件类型：" + contentType);
        if (FileTypeUtil.canOnlinePreview(contentType) && !download) {
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
        } else {
            // 其他文件,强制下载
            response.setContentType( "application/force-download" );
            String newName;
            try {
                newName = URLEncoder.encode( inFile.getName(), "utf-8" );
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                newName = inFile.getName();
            }
            response.setHeader("Content-Disposition", "attachment;fileName=" + newName );
        }
        // 输出文件流
        OutputStream os = null;
        FileInputStream is = null;
        try {
            is = new FileInputStream(inFile);
            os = response.getOutputStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取文件类型
     *
     * @param suffix
     * @param contentType
     * @return
     */
    private String getFileType(String suffix, String contentType) {
        String type;
        if (FileTypeEnum.PPT.getName().equalsIgnoreCase(suffix) || FileTypeEnum.PPTX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PPT.getName();
        } else if (FileTypeEnum.DOC.getName().equalsIgnoreCase(suffix) || FileTypeEnum.DOCX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.DOC.getName();
        } else if (FileTypeEnum.XLS.getName().equalsIgnoreCase(suffix) || FileTypeEnum.XLSX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.XLS.getName();
        } else if (FileTypeEnum.PDF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PDF.getName();
        } else if (FileTypeEnum.HTML.getName().equalsIgnoreCase(suffix) || FileTypeEnum.HTM.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.HTM.getName();
        } else if (FileTypeEnum.TXT.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.TXT.getName();
        } else if (FileTypeEnum.SWF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.FLASH.getName();
        } else if (FileTypeEnum.ZIP.getName().equalsIgnoreCase(suffix) || FileTypeEnum.RAR.getName().equalsIgnoreCase(suffix) || FileTypeEnum.SEVENZ.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.ZIP.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.AUDIO.getName() + SLASH)) {
            type = FileTypeEnum.MP3.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.VIDEO.getName() + SLASH)) {
            type = FileTypeEnum.MP4.getName();
        } else {
            type = FileTypeEnum.FILE.getName();
        }
        return type;
    }

    @Login
    @RequestMapping("/api/initFP")
    @ResponseBody
    public Map intiFP(HttpSession session){
        Map map = new HashMap();
        map.put("tvFP","/");
        map.put("title","0");
        map.put("data","/");

        map.put("code","1");
        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User){
            User tmp = (User)o;
            if(!"root".equals(tmp.getUname())){
                Department department = departServers.findById(tmp.getDid());
                if(department != null && !StringUtils.isEmpty(department.getUrl()) && !"/".equals(department.getUrl())){
                    map.put("tvFP",department.getUrl()+SLASH+tmp.getUname());
                    map.put("path",department.getUrl()+SLASH+tmp.getUname());
                }else{
                    map.put("tvFP","/"+tmp.getUname());
                    map.put("path","/"+tmp.getUname());
                }
                map.put("data",tmp.getUname());
                map.put("title",tmp.getRank());
            }
            map.put("data_id",tmp.getId());

        }
        return map;
    }

    @Login
    @RequestMapping("/api/initPublic")
    @ResponseBody
    public Map initPublic(HttpSession session){
        Map map = new HashMap();
        map.put("tvFP","/公共桌面");
        map.put("title","0");
        map.put("path","/公共桌面");
        File file = new File(fileDir+"/公共桌面");
        if(!file.exists()){
            file.mkdir();
            //记录文件上传属性
            FileRecord fileRecord = new FileRecord();
            fileRecord.setUrl(file.getPath());
            fileRecord.setUname("系统");
            fileRecord.setCurname(file.getName());
            fileRecord.setOrgname(file.getName());
            String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            fileRecord.setTime(date);
            fileRecord.setType("dir");
            fileRecordServers.save(fileRecord);
        }

        map.put("code","1");
        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User){
            User tmp = (User)o;
            map.put("data",tmp.getUname());
            map.put("title",tmp.getRank());
            map.put("data_id",tmp.getId());
        }
        return map;
    }

    /**
     * 获取全部文件
     *
     * @param dir
     * @param accept
     * @param exts
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/list")
    public Map list(String dir, String accept, String exts,HttpSession session) {
        String[] mExts = null;
        if (exts != null && !exts.trim().isEmpty()) {
            mExts = exts.split(",");
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }

        Map<String, Object> rs = new HashMap<>();
        if (dir == null || SLASH.equals(dir)) {
            dir = "";
        } else if (dir.startsWith(SLASH)) {
            dir = dir.substring(1);
        }
        String path = fileDir + dir;
        File file = new File(path);
        File[] listFiles = file.listFiles();
        List<Map> dataList = new ArrayList<>();
        if (listFiles != null) {
            for (File f : listFiles) {
                if ("sm".equals(f.getName())) {
                    continue;
                }
                Map<String, Object> m = new HashMap<>(0);
                // 文件名称
                m.put( "name", f.getName() );
                // 修改时间
                m.put( "updateTime", f.lastModified() );
                // 是否是目录
                m.put( "isDir", f.isDirectory() );
                if (f.isDirectory()) {
                    // 文件类型
                    m.put( "type", "dir" );
                } else {
                    // 是否支持在线查看
                    boolean flag = false;
                    try {
                        if (FileTypeUtil.canOnlinePreview(new Tika().detect(f))) {
                            flag = true;
                        }
                        m.put( "preview", flag );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String type;
                    // 文件地址
                    m.put( "url", (dir.isEmpty() ? dir : (dir + SLASH)) + f.getName() );
                    // 获取文件类型
                    String contentType = null;
                    String suffix = f.getName().substring( f.getName().lastIndexOf(".") + 1 );
                    try {
                        contentType = new Tika().detect(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 筛选文件类型
                    if (accept != null && !accept.trim().isEmpty() && !accept.equals("file")) {
                        if (contentType == null || !contentType.startsWith( accept + SLASH )) {
                            continue;
                        }
                        if (mExts != null) {
                            for (String ext : mExts) {
                                if (!f.getName().endsWith( "." + ext )) {
                                    continue;
                                }
                            }
                        }
                    }
                    // 获取文件图标
                    m.put("type", getFileType(suffix, contentType));
                    // 是否有缩略图
                    String smUrl = "sm/" + (dir.isEmpty() ? dir : (dir + SLASH)) + f.getName();
                    if (new File(fileDir + smUrl ).exists()) {
                        m.put( "hasSm", true );
                        // 缩略图地址
                        m.put( "smUrl", smUrl );
                    }
                }
                dataList.add(m);
            }
        }
        // 根据上传时间排序
        Collections.sort(dataList, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                Long l1 = (long) o1.get("updateTime");
                Long l2 = (long) o2.get("updateTime");
                return l1.compareTo(l2);
            }
        });
        // 把文件夹排在前面
        Collections.sort(dataList, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                Boolean l1 = (boolean) o1.get("isDir");
                Boolean l2 = (boolean) o2.get("isDir");
                return l2.compareTo(l1);
            }
        });
        rs.put( "code", 200 );
        rs.put( "msg", "查询成功" );
        rs.put( "data", dataList );
        return rs;
    }

    /**
     * 递归删除目录下的文件以及目录
     *
     * @param file
     * @return
     */
    static boolean forDelFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                forDelFile(f);
            }
        }
        return file.delete();
    }

    /**
     * 删除
     *
     * @param file
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/del")
    public Map del(String file,HttpSession session) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (file != null && !file.isEmpty()) {
            String fileName = file.substring(file.lastIndexOf("/")+1,file.length());
            User user = userServers.findByUname(fileName);
            if(user != null){
                return getRS(500, "该文件为用户文件夹不能删除" );
            }
            Department department = departServers.findByDname(fileName);
            if(department != null){
                return getRS(500, "该文件为部门文件夹不能删除" );
            }
            File f = new File(fileDir + file );
            File smF = new File(fileDir + "sm/" + file );
            if (f.exists()) {
                // 文件
                if (f.isFile()) {
                    if (f.delete()) {
                        if (smF.exists() && smF.isFile()) {
                            smF.delete();
                        }
                        //记录日志
                        Object o = session.getAttribute("LOGIN_USER");
                        if(o != null && o instanceof User) {
                            User tmp = (User) o;
                            Logs logs = new Logs();
                            logs.setLog(tmp.getUname()+"删除路径【"+file+"】下的文件【"+fileName+"】");
                            String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                            logs.setTime(date);
                            logs.setUserid(tmp.getId()+"");
                            logs.setUname(tmp.getUname());
                            logsServers.saveLogs(logs);
                        }
                        return getRS(200, "文件删除成功" );
                    }
                } else {
                    // 目录
                    forDelFile(f);
                    if (smF.exists() && smF.isDirectory()) {
                        forDelFile(smF);
                    }
                    //记录日志
                    Object o = session.getAttribute("LOGIN_USER");
                    if(o != null && o instanceof User) {
                        User tmp = (User) o;
                        Logs logs = new Logs();
                        logs.setLog(tmp.getUname()+"删除路径【"+file+"】下的文件夹【"+fileName+"】");
                        String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                        logs.setTime(date);
                        logs.setUserid(tmp.getId()+"");
                        logs.setUname(tmp.getUname());
                        logsServers.saveLogs(logs);
                    }
                    return getRS(200, "目录删除成功" );
                }
            } else {
                return getRS(500, "文件或目录不存在" );
            }
        }
        return getRS(500, "文件或目录删除失败" );
    }

    /**
     * 重命名
     *
     * @param oldFile
     * @param newFile
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/rename")
    public Map rename(String oldFile, String newFile,HttpSession session) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (!StringUtils.isEmpty(oldFile) && !StringUtils.isEmpty(newFile)) {
            String fileName = oldFile.substring(oldFile.lastIndexOf("/")+1,oldFile.length());
            User user = userServers.findByUname(fileName);
            if(user != null){
                return getRS(500, "该文件为用户文件夹不能修改" );
            }
            Department department = departServers.findByDname(fileName);
            if(department != null){
                return getRS(500, "该文件为部门文件夹不能修改" );
            }

            File f = new File(fileDir + oldFile );
            File smF = new File(fileDir + "sm/" + oldFile );
            File nFile = new File(fileDir + newFile );
            File nsmFile = new File(fileDir + "sm/" + newFile );
            if (f.renameTo(nFile)) {
                if (smF.exists()) {
                    smF.renameTo(nsmFile);
                }
                //记录日志
                recordLog(oldFile, newFile, session);
                //记录日志
                Object o = session.getAttribute("LOGIN_USER");
                if(o != null && o instanceof User) {
                    User tmp = (User) o;
                    String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    //记录文件上传属性
                    FileRecord fileRecord = fileRecordServers.findByCurnameAndUname(oldFile,tmp.getUname());
                    if(fileRecord != null){
                        fileRecord.setUrl(fileDir + newFile );
                        fileRecord.setUname(tmp.getUname());
                        fileRecord.setCurname(newFile);
                        fileRecord.setOrgname(oldFile);
                        fileRecord.setTime(date);
                        if(nFile.isDirectory()){
                            fileRecord.setType("dir");
                        }else{
                            fileRecord.setType("file");
                        }
                        fileRecordServers.save(fileRecord);
                    }

                }

                return getRS(200, "重命名成功", SLASH + newFile );
            }
        }
        return getRS(500, "重命名失败" );
    }

    private void recordLog(String oldFile, String newFile, HttpSession session) {
        //记录日志
        Object o = session.getAttribute("LOGIN_USER");
        if(o != null && o instanceof User) {
            User tmp = (User) o;
            Logs logs = new Logs();
            logs.setLog(tmp.getUname()+"重名文件【"+oldFile+"】修改成【"+newFile+"】");
            String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            logs.setTime(date);
            logs.setUserid(tmp.getId()+"");
            logs.setUname(tmp.getUname());
            logsServers.saveLogs(logs);

        }
    }

    /**
     * 获取当前日期
     */
    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        return sdf.format(new Date());
    }

    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @param url
     * @return Map
     */
    private Map getRS(int code, String msg, String url) {
        Map<String, Object> map = new HashMap<>();
        map.put( "code", code );
        map.put( "msg", msg );
        if (url != null) {
            map.put( "url", url );
        }
        return map;
    }

    /**
     * 封装返回结果
     *
     * @param code
     * @param msg
     * @return Map
     */
    private Map getRS(int code, String msg) {
        return getRS(code, msg, null);
    }

    /**
     * 新建文件夹
     *
     * @param curPos
     * @param dirName
     * @return Map
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/mkdir")
    public Map mkdir(String curPos, String dirName,HttpSession session) {
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (!StringUtils.isEmpty(curPos) && !StringUtils.isEmpty(dirName)) {
            curPos = curPos.substring(1);
            String dirPath = fileDir + curPos + SLASH + dirName;
            File f = new File(dirPath);
            if (f.exists()) {
                return getRS( 500, "目录已存在" );
            }
            if (!f.exists() && f.mkdir()) {
                //记录日志
                Object o = session.getAttribute("LOGIN_USER");
                if(o != null && o instanceof User) {
                    User tmp = (User) o;
                    Logs logs = new Logs();
                    logs.setLog(tmp.getUname()+"创建文件夹【"+dirName+"】到【"+curPos+"】下");
                    String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    logs.setTime(date);
                    logs.setUserid(tmp.getId()+"");
                    logs.setUname(tmp.getUname());
                    logsServers.saveLogs(logs);
                    //记录文件上传属性
                    FileRecord fileRecord = new FileRecord();
                    fileRecord.setUrl(dirPath);
                    fileRecord.setUname(tmp.getUname());
                    fileRecord.setCurname(dirName);
                    fileRecord.setOrgname(dirName);
                    fileRecord.setTime(date);
                    fileRecord.setType("dir");
                    fileRecordServers.save(fileRecord);
                }
                return getRS(200, "创建成功" );
            }
        }
        return getRS(500, "创建失败" );
    }

    /**
     * 分享文件
     *
     * @param file 文件
     * @param time 有效时间(分钟)
     * @return Map
     */
    @Login
    @ResponseBody
    @PostMapping("/api/share")
    public Map share(String file, int time) {
        // 若文件已经分享
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsValue(file)) {
                Set<String> set = CacheUtil.dataExpireMap.keySet();
                // 找出分享的key
                String key = null;
                for (String t : set) {
                    if (CacheUtil.get(t) != null && CacheUtil.get(t).equals(file)) {
                        key = t;
                        break;
                    }
                }
                // 是否在有效期内
                if (key != null) {
                    Date expireDate = CacheUtil.dataExpireMap.get(key);
                    if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                        return getRS(200, "该文件已分享", domain + SLASH + "share?sid=" + key );
                    }
                }
            }
        }
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        String sid = UUID.randomUUID().toString();
        CacheUtil.put( sid, file, time );
        return getRS(200, "分享成功", domain + SLASH + "share?sid=" + sid );
    }

    /**
     * 分享文件展示页面
     *
     * @param sid 分享文件sid
     * @param modelMap
     * @return
     */
    @GetMapping("/share")
    public String sharePage(@RequestParam(value = "sid", required = true) String sid, ModelMap modelMap) {
        if (!CacheUtil.dataMap.isEmpty()) {
            if (CacheUtil.dataMap.containsKey(sid)) {
                // 是否在有效期内
                Date expireDate = CacheUtil.dataExpireMap.get(sid);
                if (expireDate != null && expireDate.compareTo(new Date()) > 0) {
                    String url = (String) CacheUtil.get(sid);
                    // 文件是否存在
                    File existFile = new File(fileDir + url );
                    if (!existFile.exists()) {
                        modelMap.put( "exists", false );
                        modelMap.put( "msg", "该文件已不存在~" );
                        return "share";
                    }
                    // 检测文件类型
                    String contentType = null;
                    String suffix = existFile.getName().substring( existFile.getName().lastIndexOf(".") + 1 );
                    try {
                        contentType = new Tika().detect(existFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 获取文件图标、文件名、图片文件缩略图片地址、过期时间
                    modelMap.put( "sid", sid );
                    modelMap.put( "type", getFileType(suffix, contentType) );
                    modelMap.put( "exists", true );
                    modelMap.put( "fileName", url.substring(url.lastIndexOf('/') + 1) );
                    // 是否有缩略图
                    String smUrl = "sm/" + url;
                    if (new File(fileDir + smUrl ).exists()) {
                        modelMap.put( "hasSm", true );
                        // 缩略图地址
                        modelMap.put( "smUrl", "share/file/sm?sid=" + sid );
                    }
                    modelMap.put( "expireTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(CacheUtil.dataExpireMap.get(sid)) );
                    // 是否支持浏览器在线查看
                    boolean flag = false;
                    if (FileTypeUtil.canOnlinePreview(contentType)) {
                        flag = true;
                    }
                    modelMap.put( "preview", flag );
                    return "share";
                }
            }
        }
        modelMap.put( "exists", false );
        modelMap.put( "msg", "分享不存在或已经失效~" );
        return "share";
    }

    /**
     * 获取文件属性
     * @param fileurl
     * @param session
     * @return
     * @throws Exception
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/getAttr")
    public ModelMap getAttr(String fileurl ,String cururl,HttpSession session){
        ModelMap modelMap = new ModelMap();
        Object o = session.getAttribute("LOGIN_USER");
        String uname = "";
        if(o != null){
            User u = (User)o;
            uname = u.getUname();
        }
        if(fileurl == null){
            modelMap.put("code","-1");
            modelMap.put("msg","未找到对应的文件");
            return modelMap;
        }

        if("/".equals(fileurl)){
            //根节点 目录属性。通过查询所有已记录的文件的大小来汇总
            List<FileRecord> fileRecords =  fileRecordServers.findAll();
            long length =0;
            for(FileRecord fileRecord:fileRecords){
                length+=fileRecord.getLength() == null ?0:fileRecord.getLength();
            }
            FileRecord fileRecord = new FileRecord();
            fileRecord.setSize(FileUtil.FormetFileSize(length));
            fileRecord.setCurname("root");
            fileRecord.setUname("超级管理员");
            try {
                File f = new File(fileDir );
                fileRecord.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(f.lastModified()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            modelMap.put("attr",fileRecord);
            modelMap.put("code","1");
        }else{
            String curname = fileurl.substring(fileurl.lastIndexOf("/")+1,fileurl.length());
            List<FileRecord> fileRecord = fileRecordServers.findByCurnameAndUrlLike(curname,fileurl);
            if(fileRecord != null && fileRecord.size()>0){
//                if(fileRecord.getLength() == null || fileRecord.getLength() == 0 || StringUtils.isEmpty(fileRecord.getSize())){
                    //获取文件大小
                    long length =0;
                    //目录属性  查询目录下的所有文件大小进行汇总最新的目录大小
                    List<FileRecord> fileRecords =  fileRecordServers.findByUrlLike(fileurl);
                    for(FileRecord fc:fileRecords){
                        length+=fc.getLength() == null ?0:fc.getLength();
                    }
                FileRecord  fileRecord_res = new FileRecord();
                   /* try {//直接遍历文件取大小。当文件超多的时候，耗时耗资源
                        File f = new File(fileDir + fileurl );
                        if(f.isDirectory()){
                            length =  FileUtil.getFileSizes(f);
                        }else{
                            length =  FileUtil.getFileSize(f);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                fileRecord_res.setLength(length);
                fileRecord_res.setCurname(curname);
                fileRecord_res.setSize(FileUtil.FormetFileSize(length));
                fileRecord_res.setUname(fileRecords.get(0).getUname());
                fileRecord_res.setTime(fileRecords.get(0).getTime());
                modelMap.put("attr",fileRecord_res);
                modelMap.put("code","1");
            }else{//如果文件信息没有存储在系统表里。在这里取文件进行补存
                FileRecord  fileRecord_res = new FileRecord();
                 long length =0;
                 try {
                    File f = new File(fileDir + fileurl );
                    if(f.isDirectory()){
                        //文件夹，不存在到表中，这样查看属性时会对文件夹重新求大小，保证文件夹大小准确
                        length =  FileUtil.getFileSizes(f);
                        fileRecord_res.setType("dir");
                    }else{
                        //如果是文件的话，将文件大小保存到表中
                        length =  FileUtil.getFileSize(f);
                        fileRecord_res.setLength(length);
                        fileRecord_res.setType("file");
                        fileRecord_res.setSize(FileUtil.FormetFileSize(length));
                    }
                     fileRecord_res.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(f.lastModified()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fileRecord_res.setOrgname(curname);
                fileRecord_res.setCurname(curname);
                fileRecord_res.setUname("未知");
                String url = fileDir+fileurl;
                fileRecord_res.setUrl(url);
                FileRecord newfile = fileRecordServers.save(fileRecord_res);
                if(newfile != null){
                   if(StringUtils.isEmpty(newfile.getSize())){
                       //如果存储的是文件夹，这里将文件夹的大小放到前端
                       fileRecord_res.setSize(FileUtil.FormetFileSize(length));
                   }
                    modelMap.put("attr",fileRecord_res);
                    modelMap.put("code","1");
                }else{
                    modelMap.put("code","-1");
                    modelMap.put("msg","未找到对应的文件");
                }
            }
        }
        return modelMap;

    }

    /**
     * 模糊查询搜索文件
     * @param filename
     * @param session
     * @return
     * @throws Exception
     */
    @Login
    @ResponseBody
    @RequestMapping("/api/queryFileByName")
    public ModelMap queryFileByName(String curPos,String filename ,HttpSession session)throws Exception{
        ModelMap modelMap = new ModelMap();
        Object o = session.getAttribute("LOGIN_USER");
        String uname = "";
        if(o != null){
            User u = (User)o;
            uname = u.getUname();
        }
//        List<FileRecord> fileRecord = fileRecordServers.findByUnameAndCurnameLike(uname,filename);
        if (fileDir == null) {
            fileDir = SLASH;
        }
        if (!fileDir.endsWith(SLASH)) {
            fileDir += SLASH;
        }
        if (!StringUtils.isEmpty(curPos)) {
            String dirPath = fileDir + curPos;
            File f = new File(dirPath);
            List<ModelMap> list = new ArrayList<>();
            FileUtil.searchFile(f,filename,list);
            if(list != null && list.size()>0){
                modelMap.put("data",list);
                modelMap.put("code","1");
            }else{
                modelMap.put("code","-1");
                modelMap.put("msg","未找到对应的文件");
            }
        }
        return modelMap;

    }
}
