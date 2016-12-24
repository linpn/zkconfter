package com.github.zkconfter.ops.web;

import com.github.jsplite.http.JspliteHttpRequest;
import com.github.jsplite.http.JspliteHttpResponse;
import com.github.jsplite.mvc.Controller;
import com.github.jsplite.mvc.ModelView;
import com.github.zkconfter.ZkConfter;
import com.github.zkconfter.util.ZkClient;

import java.util.List;

public class Index implements Controller {

    private ZkConfter zkConfter = new ZkConfter("zkconfter.properties");

    @Override
    public void execute(JspliteHttpRequest request, JspliteHttpResponse response,
                        ModelView model) throws Exception {

        ZkClient zkClient = zkConfter.getZkClient();

        StringBuilder sb = new StringBuilder();
        List<String> list = zkClient.getChildrenOfFullPathRecursive("/");
        for (String s : list) {
            sb.append(s + "<br>");
        }

        model.addObject("hello", sb.toString());
    }

}
