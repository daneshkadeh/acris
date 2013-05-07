package sk.seges.acris.widget.client.celltable;

import java.util.Map;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.view.client.ProvidesKey;

public class DynamicCellTable extends AbstractFilterableTable<Map<String, Object>> {

	public static String STRING = "STRING";
	public static String DATE = "DATE";
	public static String NUMBER = "NUMBER";
	
	static class DynamicCellTableKeyProvider implements ProvidesKey<Map<String, Object>> {
		@Override
		public Object getKey(Map<String, Object> item) {
			return item.get("id");
		}
	}
	
	public void init() {
		initialize();
	}

	public DynamicCellTable() {
		super(new DynamicCellTableKeyProvider(), Map.class);
	}

	public void setColumns(Map<String, String[]> columns) {
		int colCount = getColumnCount();
		for (int i = 0; i < colCount; i++) {
			removeColumn(0);
		}
		for (final String column : columns.keySet()) {
			if (columns.get(column) == null) {
				continue;
			}
			if (columns.get(column)[0].toUpperCase().equals(STRING)) {
				Column<Map<String, Object>, String> col = new Column<Map<String, Object>, String>(new TextCell()) {
					@Override
					public String getValue(Map<String, Object> arg0) {
						return (String) arg0.get(column);
					}
				};
				addTextColumn(col, 90 / (columns.size()), columns.get(column)[1], column);
			} else if (columns.get(column)[0].toUpperCase().equals(DATE)) {

			} else if (columns.get(column)[0].toUpperCase().equals(NUMBER)) {

			}
		}
		addCheckboxColumn(50);

	}

}