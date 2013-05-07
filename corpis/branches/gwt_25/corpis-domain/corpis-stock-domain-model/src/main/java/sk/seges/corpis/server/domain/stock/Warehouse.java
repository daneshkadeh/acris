package sk.seges.corpis.server.domain.stock;

import java.util.List;

import sk.seges.corpis.appscaffold.shared.annotation.BaseObject;
import sk.seges.corpis.appscaffold.shared.annotation.DomainInterface;
import sk.seges.sesam.domain.IMutableDomainObject;

@DomainInterface
@BaseObject
public interface Warehouse extends IMutableDomainObject<Integer> {

	String name();

	String address();

	String email();

	String phone();

	String fax();

	Boolean active();

	String town();

	String psc();

	String contactPerson();

	List<StockItem> stockItems();

}