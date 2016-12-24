package com.github.zkconfter.annotation;

import java.lang.annotation.*;

/**
 * 分布式资源管理的（DRM）中描述管理资源的注解。
 * <p/>
 * Created by pinian.lpn on 2015/1/15.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DRMResource {
    /**
     * 资源名称
     */
    String name() default "";

    /**
     * 资源描述
     */
    String description() default "";

}