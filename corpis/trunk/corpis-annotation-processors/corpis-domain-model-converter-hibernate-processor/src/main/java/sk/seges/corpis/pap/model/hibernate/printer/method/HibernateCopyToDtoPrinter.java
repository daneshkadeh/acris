package sk.seges.corpis.pap.model.hibernate.printer.method;

import java.io.PrintWriter;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;

import org.hibernate.Hibernate;

import sk.seges.corpis.pap.model.hibernate.resolver.HibernateEntityResolver;
import sk.seges.corpis.shared.converter.utils.ConverterUtils;
import sk.seges.sesam.core.pap.model.PathResolver;
import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.context.api.TransferObjectContext;
import sk.seges.sesam.pap.model.hibernate.resolver.HibernateParameterResolver;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.ElementHolderTypeConverter;
import sk.seges.sesam.pap.model.printer.converter.ConverterProviderPrinter;
import sk.seges.sesam.pap.model.printer.method.CopyToDtoMethodPrinter;
import sk.seges.sesam.pap.model.printer.method.CopyToDtoPrinter;
import sk.seges.sesam.pap.model.resolver.api.EntityResolver;
import sk.seges.sesam.pap.model.resolver.api.ParametersResolver;

public class HibernateCopyToDtoPrinter extends CopyToDtoPrinter {

	private final HibernateEntityResolver entityResolver;
	
	public HibernateCopyToDtoPrinter(ConverterProviderPrinter converterProviderPrinter, ElementHolderTypeConverter elementHolderTypeConverter,
			HibernateEntityResolver entityResolver, ParametersResolver parametersResolver, RoundEnvironment roundEnv,
			TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
		super(converterProviderPrinter, elementHolderTypeConverter, entityResolver, parametersResolver, roundEnv, processingEnv, pw);
		this.entityResolver = entityResolver;
	}

	@Override
	public void print(TransferObjectContext context) {
		copy(context, pw, new HibernateCopyToDtoMethodPrinter(converterProviderPrinter, elementHolderTypeConverter, entityResolver, parametersResolver, roundEnv, processingEnv));
	}
	
}
