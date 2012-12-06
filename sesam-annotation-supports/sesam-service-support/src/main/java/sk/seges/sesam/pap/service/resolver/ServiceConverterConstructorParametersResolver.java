package sk.seges.sesam.pap.service.resolver;

import sk.seges.sesam.core.pap.model.mutable.api.MutableReferenceType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.pap.model.resolver.DefaultConverterConstructorParametersResolver;

public class ServiceConverterConstructorParametersResolver extends DefaultConverterConstructorParametersResolver {

	public ServiceConverterConstructorParametersResolver(MutableProcessingEnvironment processingEnv) {
		super(processingEnv);
	}

	protected boolean isConverterProviderParameterPropagated() {
		return false;
	}

	@Override
	protected boolean isConverterCacheParameterPropagated() {
		return false;
	}
	
	@Override
	protected MutableReferenceType getConverterProviderReference() {
		return null;
	}
}