package sk.seges.sesam.pap.test;

import java.io.File;

import javax.annotation.processing.Processor;

import org.junit.Test;

import sk.seges.sesam.core.pap.AnnotationTest;
import sk.seges.sesam.core.pap.model.OutputClass;
import sk.seges.sesam.core.pap.model.api.NamedType;
import sk.seges.sesam.pap.model.DomainObject;
import sk.seges.sesam.pap.model.MockEntityDtoConfiguration;
import sk.seges.sesam.pap.model.SecondDomainObject;
import sk.seges.sesam.pap.model.SecondDomainObjectDtoConfiguration;
import sk.seges.sesam.pap.model.TransferObjectConvertorProcessor;
import sk.seges.sesam.pap.model.TransferObjectProcessor;
import sk.seges.sesam.pap.model.annotation.Field;


public class TransferObjectProcessorTest extends AnnotationTest {

	@Test
	public void testNestedBounds() {
		assertCompilationSuccessful(compileFiles(MockEntityDtoConfiguration.class, SecondDomainObjectDtoConfiguration.class, DomainObject.class, SecondDomainObject.class));
	//	assertOutput(getResourceFile(TestBeanNestedBounds.class), getOutputFile(TestBeanNestedBounds.class));
	}

	@Override
	protected Processor[] getProcessors() {
		return new Processor[] {
			new TransferObjectProcessor(),
			new TransferObjectConvertorProcessor()
		};
	}

	private File getOutputFile(Class<?> clazz) {
		OutputClass inputClass = new OutputClass(clazz.getPackage().getName(), clazz.getSimpleName());
		NamedType outputClass = TransferObjectProcessor.getOutputClass(inputClass);
		return new File(OUTPUT_DIRECTORY, toPath(outputClass.getPackageName()) + "/" + outputClass.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

}
