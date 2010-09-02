/**
 * 
 */
package sk.seges.acris.binding.client.annotations;

import sk.seges.acris.binding.client.processor.IBeanPropertyConverter;
import sk.seges.acris.binding.client.processor.POJOPropertyConverter;

/**
 * @author eldzi
 */
public @interface BeanWrapper {

	//Do not rename the name, see BeanWrapperProcessor for the reference of that
	Class<? extends IBeanPropertyConverter>[] beanPropertyConverter() default {POJOPropertyConverter.class};
}
