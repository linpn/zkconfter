package com.github.linpn.zkconfter.annotation;

import java.lang.annotation.*;

/**
 * 标注了此注解的方法，将在资源属性更新后被调用。供使用者加入自定义的通用处理逻辑。
 * 此注解是可选的。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DRMAfterEvent {

}
