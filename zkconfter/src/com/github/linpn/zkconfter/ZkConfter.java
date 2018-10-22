package com.github.linpn.zkconfter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.linpn.zkconfter.util.ZkClient;
import com.github.linpn.zkconfter.annotation.DRMAttribute;
import com.github.linpn.zkconfter.annotation.DRMResource;
import com.github.linpn.zkconfter.util.BeanUtils;
import org.I0Itec.zkclient.IZkDataListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;


/**
 * ZkConfter 主操作对象，可以配置在Spring中
 */
public class ZkConfter implements InitializingBean {

    protected final static Log logger = LogFactory.getLog(ZkConfter.class);
    private final static String DEFAULT_ZKCONFTER_FILE = "zkconfter.properties";
    private final static String ZK_ROOT = "/zkconfter/";

    private Resource resource;
    private Resource[] resources;

    private ZkClient zkClient;
    private String appRoot;

    private String zkServers;
    private String appName;
    private String root;
    private String runtime;
    private String sync;
    private String drm;
    private String drmPackages;
    private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private static Properties zkConfigProps = new Properties();
    private static Map<String, Object> zkDrmMaps = new HashMap<String, Object>();

    /**
     * 构造函数
     */
    public ZkConfter() {
    }

    /**
     * 构造函数
     *
     * @param zkConfterFile 配置文件
     */
    public ZkConfter(String zkConfterFile) {
        try {
            this.resource = new ClassPathResource(zkConfterFile);

            if (!this.resource.exists()) {
                this.resource = new FileSystemResource(zkConfterFile);

                if (!this.resource.exists()) {
                    this.resource = new UrlResource(zkConfterFile);
                }
            }

            this.afterPropertiesSet();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.appName == null || this.appName.equals(""))
            this.init();

        if (!this.zkServers.equals("") && this.sync.equals("true")) {
            this.syncZkConfter();
            if (drm.equals("true")) {
                this.drmZkConfter();
            }
        } else {
            //载入到Properties
            List<Resource> list = new ArrayList<Resource>();
            list.addAll(Arrays.asList(resolver.getResources("file:/" + this.getLcRoot() + "/**")));
            list.addAll(Arrays.asList(resolver.getResources("file:/" + this.getLcPath() + "/**")));
            this.resources = list.toArray(new Resource[list.size()]);

            zkConfigProps.load(resource.getInputStream());
            for (Resource res : resources) {
                zkConfigProps.load(res.getInputStream());
            }
        }
    }

    /**
     * 初始化配置参数
     *
     * @throws IOException 抛出IO异常
     */
    public void init() throws IOException {
        logger.info("加载ZkConfter配置中心...");

        //获取zkconfter配置
        Properties zkProps = new Properties();
        if (resource == null || !resource.exists()) {
            zkProps.load(this.getClass().getClassLoader().getResourceAsStream(DEFAULT_ZKCONFTER_FILE));
        } else {
            zkProps.load(resource.getInputStream());
        }

        //系统名
        appName = zkProps.getProperty(SysConstant.APP_NAME, "zkconfter");
        logger.info(SysConstant.APP_NAME + " = " + appName);

        //Zk配置中心地址
        zkServers = zkProps.getProperty(SysConstant.ZK_SERVERS, "");
        logger.info(SysConstant.ZK_SERVERS + " = " + zkServers);

        //本地配置文件目录
        root = zkProps.getProperty(SysConstant.ROOT, "conf");
        logger.info(SysConstant.ROOT + " = " + root);

        //当前运行的环境
        runtime = zkProps.getProperty(SysConstant.RUNTIME, "dev");
        logger.info(SysConstant.RUNTIME + " = " + runtime);

        //是否从服务器同步(下载)配置文件
        sync = zkProps.getProperty(SysConstant.SYNC, "false");
        logger.info(SysConstant.SYNC + " = " + sync);

        //是否启用动态资源管控(DRM)
        drm = zkProps.getProperty(SysConstant.DRM, "false");
        logger.info(SysConstant.DRM + " = " + drm);

        //从哪个包开始递归扫描DRM注解
        drmPackages = zkProps.getProperty(SysConstant.DRM_PACKAGES, "");
        logger.info(SysConstant.DRM_PACKAGES + " = " + drmPackages);

        //验证配置项
        if (zkServers.equals(""))
            throw new NullPointerException("Property zkconfter.zkServers cannot be null.");
        if (appName.equals(""))
            throw new NullPointerException("Property zkconfter.appName cannot be null.");
    }

    /**
     * 同步配置中心
     *
     * @throws IOException 抛出IO异常
     */
    public void syncZkConfter() throws IOException {
        //创建ZkClient对象
        zkClient = new ZkClient(zkServers);

        //创建配置中心根目录
        String zkPath = this.getZkPath();
        if (!zkClient.exists(zkPath)) {
            zkClient.create(zkPath, CreateMode.PERSISTENT, true);
        }

        //获取配置中心文件列表
        if (!zkClient.hasChildren(zkPath)) {
            //如果没有子节点，则上传初始化配置文件
            this.uploadZkConfter();
        } else {
            //如果有子节点，则直接下载并覆盖本地文件
            this.downloadZkConfter();
        }
    }

    /**
     * 上传本地文件至配置中心
     *
     * @throws IOException 抛出IO异常
     */
    public void uploadZkConfter() throws IOException {
        logger.info("开始向ZkConfter配置中心上传文件...");

        Resource[] resources = resolver.getResources("file:/" + this.getLcPath() + "/**");

        //上传文件到配置中心
        for (Resource res : resources) {
            String lcPath = ((FileSystemResource) res).getPath();
            String zkPath = this.getZkPathByLcPath(lcPath);

            //上传配置文件
            byte[] data = new byte[(int) res.getFile().length()];
            InputStream in = null;
            try {
                in = res.getInputStream();
                in.read(data);
            } catch (IOException e) {
                throw e;
            } finally {
                if (in != null)
                    in.close();
            }
            zkClient.writeData(zkPath, data, CreateMode.PERSISTENT);
            logger.info("上传文件:" + zkPath);

            //载入到Properties
            zkConfigProps.load(res.getInputStream());
        }
    }

    /**
     * 下载配置中心文件至本地
     *
     * @throws IOException 抛出IO异常
     */
    public void downloadZkConfter() throws IOException {
        logger.info("开始从ZkConfter配置中心下载文件...");

        //从配置中心下载文件
        List<String> zkPathList = zkClient.getChildrenOfFullPathRecursive(this.getZkPath());
        for (Iterator<String> it = zkPathList.iterator(); it.hasNext(); ) {
            String zkPath = it.next();
            byte[] data = zkClient.readData(zkPath);

            //如果不是文件节点，则不下载
            if (data == null || data.length == 0) {
                it.remove();
                continue;
            }

            String lcPath = this.getLcPathByZkPath(zkPath);
            File file = new File(lcPath);
            if (!file.exists())
                file.createNewFile();

            //下载配置文件
            FileOutputStream on = null;
            try {
                on = new FileOutputStream(file);
                on.write(data);
            } catch (IOException e) {
                throw e;
            } finally {
                if (on != null)
                    on.close();
            }
            logger.info("下载文件:" + zkPath);

            //载入到Properties
            zkConfigProps.load(new FileInputStream(file));
        }
    }

    /**
     * 启动并同步动态资源(DRM)
     *
     * @throws IllegalAccessException IllegalAccessException
     * @throws InstantiationException InstantiationException
     */
    public void drmZkConfter() throws IllegalAccessException, InstantiationException {
        if (drmPackages == null || drmPackages.equals(""))
            return;

        // 获取包下的class类
        String[] packages = drmPackages.split(",");
        Set<Class<?>> sets = new HashSet<Class<?>>();
        for (String p : packages) {
            sets.addAll(BeanUtils.getClasses(p));
        }

        List<Class<?>> list = new ArrayList<Class<?>>();
        list.addAll(sets);

        for (Class<?> clazz : list) {
            DRMResource drmResource = clazz.getAnnotation(DRMResource.class);
            if (drmResource != null) {
                // 实例化
                final Object inst = clazz.newInstance();

                // 创建类节点
                String zkDrmResource = this.getZkDrmPath() + "/" + clazz.getCanonicalName();
                if (!zkClient.exists(zkDrmResource)) {
                    zkClient.create(zkDrmResource, CreateMode.PERSISTENT, true);
                }

                // 设置资源节点
                JSONObject dataResource = new JSONObject();
                dataResource.put("name", drmResource.name());
                dataResource.put("description", drmResource.description());
                zkClient.writeData(zkDrmResource, dataResource.toString(), CreateMode.PERSISTENT);

                // 创建属性节点
                Field[] fields = clazz.getDeclaredFields();
                for (final Field field : fields) {
                    DRMAttribute drmAttribute = field.getAnnotation(DRMAttribute.class);
                    if (drmAttribute != null) {
                        field.setAccessible(true);
                        String zkDrmAttribute = zkDrmResource + "/" + field.getName();
                        if (!zkClient.exists(zkDrmAttribute)) {
                            zkClient.create(zkDrmAttribute, CreateMode.PERSISTENT, true);

                            // 设置属性节点
                            JSONObject dataAttribute = new JSONObject();
                            dataAttribute.put("name", drmAttribute.name());
                            dataAttribute.put("description", drmAttribute.description());
                            dataAttribute.put("value", field.get(inst));
                            zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);
                        } else {
                            // 设置属性节点
                            JSONObject dataAttribute = JSON.parseObject(zkClient.readData(zkDrmAttribute).toString());
                            dataAttribute.put("name", drmAttribute.name());
                            dataAttribute.put("description", drmAttribute.description());
                            field.set(inst, dataAttribute.get("value"));
                            zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);
                        }

                        // 监听动态资源(DRM)
                        zkClient.subscribeDataChanges(zkDrmAttribute, new IZkDataListener() {
                            @Override
                            public void handleDataChange(String dataPath, Object data) throws Exception {
                                JSONObject dataAttribute = JSON.parseObject(data.toString());
                                Object value = dataAttribute.get("value");
                                field.set(inst, value);
                                logger.info("推送DRM属性" + dataPath + "，值为:" + value);
                            }

                            @Override
                            public void handleDataDeleted(String dataPath) throws Exception {
                                logger.info("DRM清除废弃属性:" + dataPath);
                            }
                        });
                    }
                }

                //清除已废弃的属性节点
                List<String> zkDrmAttributePaths = zkClient.getChildrenOfFullPathRecursive(zkDrmResource);
                for (String path : zkDrmAttributePaths) {
                    boolean flag = true;
                    for (Field field : fields) {
                        String zkDrmAttribute = zkDrmResource + "/" + field.getName();
                        if (path.equals(zkDrmAttribute)) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) {
                        zkClient.deleteRecursive(path);
                    }
                }

                // 载入到drm对象池
                zkDrmMaps.put(clazz.getCanonicalName(), inst);
            }
        }
    }


    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public Properties getProperties() {
        return zkConfigProps;
    }

    public void load(String filename) {
        try {
            Resource res = new ClassPathResource(filename);

            if (!res.exists()) {
                res = new FileSystemResource(filename);

                if (!res.exists()) {
                    res = new UrlResource(filename);
                }
            }

            this.getProperties().load(res.getInputStream());

        } catch (Exception e) {
            logger.info("No file: " + filename);
        }
    }


    private String getAppRoot() {
        if (appRoot != null)
            return appRoot;

        File file;

        //classpath目录
        String classpath = ZkConfter.class.getResource("/").toString().replaceFirst("file:", "");
        appRoot = classpath;
        file = new File(appRoot + root);
        if (file.exists())
            return appRoot;

        if (classpath.lastIndexOf("WEB-INF/classes/") > -1) {
            //如果是WEB系统，获取WEB-INF目录
            appRoot = classpath.replaceAll("classes/", "");
            file = new File(appRoot + root);
            if (file.exists())
                return appRoot;

            //获取WEB系统根目录
            appRoot = classpath.replaceAll("WEB-INF/classes/", "");
            file = new File(appRoot + root);
            if (file.exists())
                return appRoot;
        } else {
            //如果不是WEB系统，获取应用根目录
            appRoot = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/";
            file = new File(appRoot + root);
            if (file.exists())
                return appRoot;
        }

        appRoot = "";
        return appRoot;
    }

    private String getLcRoot() {
        return getAppRoot() + root;
    }

    private String getZkRoot() {
        return ZK_ROOT + appName + "/resource";
    }

    private String getZkDrmRoot() {
        return ZK_ROOT + appName + "/drm";
    }

    private String getLcPath() {
        return getLcRoot() + (runtime.equals("") ? "" : "/" + runtime);
    }

    private String getZkPath() {
        return getZkRoot() + (runtime.equals("") ? "" : "/" + runtime);
    }

    private String getZkDrmPath() {
        return getZkDrmRoot() + (runtime.equals("") ? "" : "/" + runtime);
    }

    private String getLcPathByZkPath(String zkPath) {
        return getLcRoot() + zkPath.replaceFirst(getZkRoot(), "");
    }

    private String getZkPathByLcPath(String lcPath) {
        return getZkRoot() + lcPath.replaceFirst(getLcRoot(), "");
    }


    /**
     * 获取drm对象，该对象在系统连接配置中心时创建，为本地单例的对象
     *
     * @param clazz DRM对象类型
     * @param <T>   DRM对象类型
     * @return 返回drm对象的实例，如果获取不到，则返回空
     */
    public static <T> T drm(Class<T> clazz) {
        return (T) zkDrmMaps.get(clazz.getCanonicalName());
    }

    /**
     * 获取所有drm对象
     *
     * @return 返回所有drm对象
     */
    public static Map<String, Object> drm() {
        return zkDrmMaps;
    }

    /**
     * 获取已从配置中心下载过来的配置值
     *
     * @param key 配置文件的参数key
     * @return 返回配置值，如果获取不到，返回空串
     */
    public static String config(String key) {
        return zkConfigProps.getProperty(key, "");
    }

    /**
     * 获取所有已从配置中心下载过来的配置信息
     *
     * @return 返回配置信息
     */
    public static Properties config() {
        return zkConfigProps;
    }


    /**
     * 获取所有从配置中心全新下载的配置信息
     *
     * @return 返回配置信息
     */
    public static Properties getAllNewConfig(String zkConfterFile) {
        ZkConfter zkConfter = new ZkConfter(zkConfterFile);
        return zkConfter.getProperties();
    }
}
