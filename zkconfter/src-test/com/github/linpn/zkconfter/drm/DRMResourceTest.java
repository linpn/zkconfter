package com.github.linpn.zkconfter.drm;

import com.github.linpn.zkconfter.annotation.DRMResource;
import com.github.linpn.zkconfter.annotation.DRMAttribute;

/**
 * Created by pinian.lpn on 2015/8/10.
 */
@DRMResource
public class DRMResourceTest {

    @DRMAttribute
    private String testString = "10\n20";

    @DRMAttribute
    private int testInt = 20;

    @DRMAttribute
    private boolean testBoolean = true;

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public int getTestInt() {
        return testInt;
    }

    public void setTestInt(int testInt) {
        this.testInt = testInt;
    }

    public boolean isTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(boolean testBoolean) {
        this.testBoolean = testBoolean;
    }
}
