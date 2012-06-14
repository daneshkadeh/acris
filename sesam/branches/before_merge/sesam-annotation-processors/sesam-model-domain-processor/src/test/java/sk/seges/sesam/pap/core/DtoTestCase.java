package sk.seges.sesam.pap.core;

import java.io.File;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.test.AnnotationTest;
import sk.seges.sesam.pap.model.model.ConfigurationTypeElement;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.model.api.dto.DtoDeclaredType;

public abstract class DtoTestCase extends AnnotationTest {

	protected File getOutputFile(Class<?> clazz) {
		MutableDeclaredType inputClass = toMutable(clazz);
		
		TransferObjectProcessingEnvironment tope = new TransferObjectProcessingEnvironment(processingEnv, roundEnv);
		DtoDeclaredType dto = new ConfigurationTypeElement(processingEnv.getElementUtils().getTypeElement(inputClass.getCanonicalName()), tope, roundEnv).getDto();

		return new File(OUTPUT_DIRECTORY, toPath(dto.getPackageName()) + "/" + dto.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

}