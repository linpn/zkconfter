package com.github.linpn.zkconfter;

/**
 * Created by pinian.lpn on 2015/3/4.
 */
public final class SysConstant {

    /**
     * 系统名
     */
    public final static String APP_NAME = "zkconfter.appName";

    /**
     * Zk配置中心地址
     */
    public final static String ZK_SERVERS = "zkconfter.zkServers";

    /**
     * 配置文件根目录
     */
    public final static String ROOT = "zkconfter.root";

    /**
     * 当前运行的环境 dev: 开发环境; test: 测试环境; www: 正式环境
     */
    public final static String RUNTIME = "zkconfter.runtime";

    /**
     * 是否从服务器同步(下载)配置文件
     */
    public final static String SYNC = "zkconfter.sync";

    /**
     * 是否启用动态资源管控(DRM)
     */
    public final static String DRM = "zkconfter.drm";

    /**
     * 从哪个包开始扫描DRM注解
     */
    public final static String DRM_PACKAGES = "zkconfter.drm.packages";

}
