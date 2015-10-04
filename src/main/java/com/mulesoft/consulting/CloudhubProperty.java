package com.mulesoft.consulting;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by adamdavis on 04/10/2015.
 */
public class CloudhubProperty {


    @DataBoundConstructor
    public CloudhubProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private String key ;

    private String value ;

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
