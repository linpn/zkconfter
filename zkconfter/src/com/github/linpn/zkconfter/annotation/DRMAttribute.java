package com.github.linpn.zkconfter.annotation;


import java.lang.annotation.*;

/**
 * 分布式资源管理的（DRM）中描述资源属性的注解。
 * 该注解可以标注在属性的Setter和Getter方法上，属性具有读/写特性；如果该注解标识在Setter方法上标识该属性为可写
 * 如果该注解标识在Getter方法上标识该属性可读。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DRMAttribute {
    /**
     * 属性名称
     *
     * @return 返回属性名称
     */
    String name() default "";

    /**
     * 属性描述
     *
     * @return 返回属性描述
     */
    String description() default "";
}
