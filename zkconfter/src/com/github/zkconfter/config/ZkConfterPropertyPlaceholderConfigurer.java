package com.github.zkconfter.config;

import com.github.zkconfter.ZkConfter;

/**
 * 集成配置中心ZkConfter
 * Spring导入properties文件，允许动态配置你要导入哪个目录下的配置文件。
 * @author Linpn
 */
public class ZkConfterPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private ZkConfter zkConfter;
	
	@Override
	public void afterPropertiesSet() throws Exception {
        super.setConfig(zkConfter.getConfig());
        super.afterPropertiesSet();
	}

    public ZkConfter getZkConfter() {
        return zkConfter;
    }

    public void setZkConfter(ZkConfter zkConfter) {
        this.zkConfter = zkConfter;
    }
}
