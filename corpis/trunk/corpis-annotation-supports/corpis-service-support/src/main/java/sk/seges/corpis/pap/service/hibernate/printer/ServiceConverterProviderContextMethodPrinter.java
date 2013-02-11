package sk.seges.corpis.pap.service.hibernate.printer;

import javax.lang.model.element.Modifier;

import sk.seges.corpis.pap.model.printer.converter.HibernateServiceConverterProviderParameterResolver;
import sk.seges.sesam.core.pap.builder.api.ClassPathTypes;
import sk.seges.sesam.core.pap.model.ParameterElement;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider;
import sk.seges.sesam.pap.model.resolver.ConverterConstructorParametersResolverProvider.UsageType;
import sk.seges.sesam.pap.service.model.ConverterProviderContextType;
import sk.seges.sesam.pap.service.model.ServiceTypeElement;
import sk.seges.sesam.pap.service.printer.converterprovider.ServiceConverterProviderContextPrinter;

public class ServiceConverterProviderContextMethodPrinter extends ServiceConverterProviderContextPrinter {

	public ServiceConverterProviderContextMethodPrinter(TransferObjectProcessingEnvironment processingEnv,
			ConverterConstructorParametersResolverProvider parametersResolverProvider, FormattedPrintWriter pw,
			ConverterProviderPrinter converterProviderPrinter, ClassPathTypes classPathTypes) {
		super(processingEnv, parametersResolverProvider, pw, converterProviderPrinter, classPathTypes);
	}
	
	@Override
	protected void initialize(ServiceTypeElement serviceTypeElement) {

		UsageType previousUsageType = converterProviderPrinter.changeUsage(UsageType.CONVERTER_PROVIDER_CONTEXT_CONSTRUCTOR);

		ConverterProviderContextType convertProviderContextType = new ConverterProviderContextType(serviceTypeElement, processingEnv);
		ParameterElement[] generatedParameters = convertProviderContextType.getRequiredParameters(
				parametersResolverProvider.getParameterResolver(UsageType.CONVERTER_PROVIDER_CONTEXT_CONSTRUCTOR),
				parametersResolverProvider.getParameterResolver(UsageType.DEFINITION));
		
		ParameterElement[] requiredParameters = 
				convertProviderContextType.getConverterParameters(parametersResolverProvider.getParameterResolver(UsageType.CONVERTER_PROVIDER_CONTEXT_CONSTRUCTOR));
		
		pw.print(Modifier.PROTECTED.name().toLowerCase() + " " + convertProviderContextType.getSimpleName() +  " " + HibernateServiceConverterProviderParameterResolver.GET_CONVERTER_PROVIDER_CONTEXT_METHOD + "(");

		int i = 0;
		for (ParameterElement generatedParameter: generatedParameters) {
			if (i > 0) {
				pw.print(", ");
			}
			
			pw.print(generatedParameter.getType(), " " + generatedParameter.getName());
			i++;
		}

		pw.println(") {");
		pw.print("return new " + convertProviderContextType.getSimpleName() + "(");
		i = 0;
		for (ParameterElement parameter: requiredParameters) {
			if (i > 0) {
				pw.print(", ");
			}

			pw.print(parameter.getName());
			i++;
		}
		pw.println(");");
		pw.println("}");
		pw.println();
		
		converterProviderPrinter.changeUsage(previousUsageType);
		
		super.initialize(serviceTypeElement);
	}
}