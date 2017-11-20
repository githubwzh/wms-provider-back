package com.womai.wms.rf.manager.util;

import com.womai.auth.ClientApi;
import com.womai.auth.soa.domain.Authority;
import com.womai.auth.soa.domain.UserMenus;
import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.WhEnum;
import com.womai.wms.rf.common.util.JedisUtils;
import com.womai.wms.rf.common.util.ManagerLog;
import com.womai.wms.rf.common.util.WMSDebugManager;
import com.womai.wms.rf.manager.window.menu.MenuElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

/**
 * ClassDescribe: 用户菜单权限缓存处理
 * Author :Xiafei Qi
 * Date: 2016-08-15
 * Since
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component
public class UserMenuAuthCache {
    @Autowired
    ClientApi authClientService;

    private static final String KEY_SPLITE = ":"; // 缓存key各名称之间的分隔符
    private static final String LEVELONE_PID_KEY_PREFIX = "MENULEVELONE".concat(KEY_SPLITE).concat("PID").concat(KEY_SPLITE);// 一级菜单PID键的前缀，完整格式MENULEVELONE:PID:whno
    private static final String MENULIST_KEY_PREFIX = "MENULIST".concat(KEY_SPLITE); // 层级关系键前缀，完整格式MENULIST:whno:userid:pid，value：menuid...
    private static final String MENU_KEY_PREFIX = "MENU".concat(KEY_SPLITE); // 菜单键前缀，完整格式MENU:whno:userid:menuid
    private static final String VERSION_KEY_PREFIX = "VERSION".concat(KEY_SPLITE);// 版本号键前缀，完整格式VERSION:whno:userid
    private static final String SET_KEY_STORAGE_KEY_PREFIX = "KEYSET".concat(KEY_SPLITE); // 存放缓存key的集合，完整格式KEYSET:whno:userid
    private static final String SET_KEY_STOKRAGE_MENUID_PREFIX = "MENUIDSET".concat(KEY_SPLITE);// 储存菜单id的集合，完成格式MENUIDSET:whno:userid

    // Redis存储的key和field定义
    private final static String MENU_ID_FIELD = "menuid";// 菜单权限列表中，记录menuid的field
    private final static String MENU_NAME_FIELD = "name";// 菜单权限列表中，记录菜单描述的field
    private final static String MENU_URL_FIELD = "url";// 菜单权限列表中，记录菜单指向handler地址的field
    private final static String MENU_PID_FIELD = "pid";// 菜单权限列表中，记录菜单父id的field
    private final static String MENU_LEVEL_FIELD = "level";// 菜单权限列表中，记录当前菜单层级的field

    /**
     * 每次登陆或权限发生变化或Redis缓存中找不到key都会调用该方法，刷新用户缓存
     *
     * @param userId      用户id
     * @param currentSite 当前站点
     * @throws JedisException      Jedis异常
     * @throws RFMenuAuthException 可恢复异常，通常跳到菜单页并打印错误提示
     * @throws RFFatalException    严重异常，通常需要直接跳转到登录页面
     */
    public void refreshUserMenuAuthCache(Long userId, String currentSite) throws JedisException, RFMenuAuthException, RFFatalException {
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            // 查询用户权限列表信息
            UserMenus userMenus = authClientService.queryUserMenusByUserId(userId, currentSite, Constants.APPTYPE);
            fullDelAndSetUserMenuAuthCache(userId.toString(), currentSite, userMenus, jedis);// 全量删除再储存菜单权限缓存
        } finally {
            JedisUtils.returnResource(jedis);
        }

    }

    /**
     * 检测用户在某个站点是否存在权限
     *
     * @param userId      用户id
     * @param currentSite 站点编码
     * @return 存在权限返回true，不存在权限返回false
     * @throws Exception 抛出异常
     */
    public boolean isExistAuthorityBySite(Long userId, String currentSite) throws Exception {
        UserMenus userMenus = authClientService.queryUserMenusByUserId(userId, currentSite, Constants.APPTYPE);
        if (userMenus == null || userMenus.getAuthorityList() == null || userMenus.getAuthorityList().size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 根据用户id、站点、是否一级菜单、选择菜单id信息获取下级菜单列表
     * MenuElement 自定义出菜单对象
     *
     * @param userId      用户id
     * @param currentSite 当前站点编码
     * @param isLevelOne  是否是一级菜单，true：是，false：否
     * @param menuid      选择的菜单id，作为获得下级菜单的pid 当isLevelOne==true时该参数没用
     * @return 下级菜单列表，不存在空列表，若列表为空抛出RFMenuAuthException异常
     * @throws RFMenuAuthException 可恢复异常，通常跳到菜单页并打印错误提示
     * @throws RFFatalException    严重异常，通常需要直接跳转到登录页面
     */
    public List<MenuElement> getMenus(String userId, String currentSite, boolean isLevelOne, String menuid) throws RFMenuAuthException, RFFatalException {
        Jedis jedis = null;
        try {
            List<MenuElement> menuElementList; // 菜单列表
            jedis = JedisUtils.getResource();
            if (isLevelOne) {  //  如果是一级菜单，从缓存中取出一级菜单的pid，然后根据pid找到一级菜单项，跟menuid无关
                String menuLevelOnepidKey = LEVELONE_PID_KEY_PREFIX.concat(currentSite); // 一级菜单的pid在redis中的key
                String pid = jedis.get(menuLevelOnepidKey);
                if (pid == null) {
                    ManagerLog.errorLog(getClass().getName().concat("-->>--getMenus()-->>--Redis中找不到一级菜单的pid，一级菜单pid的redis-key：").concat(menuLevelOnepidKey));
                    throw new RFMenuAuthException(Constants.AUTH_SYSTEM_ERROR);
                }
                menuElementList = queryMenuElementsByPid(userId, currentSite, pid, jedis);// 根据pid获取菜单列表
            } else {//  如果不是一级菜单，将menuid作为pid，然后找到子菜单项
                menuElementList = queryMenuElementsByPid(userId, currentSite, menuid, jedis);// 根据pid获取菜单列表
            }
            if (menuElementList.size() == 0) { // 如果根据用户id、站点、pid在缓存中找不到菜单信息，说明无权限
                String currentSiteName = WhEnum.getWhName(currentSite);
                if (currentSiteName == null) {
                    ManagerLog.errorLog(getClass().getName().concat("-->>--getMenus()-->>--用户站点在WhEnum中匹配不到,用户站点编码：" + currentSite));
                    throw new RFFatalException(Constants.SITE_ERROR);// 如果匹配不到，用户站点异常
                }
                throw new RFMenuAuthException(currentSiteName.concat(Constants.MENU_IS_NULL_TIP_SUFFIX));
            }
            return menuElementList;
        } finally {
            JedisUtils.returnResource(jedis);
        }

    }

    /**
     * 判断用户权限是否有变化
     *
     * @param userId      用户id
     * @param currentSite 当前站点编码
     * @return true：变化，false：不变化
     * @throws Exception 获取不到jedis连接时报错
     */
    public boolean isChangeUserMenu(Long userId, String currentSite) throws Exception {
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            String version = jedis.get(VERSION_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId.toString()));
            if (version != null) { // 如果version不等于空，调用auth接口判断权限是否有变化，若version为空，说明该用户该站点权限还没有初始化，也返回变化
                UserMenus userMenus = new UserMenus(); // 调用接口参数
                userMenus.setApptype(Constants.APPTYPE);// 权限模块类型，RF--2
                userMenus.setUserid(userId); // 用户id
                userMenus.setCurrentsite(currentSite);// 站点编码
                userMenus.setAuthorityversioncount(Long.parseLong(version)); // 权限列表版本合计
                // 从缓存中取出所有菜单id集合，组装菜单id数组
                Set<String> menuidSet = jedis.smembers(SET_KEY_STOKRAGE_MENUID_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId.toString()));
                if (menuidSet == null) {// 如果缓存数据丢失，返回权限变化去刷新权限
                    WMSDebugManager.errorLog(getClass().getName().concat("-->>--isChangeUserMenu-->>--从Redis取所有菜单id集合时取不到，缓存异常"));
                    return true;
                }
                Long[] authorityArray = new Long[menuidSet.size()];
                int index = 0;
                for (String s : menuidSet) {// 将id放入数组转型成Long
                    try {
                        authorityArray[index] = Long.parseLong(s);
                        index++;
                    } catch (NumberFormatException e) {
                        WMSDebugManager.errorLog(getClass().getName().concat("-->>--isChangeUserMenu-->>--从Redis取所有菜单id集合后转换Long类型报错，id：").concat(s));
                        return true;
                    }
                }
                userMenus.setAuthorityArray(authorityArray);//放到userMunus对象里
//                String userMenusData =  "apptype:"+userMenus.getApptype()+";AuthorityArray:"+ makeStringByLongArr(userMenus.getAuthorityArray())
//                        +";AuthorityList:"+ userMenus.getAuthorityList()+";Authorityversioncount:"+ userMenus.getAuthorityversioncount()
//                        +";:Currentsite"+ userMenus.getCurrentsite()+";:Userid"+ userMenus.getUserid();
//                WMSDebugManager.debugLog("校验权限修改参数:"+userMenusData);
                boolean result = authClientService.isChangeUserMenu(userMenus);
//                System.out.println("返回的参数："+result);
                return result;
            } else {
                return true;
            }

        } finally {
            JedisUtils.returnResource(jedis);
        }
    }

    /**
     * 临时方法，输出Long数组
     *
     * @param longArr
     * @return
     */
    private static String makeStringByLongArr(Long[] longArr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Long ll : longArr) {
            sb.append(ll).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 全量删除再储存菜单权限缓存
     *
     * @param userId      用户id
     * @param currentSite 当前站点
     * @param userMenus   用户的菜单对象，从Person项目中查的
     * @param jedis       Java-Redis 接口
     * @throws JedisException      Jedis异常
     * @throws RFMenuAuthException 可恢复异常，通常跳到菜单页并打印错误提示
     * @throws RFFatalException    严重异常，通常需要直接跳转到登录页面
     */
    private void fullDelAndSetUserMenuAuthCache(String userId, String currentSite, UserMenus userMenus, Jedis jedis) throws JedisException, RFMenuAuthException, RFFatalException {
        String logSuffix = ",用户站点编码：" + currentSite + ",用户id：" + userId + ",权限对象:" + userMenus;
        if (userMenus != null && userMenus.getAuthorityList() == null) {
            ManagerLog.errorLog(getClass().getName().concat("-->>--fullDelAndSetUserMenuAuthCache()-->>--用户在该站点下无权限".concat(logSuffix)));
            throw new RFMenuAuthException(Constants.AUTH_SYSTEM_ERROR);
        } else if (userMenus == null) { // 如果用户在该站点下无权限，userMenus对象为空
            String currentSiteName = WhEnum.getWhName(currentSite);
            if (currentSiteName == null) {
                ManagerLog.errorLog(getClass().getName().concat("-->>--fullDelAndSetUserMenuAuthCache()-->>--用户站点在WhEnum中匹配不到".concat(logSuffix)));
                throw new RFFatalException(Constants.SITE_ERROR);// 如果匹配不到，用户站点异常
            }
            throw new RFMenuAuthException(currentSiteName.concat(Constants.MENU_IS_NULL_TIP_SUFFIX));
        }
        // 一、该部分操作包裹在Redis事物中执行，所以先准备数据
        // 1.首先准备待删除的key数据
        String setKey = SET_KEY_STORAGE_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId);// Redis集合的key，该集合用来存储当前用户当前站点下的所有缓存的key
        Set<String> keySet = jedis.smembers(setKey);// 取到所有待删除的键的Set
        String[] keysToDel = keySet.toArray(new String[keySet.size()]);// 键待删除键的Set转化成数组

        // 2.然后准备待存储进Redis的数据
        // 2.1 层级关系Map , key -- pid , value -- List<String>  放置某一级菜单的菜单id的有序列表，主键是pid
        Map<String, List<String>> pidMenuListMap = new HashMap<String, List<String>>();
        // 2.2 菜单列表map , key -- menuid , value -- map 放置菜单的各项属性,主键是菜单id
        Map<String, Map<String, String>> menuMap = new HashMap<String, Map<String, String>>();
        // 2.3 所有要存储的key集合
        List<String> keysToSave = new ArrayList<String>();
        // 2.4 一级菜单的pid
        String menuLevelOnePid = null;
        // 2.5 菜单id列表
        ArrayList<String> authorityArray = new ArrayList<String>(userMenus.getAuthorityList().size());
        // 遍历Person项目查到的权限列表，生成缓存对象
        for (Authority auth : userMenus.getAuthorityList()) {
            //如果还没找到一级菜单的pid并且当前循环到一级菜单项，将其pid记录下来
            if (menuLevelOnePid == null && auth.getLevel().equals(Constants.MENU_LEVEL_ONE)) {
                menuLevelOnePid = auth.getPid().toString();
            }
            // 将menuid存入层级关系集合
            if (pidMenuListMap.get(auth.getPid().toString()) != null) {
                pidMenuListMap.get(auth.getPid().toString()).add(auth.getId().toString());
            } else {
                List<String> menuIdList = new ArrayList<String>();
                menuIdList.add(auth.getId().toString());
                pidMenuListMap.put(auth.getPid().toString(), menuIdList);
            }
//            System.out.println("权限说明:"+auth.getAuthDesc());
//            System.out.println("权限名称:"+auth.getName());
            // 将菜单信息存入菜单列表
            Map<String, String> menu = new HashMap<String, String>();
            menu.put(MENU_ID_FIELD, auth.getId().toString());
            menu.put(MENU_NAME_FIELD, auth.getName() == null ? "" : auth.getName());
            menu.put(MENU_URL_FIELD, auth.getUrl() == null ? "" : auth.getUrl());
            menu.put(MENU_PID_FIELD, auth.getPid().toString());
            menu.put(MENU_LEVEL_FIELD, auth.getLevel().toString());
            menuMap.put(auth.getId().toString(), menu);
        }
        if (menuLevelOnePid == null) {
            ManagerLog.errorLog(getClass().getName().concat("-->>--fullDelAndSetUserMenuAuthCache()-->>--找不到一级菜单id".concat(logSuffix)));
            throw new RFFatalException(Constants.AUTH_SYSTEM_ERROR);// 如果找不到一级菜单id
        }
        // 二、使用事物进行删除再添加操作
        Transaction transaction = jedis.multi();
        // 删除keySet
        transaction.del(setKey);
        // 批量删除所有该用户在该站点下的权限
        if (keysToDel.length > 0) {
            transaction.del(keysToDel);
        }
        // 批量添加
        // 	缓存【层级关系】，为每一个菜单层级建立一个有序列表，有序列表的key包含用户站点和pid信息，有序列表的内容是该层级下的菜单的id：MENULIST:whno:userid:pid，value：menuid...
        Set<String> pidMenuListKeySet = pidMenuListMap.keySet();
        for (String pid : pidMenuListKeySet) { // 每次循环是一个层级
            List<String> menuIdList = pidMenuListMap.get(pid);// 拿到该层级的菜单id列表
            String key = MENULIST_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId).concat(KEY_SPLITE).concat(pid);// 要存储进redis的有序列表的key
            transaction.rpush(key, menuIdList.toArray(new String[menuIdList.size()]));// 放入redis，添加到列表末尾，若没有该有序列表则新建
            keysToSave.add(key);// 将这个有序列表的key放入已保存key的集合，最后放入保存该用户该站点下所有key的集合，以备之后删除使用
        }

        //	缓存【菜单列表】，使用散列(hash)，为用户权限中包含的每一个菜单建立一个键：MENU:whno:userid:menuid
        // ，字段：ID：menuid，authdesc,url,pid,level
        Set<String> menuKeySet = menuMap.keySet();
        for (String menuId : menuKeySet) {
            String key = MENU_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId).concat(KEY_SPLITE).concat(menuId);// 要存储进redis的有序列表的key
            transaction.hmset(key, menuMap.get(menuId));// jedis包装了散列存储方式，可以直接传一个map进去
            authorityArray.add(menuId);// 将菜单id存入菜单id列表
            keysToSave.add(key);
        }
        //	在Redis中新建或更新键【VERSION:whno:userid】，该键储存queryUserMenusByUserId()返回对象的VERSION字段。
        String versionKey = VERSION_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId);
        transaction.set(versionKey, userMenus.getAuthorityversioncount().toString());
        keysToSave.add(versionKey);
        //	在Redis中缓存键【MENULEVELONE:PID:whno】，该键储存当前仓库的一级菜单的pid。
        // 从权限列表中找到一个level==2的列表对象获取它的pid缓存进键MENULEVELONE:PID:whno。
        String menuLevelOnepidKey = LEVELONE_PID_KEY_PREFIX.concat(currentSite);
        transaction.set(menuLevelOnepidKey, menuLevelOnePid);
        keysToSave.add(menuLevelOnepidKey);
        // 在Redis中缓存集合类型，键【MENUIDSET:whno:userid】，该集合存储该用户在该站点下的所有菜单id，是为了判断菜单是否有变化服务的
        String menuidSetkey = SET_KEY_STOKRAGE_MENUID_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId);
        transaction.sadd(menuidSetkey, authorityArray.toArray(new String[authorityArray.size()]));
        keysToSave.add(menuidSetkey);
        // 在Redis中缓存键【KEYSET:whno:userid】,该键储存所有已缓存的键，用于删除
        transaction.sadd(setKey, keysToSave.toArray(new String[keysToSave.size()]));
        transaction.exec();
    }

    /**
     * 根据pid获取子菜单列表，包括一级菜单
     *
     * @param userId      用户id
     * @param currentSite 当前站点
     * @param pid         父id
     * @param jedis       Java-Redis接口
     * @return 所有父id等于pid的菜单对象列表
     */
    private List<MenuElement> queryMenuElementsByPid(String userId, String currentSite, String pid, Jedis jedis) {
        List<MenuElement> returnList = new ArrayList<MenuElement>();
        // Redis中层级关系列表的key,该关系列表key和pid相关，列表内容是以pid为父id的菜单id
        String menuListKey = MENULIST_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId).concat(KEY_SPLITE).concat(pid);
        List<String> menuidList = jedis.lrange(menuListKey, 0, -1);// 取到层级关系列表中的menuidList列表
        Pipeline pipeline = jedis.pipelined();// 使用pipeline批量读取，减少通讯次数
        for (String id : menuidList) {
            // Redis中菜单Map的key,该关系列表key和pid相关，列表内容是以pid为父id的菜单id
            String menuKey = MENU_KEY_PREFIX.concat(currentSite).concat(KEY_SPLITE).concat(userId).concat(KEY_SPLITE).concat(id);
            // 管道中插入获取菜单属性的命令
            pipeline.hmget(menuKey, MENU_ID_FIELD, MENU_NAME_FIELD, MENU_URL_FIELD, MENU_PID_FIELD, MENU_LEVEL_FIELD);
        }
        List<Object> responseList = pipeline.syncAndReturnAll(); // 提交管道
        for (Object response : responseList) { // 将查询结果组装成菜单对象
            @SuppressWarnings("unchecked")
            List<String> fieldValue = (List<String>) response;
            MenuElement menuElement = new MenuElement();
            menuElement.setId(fieldValue.get(0));
            menuElement.setName(fieldValue.get(1));
            menuElement.setUrl(fieldValue.get(2));
            menuElement.setPid(fieldValue.get(3));
            menuElement.setLevel(fieldValue.get(4));
            returnList.add(menuElement);
        }
        return returnList;
    }

}
