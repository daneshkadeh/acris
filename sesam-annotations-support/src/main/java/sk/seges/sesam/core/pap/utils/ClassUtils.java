package sk.seges.sesam.core.pap.utils;

import java.lang.reflect.Type;

import sk.seges.sesam.core.pap.builder.api.NameTypes.ClassSerializer;
import sk.seges.sesam.core.pap.model.InputClass.HasTypeParameters;
import sk.seges.sesam.core.pap.model.api.NamedType;

public class ClassUtils {

	public static String toString(Class<?> clazz, ClassSerializer serializer) {
		switch (serializer) {
		case CANONICAL:
			return clazz.getCanonicalName();
		case QUALIFIED:
			return clazz.getName();
		case SIMPLE:
			return clazz.getSimpleName();
		}
		return null;
	}
	
	public static String toString(NamedType inputClass, Type type, ClassSerializer serializer, boolean typed) {
		if (type instanceof Class) {
			return toString((Class<?>)type, serializer);
		}
		
		if (type instanceof HasTypeParameters) {
			return ((HasTypeParameters)type).toString(inputClass, serializer, typed);
		}
		
		if (type instanceof NamedType) {
			return ((NamedType)type).toString(serializer);
		}
		
		throw new IllegalArgumentException("Not supported annotation element " + type.toString());
	}
}