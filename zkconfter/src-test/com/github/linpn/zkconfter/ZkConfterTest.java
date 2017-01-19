package com.github.linpn.zkconfter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.linpn.zkconfter.util.ZkClient;
import com.github.linpn.zkconfter.drm.DRMResourceTest;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by pinian.lpn on 2015/8/7.
 */
public class ZkConfterTest {

    @Test
    public void testZkConfter() throws Exception {
        ZkConfter zkConfter = new ZkConfter("zkconfter.properties");
        zkConfter.afterPropertiesSet();

        String c = ZkConfter.config("dubbo.registry.address");
        DRMResourceTest d = ZkConfter.drm(DRMResourceTest.class);

        System.out.println(c);
        System.out.println(d);

        ZkClient zkClient = zkConfter.getZkClient();
        String zkDrmAttribute = "/zkconfter/zkconfter-test/drm/test/DRMResourceTest/testString";

        JSONObject dataAttribute = JSON.parseObject(zkClient.readData(zkDrmAttribute).toString());
        dataAttribute.put("value", "2");
        zkClient.writeData(zkDrmAttribute, dataAttribute.toString(), CreateMode.PERSISTENT);

        TimeUnit.SECONDS.sleep(2);
        System.out.println(d.getTestString());
    }
}
