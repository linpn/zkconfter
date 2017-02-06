package com.github.linpn.zkconfter.ops.web;

import com.github.linpn.jsplite.http.JspliteHttpRequest;
import com.github.linpn.jsplite.http.JspliteHttpResponse;
import com.github.linpn.jsplite.mvc.Controller;
import com.github.linpn.jsplite.mvc.ModelView;
import com.github.linpn.zkconfter.ZkConfter;
import com.github.linpn.zkconfter.util.ZkClient;

import java.util.List;

public class Index implements Controller {

    private ZkConfter zkConfter = new ZkConfter("zkconfter.properties");

    @Override
    public void execute(JspliteHttpRequest request, JspliteHttpResponse response,
                        ModelView model) {

        ZkClient zkClient = zkConfter.getZkClient();

        StringBuilder sb = new StringBuilder();
        List<String> list = zkClient.getChildrenOfFullPathRecursive("/");
        for (String s : list) {
            sb.append(s + "<br>");
        }

        model.addObject("hello", sb.toString());
    }

}
