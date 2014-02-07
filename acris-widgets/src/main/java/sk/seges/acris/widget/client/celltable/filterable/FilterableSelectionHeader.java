package sk.seges.acris.widget.client.celltable.filterable;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.cellview.client.Header;
import sk.seges.acris.widget.client.celltable.AbstractFilterableTable.Validator;
import sk.seges.sesam.shared.model.dto.SimpleExpressionDTO;

import java.util.List;

public class FilterableSelectionHeader extends Header<SimpleExpressionDTO> {

	private SimpleExpressionDTO criteria;
	
	public FilterableSelectionHeader(ValueUpdater<SimpleExpressionDTO> valueUpdate, SimpleExpressionDTO criteria, Validator validator, List<String> options, String text) {
		super(new SelectionFilterColumn(validator, options, text));
		setUpdater(valueUpdate);
		this.criteria = criteria;
	}
	
	@Override
	public SimpleExpressionDTO getValue() {
		return criteria;
	}

}