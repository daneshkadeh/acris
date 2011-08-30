package sk.seges.acris.domain.shared.domain.api;

import java.util.Date;
import java.util.List;

import sk.seges.corpis.shared.domain.api.HasWebId;
import sk.seges.sesam.domain.IMutableDomainObject;

public interface ContentData<T> extends IMutableDomainObject<T>, HasWebId {

	String getTitle();

	void setTitle(String title);

	String getKeywords();

	void setKeywords(String keywords);

	String getDescription();

	void setDescription(String description);

	String getNiceUrl();

	void setNiceUrl(String niceUrl);

	String getPageName();

	void setPageName(String pageName);

	String getToken();

	void setToken(String token);

	String getContentDetached();

	void setContentDetached(String contentDetached);

	List<ContentData<T>> getSubContents();

	void setSubContents(List<ContentData<T>> subContents);

	void addSubContent(ContentData<T> subContent);

	ContentData<T> getParent();

	void setParent(ContentData<T> parent);

	String getLabel();

	void setLabel(String label);

	Integer getIndex();

	void setIndex(Integer index);

	String getRef();

	void setRef(String ref);

	String getStylePrefix();

	void setStylePrefix(String stylePrefix);

	Date getCreated();

	void setCreated(Date created);

	Date getModified();

	void setModified(Date modified);

	String getPosition();

	void setPosition(String position);

	String getGroup();

	void setGroup(String group);

	Boolean getDefaultlyLoaded();

	void setDefaultlyLoaded(Boolean defaultlyLoaded);

	String getDefaultStyleClass();

	void setDefaultStyleClass(String defaultStyleClass);

	String getMenuItems();

	void setMenuItems(String menuItems);

	String getDecoration();

	void setDecoration(String decoration);

	Boolean getHasChildren();

	void setHasChildren(Boolean hasChildren);

	Integer getVersion();

	void setVersion(Integer version);

	String getParams();

	void setParams(String params);
}
