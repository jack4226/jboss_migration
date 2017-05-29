package ltj.msgui.model;

import java.util.Arrays;
import java.util.Comparator;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelListener;

import ltj.msgui.Name;

public class SortFilterModel extends DataModel {
	private DataModel model;
	private Row[] rows;

	private static Comparator<Row> byLast = new Comparator<Row>() {
		public int compare(Row r1, Row r2) {
			Name n1 = (Name) r1.getData();
			Name n2 = (Name) r2.getData();
			return n1.getLast().compareTo(n2.getLast());
		}
	};

	private static Comparator<Row> byFirst = new Comparator<Row>() {
		public int compare(Row r1, Row r2) {
			Name n1 = (Name) r1.getData();
			Name n2 = (Name) r2.getData();
			return n1.getFirst().compareTo(n2.getFirst());
		}
	};

	private class Row {
		private int row;

		public Row(int row) {
			this.row = row;
		}

		public Object getData() {
			int originalIndex = model.getRowIndex();
			model.setRowIndex(row);
			Object thisRowData = model.getRowData();
			model.setRowIndex(originalIndex);
			return thisRowData;
		}
	}

	public SortFilterModel() { // mandated by JSF spec
		this((Name[]) null);
	}

	public SortFilterModel(Name[] names) { // recommended by JSF spec
		setWrappedData(names);
	}

	public SortFilterModel(DataModel model) {
		this.model = model;
		initializeRows();
	}

	public String sortByLast() {
		Arrays.sort(rows, byLast);
		return null;
	}

	public String sortByFirst() {
		Arrays.sort(rows, byFirst);
		return null;
	}

	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || rowIndex >= model.getRowCount()) {
			model.setRowIndex(rowIndex);
		} else {
			model.setRowIndex(rows[rowIndex].row);
		}
	}

	// The following methods delegate directly to the
	// decorated model

	public boolean isRowAvailable() {
		return model.isRowAvailable();
	}

	public int getRowCount() {
		return model.getRowCount();
	}

	public Object getRowData() {
		return model.getRowData();
	}

	public int getRowIndex() {
		return model.getRowIndex();
	}

	public Object getWrappedData() {
		return model.getWrappedData();
	}

	public void setWrappedData(Object data) {
		model.setWrappedData(data);
		initializeRows();
	}

	public void addDataModelListener(DataModelListener listener) {
		model.addDataModelListener(listener);
	}

	public DataModelListener[] getDataModelListeners() {
		return model.getDataModelListeners();
	}

	public void removeDataModelListener(DataModelListener listener) {
		model.removeDataModelListener(listener);
	}

	private void initializeRows() {
		int rowCnt = model.getRowCount();
		if (rowCnt != -1) {
			rows = new Row[rowCnt];
			for (int i = 0; i < rowCnt; ++i) {
				rows[i] = new Row(i);
			}
		}
	}
}
