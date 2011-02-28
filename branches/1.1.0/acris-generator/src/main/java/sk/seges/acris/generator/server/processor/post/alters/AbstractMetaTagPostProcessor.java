package sk.seges.acris.generator.server.processor.post.alters;

import org.htmlparser.Node;
import org.htmlparser.tags.MetaTag;

import sk.seges.acris.generator.server.processor.ContentDataProvider;
import sk.seges.acris.site.shared.service.IWebSettingsService;

public abstract class AbstractMetaTagPostProcessor extends AbstractContentMetaDataPostProcessor {
	
	protected AbstractMetaTagPostProcessor(IWebSettingsService webSettingsService, ContentDataProvider contentInfoProvider) {
		super(webSettingsService, contentInfoProvider);
	}

	public static final String NAME_ATTRIBUTE_NAME = "name";

	protected abstract String getMetaTagName();
	protected abstract String getMetaTagContent();
	
	@Override
	public boolean process(Node node) {
		MetaTag metaTag = (MetaTag)node;
		String content = getMetaTagContent();
		metaTag.setMetaTagContents(content == null ? "" : content);
		return true;
	}

	@Override
	public boolean supports(Node node) {
		if  (!(node instanceof MetaTag)) {
			return false;
		}
		
		MetaTag metaTag = (MetaTag)node;
		String name = metaTag.getAttribute(NAME_ATTRIBUTE_NAME);
		
		if (name == null) {
			return false;
		}
		
		return (name.toLowerCase().equals(getMetaTagName().toLowerCase()));
	}
}