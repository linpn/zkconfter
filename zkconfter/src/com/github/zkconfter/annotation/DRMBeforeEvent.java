package com.github.zkconfter.annotation;

import java.lang.annotation.*;

/**
 * 标注了此注解的方法，将在资源属性更新前被调用。供使用者加入自定义的通用处理逻辑。
 * 此注解是可选的。
 *
 * @author hui.shih
 * @version $Id: BeforeUpdate.java, v 0.1 2012-5-11 下午02:50:58 hui.shih Exp $
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DRMBeforeEvent {

}
