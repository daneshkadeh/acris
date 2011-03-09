package sk.seges.acris.generator.server.processor.post.annihilators;

import org.htmlparser.Node;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.StyleTag;

import sk.seges.acris.generator.server.processor.model.api.GeneratorEnvironment;

public class HeadStyleScriptPostProcessor extends AbstractPostProcessorAnnihilator {

	@Override
	protected boolean supportsParent(Node node, GeneratorEnvironment generatorEnvironment) {
		return (node instanceof HeadTag);	
	}

	@Override
	protected boolean supportsNode(Node node, GeneratorEnvironment generatorEnvironment) {
		return (node instanceof StyleTag);
	}
}