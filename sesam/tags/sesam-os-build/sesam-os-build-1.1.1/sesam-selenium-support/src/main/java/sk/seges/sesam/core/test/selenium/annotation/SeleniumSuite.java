package sk.seges.sesam.core.test.selenium.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
//This is not working in the eclipse with the source retention policy
//@Retention(RetentionPolicy.SOURCE)
public @interface SeleniumSuite {

}