package com.github.zkconfter.util;

import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Zookeeper客户端工具，ZkClient扩展
 *
 * @author Linpn
 */
public class ZkClient extends org.I0Itec.zkclient.ZkClient {

    public ZkClient(String serverstring) {
        super(serverstring);
    }

    public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout) {
        super(zkServers, sessionTimeout, connectionTimeout);
    }

    public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer) {
        super(zkServers, sessionTimeout, connectionTimeout, zkSerializer);
    }


    /**
     * 创建节点
     *
     * @param path          路径
     * @param mode          模式
     * @param createParents 是否递归创建父节点
     */
    public void create(final String path, final CreateMode mode, final boolean createParents) {
        try {
            super.create(path, null, mode);
        } catch (ZkNodeExistsException e) {
            if (!createParents) {
                throw e;
            }
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            this.create(parentDir, mode, createParents);
            this.create(path, mode, createParents);
        }
    }

    /**
     * 当znode不存在时返回空
     */
    public <T extends Object> T readData(String path, T defaultValue) {
        if (this.exists(path)) {
            return super.readData(path);
        } else {
            return defaultValue;
        }
    }

    /**
     * 向节点中写数据，当znode不存在时创建，如果存在则等同于{@link ZkClient#writeData(String, Object)}
     */
    public void writeData(final String path, Object data, final CreateMode mode) {
        try {
            super.writeData(path, data);
        } catch (ZkNoNodeException e) {
            this.create(path, mode, true);
            super.writeData(path, data);
        }
    }

    /**
     * 判断有没有子节点
     *
     * @param path
     * @return
     */
    public boolean hasChildren(String path){
        List<String> list = super.getChildren(path);
        if(list!=null && list.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 获取孩子节点的完整路径
     *
     * @param path
     * @return
     */
    public List<String> getChildrenOfFullPath(String path) {
        List<String> list = super.getChildren(path);
        if (list == null)
            return new ArrayList<String>();

        for (int i = 0; i < list.size(); i++) {
            String _path = path + "/" + list.get(i);
            _path = _path.replaceFirst("\\/\\/", "\\/");
            list.set(i, _path);
        }

        return list;
    }

    /**
     * 递归获取孩子节点的完整路径
     *
     * @param path
     * @return
     */
    public List<String> getChildrenOfFullPathRecursive(String path) {
        List<String> list = this.getChildrenOfFullPath(path);
        if (list.size() > 0) {
            List<String> children = new ArrayList<String>();
            for (String child : list) {
                children.addAll(getChildrenOfFullPathRecursive(child));
            }
            list.addAll(children);
        }
        return list;
    }

}
