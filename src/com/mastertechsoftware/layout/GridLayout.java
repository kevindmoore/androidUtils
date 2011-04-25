package com.mastertechsoftware.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import com.mastertechsoftware.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class GridLayout extends ViewGroup {
	public static final int GridLayout_columnEnd = 6;
	public static final int GridLayout_columnStart = 4;
	public static final int GridLayout_numColumns = 1;
	public static final int GridLayout_numRows = 0;
	public static final int GridLayout_preferredCellHeight = 3;
	public static final int GridLayout_preferredCellWidth = 2;
	public static final int GridLayout_rowEnd = 7;
	public static final int GridLayout_rowStart = 5;
	private int layoutLeft;
	private int layoutTop;
	private int layoutRight;
	private int layoutBottom;
	private boolean debugging = false;
	private OnClickListener onClickListener;
//    private Paint innerPaint, borderPaint;


	public static class LayoutParams extends
			android.widget.LinearLayout.LayoutParams {

		public static LayoutParams createLayout(int row, int column) {
			return new LayoutParams(row, row + 1, column, column + 1);
		}

		public static LayoutParams createLayout(int widthAttr, int heightAttr, int row, int column) {
			return new LayoutParams(widthAttr, heightAttr, row, row + 1, column, column + 1);
		}

		public static LayoutParams createLayout(int row, int column, int columSpan) {
			return new LayoutParams(row, row + 1, column, column + columSpan);
		}

		public static LayoutParams createLayout(int widthAttr, int heightAttr, int row, int column, int columSpan) {
			return new LayoutParams(widthAttr, heightAttr, row, row + 1, column, column + columSpan);
		}

		public String debug(String output) {
			return (new StringBuilder()).append(output).append(
					"LayoutParams.LayoutParams={columnStart=").append(
					columnStart).append(", rowStart=").append(rowStart).append(
					", columnEnd=").append(columnEnd).append(", rowEnd=")
					.append(rowEnd).toString();
		}

		public int columnStart, columnEnd;
		public int rowStart, rowEnd;

		public LayoutParams(int widthAttr, int heightAttr, int rowStart, int rowEnd, int columnStart, int columnEnd) {
			super(widthAttr, heightAttr);
			this.columnStart = columnStart;
			this.rowStart = rowStart;
			this.columnEnd = columnEnd;
			this.rowEnd = rowEnd;
		}

		public LayoutParams(int rowStart, int rowEnd, int columnStart, int columnEnd) {
			this(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, rowStart, rowEnd, columnStart, columnEnd);
		}

		public LayoutParams(Context c, AttributeSet attrs) {
			super(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//            TypedArray a = c
//                    .obtainStyledAttributes(attrs,
//                            R.styleable.GridLayout);
//            this.columnStart = a.getInt(
//                    R.styleable.GridLayout_columnStart, -1);
//            this.rowStart = a.getInt(
//                    R.styleable.GridLayout_rowStart, -1);
//            this.columnEnd = a.getInt(
//                    R.styleable.GridLayout_columnEnd,
//                    this.columnStart + 1);
//            this.rowEnd = a.getInt(
//                    R.styleable.GridLayout_rowEnd,
//                    this.rowStart + 1);
//            a.recycle();
			this.columnStart = attrs.getAttributeIntValue(GridLayout_columnStart, -1);
			this.rowStart = attrs.getAttributeIntValue(GridLayout_rowStart, -1);
			this.columnEnd = attrs.getAttributeIntValue(GridLayout_columnEnd,
					this.columnStart + 1);
			this.rowEnd = attrs.getAttributeIntValue(GridLayout_rowEnd,
					this.rowStart + 1);
		}

		public int getColumnLength() {
			return columnEnd - columnStart;
		}

		public int getRowLength() {
			return rowEnd - rowStart;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getColumnEnd() {
			return columnEnd;
		}

		public void setColumnEnd(int columnEnd) {
			this.columnEnd = columnEnd;
		}

		public int getColumnStart() {
			return columnStart;
		}

		public void setColumnStart(int columnStart) {
			this.columnStart = columnStart;
		}

		public int getRowEnd() {
			return rowEnd;
		}

		public void setRowEnd(int rowEnd) {
			this.rowEnd = rowEnd;
		}

		public int getRowStart() {
			return rowStart;
		}

		public void setRowStart(int rowStart) {
			this.rowStart = rowStart;
		}
	}


	private int measuredWidth = -1;
	private int measuredHeight = -1;
	private int layoutWidth = -1;
	private int layoutHeight = -1;
	private int numRows = -1;
	private int rowCount;
	private int columnCount;
	private int numColumns = -1;
	private float preferredCellWidth;
	private float preferredCellHeight;
	protected ArrayList<Row> rows = new ArrayList<Row>();
	protected ArrayList<MeasureListener> measureListeners = new ArrayList<MeasureListener>();
	protected ArrayList<LayoutListener> layoutListeners = new ArrayList<LayoutListener>();

	public GridLayout(Context context) {
		super(context);
		this.numRows = 1;
		this.numColumns = 1;
		this.preferredCellWidth = 0;
		this.preferredCellHeight = 0;
	}

	public GridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		readAttr(context, attrs);
	}

	public GridLayout(Context context, AttributeSet attrs,
					  int defStyle) {
		super(context, attrs, defStyle);
		readAttr(context, attrs);
	}

	private void readAttr(Context c, AttributeSet attrs) {
		this.numRows = attrs.getAttributeIntValue(GridLayout_numRows, 0);
		this.numColumns = attrs.getAttributeIntValue(GridLayout_numColumns, 0);
		this.preferredCellWidth = attrs.getAttributeIntValue(GridLayout_preferredCellWidth, 0);
		this.preferredCellHeight = attrs.getAttributeIntValue(GridLayout_preferredCellHeight, 0);
//        TypedArray a = c
//                .obtainStyledAttributes(attrs, R.styleable.GridLayout);
//        int n = a.getIndexCount();
//
//		attrs.
//        for (int i = 0; i < n; i++)
//
//        {
//            int attr = a.getIndex(i);
//            switch (attr) {
//                case R.styleable.GridLayout_numRows:
//                    this.numRows = a.getInt(R.styleable.GridLayout_numRows, -1);
//                    break;
//                case R.styleable.GridLayout_numColumns:
//                    this.numColumns = a.getInt(R.styleable.GridLayout_numColumns, -1);
//                    break;
//                case R.styleable.GridLayout_preferredCellWidth:
//                    this.preferredCellWidth = a.getDimension(
//                            R.styleable.GridLayout_preferredCellWidth, 0);
//                    break;
//                case R.styleable.GridLayout_preferredCellHeight:
//                    this.preferredCellHeight = a.getDimension(
//                            R.styleable.GridLayout_preferredCellHeight, 0);
//                    break;
//            }
//        }
//        a.recycle();
	}

	public void addMeasureListener(MeasureListener listener) {
		measureListeners.add(listener);
	}

	public void removeMeasureListener(MeasureListener listener) {
		measureListeners.remove(listener);
	}

	protected void fireMeasureListeners() {
		for (MeasureListener measureListener : measureListeners) {
			measureListener.measureFinished(this);
		}
	}

	public void addLayoutListener(LayoutListener listener) {
		layoutListeners.add(listener);
	}

	public void removeLayoutListener(LayoutListener listener) {
		layoutListeners.remove(listener);
	}

	protected void fireLayoutListeners() {
		for (LayoutListener layoutListener : layoutListeners) {
			layoutListener.layoutFinished(this);
		}
	}


	public int getNumRows() {
		if (numRows == -1) {
			return rows.size();
		}
		return numRows;
	}

	public int getNumColumns() {
		if (numColumns == -1) {
			if (rows.size() == 0) {
				return 0;
			}
			return rows.get(0).getColumns().size();
		}
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public void setPreferredCellWidth(float preferredCellWidth) {
		this.preferredCellWidth = preferredCellWidth;
	}

	public void setPreferredCellHeight(float preferredCellHeight) {
		this.preferredCellHeight = preferredCellHeight;
	}

	public void setDebugging(boolean debugging) {
		this.debugging = debugging;
		for (Row row : rows) {
			for (Column column : row.columns) {
				View child = column.getView();
				if (child == null) {
					continue;
				}
				if (child instanceof GridLayout) {
					((GridLayout) child).setDebugging(debugging);
				}
			}
		}
	}

	public float preferredCellWidth() {
		return preferredCellWidth;
	}

	public float preferredCellHeight() {
		return preferredCellHeight;
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		Row row = getRow(params);
		if (row != null) {
			Column column = getColumn(row, params);
			if (column != null) {
				column.setView(child);
			}
		}
	}

	public void insertView(View child, int index, GridLayout.LayoutParams params) {
		super.addView(child, index, params);
		Row row = new Row();
		row.setRowStart(params.rowStart);
		row.setExpandable(params.height == LayoutParams.FILL_PARENT);
		rows.ensureCapacity(rows.size() + 1);
		rows.add(index, row);
		if (row != null) {
			Column column = getColumn(row, params);
			if (column != null) {
				column.setView(child);
			}
		}
	}

	public boolean replaceView(View existing, View replacing) {
		for (Row row : rows) {
			for (Column column : row.columns) {
				if (column.getView() == existing) {
					LayoutParams params = (LayoutParams) existing.getLayoutParams();
					ViewGroup parent = (ViewGroup) existing.getParent();
					parent.removeView(existing);
					parent.addView(replacing, params);
					return true;
				} else if (column.getView() instanceof GridLayout) {
					if (((GridLayout) column.getView()).replaceView(existing, replacing)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void setRowHeight(int row, int height) {
		if (rows.size() > row) {
			rows.get(row).setFixedHeight(height);
		} else {
			Logger.error("GridLayout", "Row " + row + " does not exist", new Exception("Invalid Row"));
		}
	}

	public void setColumnWidth(int row, int column, int width) {
		if (rows.size() > row) {
			Row gridRow = rows.get(row);
			if (gridRow.columns.size() > column) {
				gridRow.columns.get(column).setFixedWidth(width);
			} else {
				Logger.error("Column " + column + " does not exist");
			}
		} else {
			Logger.error("Row " + row + " does not exist");
		}
	}

	protected Row getRow(ViewGroup.LayoutParams params) {
		if (params != null && (params instanceof GridLayout.LayoutParams) && ((GridLayout.LayoutParams) params).rowStart != -1) {
			GridLayout.LayoutParams gridParams = (GridLayout.LayoutParams) params;
			Row row = null;
			if (rows.size() <= gridParams.rowStart) {
				row = new Row();
				row.setRowStart(gridParams.rowStart);
				row.setExpandable(gridParams.height == LayoutParams.FILL_PARENT);
				rows.add(row);
				return row;
			}
			return rows.get(gridParams.rowStart);
		} else {
			Row row = null;
			if (rows.size() <= rowCount) {
				row = new Row();
				row.setRowStart(rowCount);
				rows.add(row);
				return row;
			}
			return rows.get(rowCount);
		}
	}

	protected Column getColumn(Row row, ViewGroup.LayoutParams params) {
		if (params != null && (params instanceof GridLayout.LayoutParams) && ((GridLayout.LayoutParams) params).columnStart != -1) {
			GridLayout.LayoutParams gridParams = (GridLayout.LayoutParams) params;
			Column column = null;
			if (row.getColumns().size() <= gridParams.columnStart) {
				column = new Column();
				column.setColumn(gridParams.columnStart);
				column.setExpandable(gridParams.width == LayoutParams.FILL_PARENT);
				row.addColumn(column);
				return column;
			}
			return row.getColumn(gridParams.columnStart);
		} else {
			Column column = null;
			if (row.getColumns().size() <= columnCount) {
				column = new Column();
				column.setColumn(columnCount++);
				if (columnCount > numColumns) {
					columnCount = 0;
					rowCount++;
				}
				row.addColumn(column);
				return column;
			}
			return row.getColumn(columnCount);
		}
	}

	public View getView(int rowIndex, int columnIndex) {
		if (rows.size() > rowIndex) {
			Row row = rows.get(rowIndex);
			if (row.getColumns().size() > columnIndex) {
				return row.getColumn(columnIndex).getView();
			}
		}
		return null;
	}

	public int findViewRow(Class classType) {
		int rowIndex = 0;
		for (Row row : rows) {
			for (Column column : row.getColumns()) {
				for (View view : column.getViews()) {
					if (view != null && classType.equals(view.getClass())) {
						return rowIndex;
					}
					if (view instanceof GridLayout) {
						int found = ((GridLayout) view).findViewRow(classType);
						if (found != -1) {
							return found;
						}
					}
				}
			}
			rowIndex++;
		}
		return -1;
	}

	public void setRowExpandable(int rowIndex, boolean expandable) {
		if (rows.size() > rowIndex) {
			Row row = rows.get(rowIndex);
			row.setExpandable(expandable);
		}
	}

	public void setColumnExpandable(int rowIndex, int columnIndex, boolean expandable) {
		if (rows.size() > rowIndex) {
			Row row = rows.get(rowIndex);
			if (row.getColumns().size() > columnIndex) {
				row.getColumn(columnIndex).setExpandable(expandable);
			}
		}
	}

/*
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				Rect hitRect = new Rect();
				for (Row row : rows) {
					boolean visibleRow = rowIsVisible(row);
					if (!visibleRow) {
						continue;
					}
					for (Column column : row.columns) {
						View child = column.getView();
						if (child == null) {
							continue;
						}
						if (child.getVisibility() != GONE) {
							child.getGlobalVisibleRect(hitRect);
							if (hitRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
								if (child instanceof ViewGroup) {
									View focus = ((ViewGroup)child).findFocus();
									if (focus != null) {

									}
								}
								child.dispatchTouchEvent(ev);
								return true;
							}

						}
					}
				}

			}
		}
		return super.dispatchTouchEvent(ev);
	}
*/

	@Override
	public void removeAllViews() {
		super.removeAllViews();
		rows.clear();
	}

	@Override
	public void removeAllViewsInLayout() {
		super.removeAllViewsInLayout();
		rows.clear();
	}


	public boolean removeViewFromRow(View view) {
		for (Row row : rows) {
			for (Column column : row.columns) {
				if (column.viewExists(view)) {
					column.removeView(view);
					if (column.getViewSize() == 0) {
						row.remove(column);
						if (row.getColumnSize() == 0) {
							removeRow(row);
						}
					}
					return true;
				}
			}
			for (Column column : row.columns) {
				View child = column.getView();
				if (child instanceof GridLayout) {
					if (((GridLayout) child).removeViewFromRow(view)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void removeRow(Row row) {
		rows.remove(row);
	}


	@Override
	public void removeViewInLayout(View view) {
		removeViewFromRow(view);
		super.removeViewInLayout(view);
	}

	@Override
	public void removeViewsInLayout(int start, int count) {
		final int end = start + count;

		for (int i = start; i < end; i++) {
			removeViewFromRow(getChildAt(i));
		}
		super.removeViewsInLayout(start, count);
	}

	@Override
	public void removeView(View view) {
		removeViewFromRow(view);
		super.removeView(view);
	}

	@Override
	public void removeViewAt(int index) {
		removeViewFromRow(getChildAt(index));
		super.removeViewAt(index);
	}

	@Override
	public void removeViews(int start, int count) {
		final int end = start + count;

		for (int i = start; i < end; i++) {
			removeViewFromRow(getChildAt(i));
		}
		super.removeViews(start, count);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int rowHeight = heightSize;
		measuredWidth = widthSize;
		measuredHeight = heightSize;
		rowHeight = rowHeight - getPaddingTop() - getPaddingBottom();
		int numRows = getVisibleRows();
		if (numRows == 0) {
			setMeasuredDimension(0, 0);
			return;
		}
		rowHeight /= numRows;
		int columnWidth = widthSize;
		columnWidth = columnWidth - getPaddingLeft() - getPaddingRight();
		int numColumns = getNumColumns();
		if (numColumns == 0) {
			setMeasuredDimension(0, 0);
			return;
		}
//        columnWidth /= numColumns;
		if (debugging) {
			Logger.debug("onMeasure:");
			Logger.debug("width:" + widthSize);
			Logger.debug("heightSize:" + heightSize);
			if (widthMode == MeasureSpec.EXACTLY) {
				Logger.debug("WIDTH EXACTLY");
			} else if (widthMode == MeasureSpec.UNSPECIFIED) {
				Logger.debug("WIDTH UNSPECIFIED");
			} else if (widthMode == MeasureSpec.AT_MOST) {
				Logger.debug("WIDTH AT_MOST");
			}
			if (heightMode == MeasureSpec.EXACTLY) {
				Logger.debug("Height EXACTLY");
			} else if (heightMode == MeasureSpec.UNSPECIFIED) {
				Logger.debug("Height UNSPECIFIED");
			} else if (heightMode == MeasureSpec.AT_MOST) {
				Logger.debug("Height AT_MOST");
			}
			Logger.debug("rowHeight:" + rowHeight);
			Logger.debug("columnWidth:" + columnWidth);
		}

		float cellWidth = columnWidth, cellHeight = rowHeight;

		// Need to go through all the cells, give the minimum to those who don't expand, then give the rest to those who want to expand

		int heightToUse = heightSize - getPaddingTop() - getPaddingBottom();
//        int maxRowHeight = heightSize - getPaddingTop() - getPaddingBottom();
		int totalRowHeights = 0, maxColumnWidth = 0, totalExpandableHeight = 0;
		int expandableRows = 0;//, rowCount = 0, columnCount = 0;

		// First go through all the rows that are not expandable to get the fixed row size
		for (Row row : rows) {
			boolean visibleRow = rowIsVisible(row);
			if (row.isExpandable() && visibleRow) {
				expandableRows++;
				continue;
			}
			if (!visibleRow) {
				continue;
			}
			if (debugging) {
				Logger.debug("Non-Expandable Row:" + rowCount++);
			}
			int widthToUse = widthSize - getPaddingLeft() - getPaddingRight();
			int maxHeight = 0, totalColumnWidths = 0;
			int nonExpandableColumns = 0, expandableColumns = 0;
			int visibleColumns = row.getVisibleNonExpandableColumns();
			if (visibleColumns == 0) {
				cellWidth = columnWidth;
			} else {
				cellWidth = columnWidth / visibleColumns;
			}
			if (debugging) {
				Logger.debug("cellWidth:" + cellWidth);
			}
			columnCount = 0;
			for (Column column : row.columns) {
				View child = column.getView();
				if (child == null) {
					continue;
				}
				if (child.getVisibility() != GONE) {
					if (column.isExpandable()) {
						expandableColumns++;
						continue;
					}
					if (debugging) {
						Logger.debug("Column:" + columnCount++);
					}
					nonExpandableColumns++;
					LayoutParams lp = (LayoutParams) child.getLayoutParams();
					int widthSpec = 0, heightSpec = 0;
					int width = Math.round(cellWidth * lp.getColumnLength());
					int height = Math.round(cellHeight * lp.getRowLength());
					if (width <= 0) {
						widthSpec = MeasureSpec.makeMeasureSpec(width,
								MeasureSpec.UNSPECIFIED);
					} else {
						widthSpec = MeasureSpec.makeMeasureSpec(width,
								MeasureSpec.AT_MOST);
					}
					if (height <= 0) {
						heightSpec = MeasureSpec.makeMeasureSpec(height,
								MeasureSpec.UNSPECIFIED);
					} else {
						heightSpec = MeasureSpec.makeMeasureSpec(height,
								MeasureSpec.AT_MOST);
					}
					child.measure(widthSpec, heightSpec);
					int childWidth = child.getMeasuredWidth();
					if (childWidth < width) {
						childWidth += child.getPaddingLeft() + child.getPaddingRight();
					}
					// Check to see if it hasn't been set
					if (column.getFixedWidth() == 0) {
						column.setWidth(childWidth);
					} else {
						column.setWidth(column.getFixedWidth());
					}
					totalColumnWidths += column.getWidth();
					if (debugging) {
						Logger.debug("View: " + child.getClass().toString() + " width:" + childWidth);
					}
					widthToUse -= column.getWidth();
					maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom());
				}
			}
			row.setNonExpandableColumns(nonExpandableColumns);
			if (expandableColumns > 0) {
				int remainderColumnWidth = widthToUse / expandableColumns;
				for (Column column : row.columns) {
					if (!column.isExpandable()) {
						continue;
					}
					View child = column.getView();
					if (child == null) {
						continue;
					}
					if (child.getVisibility() != GONE) {
						LayoutParams lp = (LayoutParams) child.getLayoutParams();
						int height = Math.round(cellHeight * lp.getRowLength());
						int widthSpec = 0, heightSpec = 0;
						int width = remainderColumnWidth - child.getPaddingLeft() - child.getPaddingRight();
						if (width <= 0) {
							widthSpec = MeasureSpec.makeMeasureSpec(width,
									MeasureSpec.UNSPECIFIED);
						} else {
							widthSpec = MeasureSpec.makeMeasureSpec(width,
									MeasureSpec.EXACTLY);
						}
						if (height <= 0) {
							heightSpec = MeasureSpec.makeMeasureSpec(height,
									MeasureSpec.UNSPECIFIED);
						} else {
							heightSpec = MeasureSpec.makeMeasureSpec(height,
									MeasureSpec.AT_MOST);
						}
						child.measure(widthSpec, heightSpec);
						int childWidth = child.getMeasuredWidth();
						if (debugging) {
							Logger.debug("View: " + child.getClass().toString() + " width:" + childWidth);
						}
						// Check to see if it hasn't been set
						if (column.getFixedWidth() == 0) {
							column.setWidth(childWidth);
						} else {
							column.setWidth(column.getFixedWidth());
						}
						totalColumnWidths += column.getWidth();
						maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom());
						if (debugging) {
							Logger.debug("maxHeight:" + maxHeight);
						}
					}
				}
			}
			maxColumnWidth = Math.max(maxColumnWidth, totalColumnWidths);
			if (row.getFixedHeight() == 0) {
				if (maxHeight == 0) {
//                    Logger.debug("maxHeight = 0 setting to RowHeight:" + rowHeight);
					row.setHeight(rowHeight);
				} else {
					row.setHeight(maxHeight);
				}
			} else {
				row.setHeight(row.getFixedHeight());
			}
			totalRowHeights += row.getHeight();
			if (debugging) {
				Logger.debug("setting RowHeight:" + maxHeight);
			}
			heightToUse -= row.getHeight();
		}
		rowCount = 0;
		for (Row row : rows) {
			boolean visibleRow = rowIsVisible(row);
			if (!row.isExpandable() || !visibleRow) {
				continue;
			}
			if (debugging) {
				Logger.debug("Exandable Row:" + rowCount++);
			}
			int widthToUse = widthSize - getPaddingLeft() - getPaddingRight();
			int maxHeight = 0, totalColumnWidths = 0;
			int nonExpandableColumns = 0, expandableColumns = 0;
			int visibleColumns = row.getVisibleNonExpandableColumns();
			if (visibleColumns == 0) {
				cellWidth = columnWidth;
			} else {
				cellWidth = columnWidth / visibleColumns;
			}
			if (debugging) {
				Logger.debug("cellWidth:" + cellWidth);
			}
			columnCount = 0;
			for (Column column : row.columns) {
				View child = column.getView();
				if (child == null) {
					continue;
				}
				if (child.getVisibility() != GONE) {
					if (column.isExpandable()) {
						expandableColumns++;
						continue;
					}
					if (debugging) {
						Logger.debug("Column:" + columnCount++);
					}
					nonExpandableColumns++;
					LayoutParams lp = (LayoutParams) child.getLayoutParams();
					int widthSpec = 0, heightSpec = 0;
					int width = Math.round(cellWidth * lp.getColumnLength());
//                    int height = Math.round(cellHeight * lp.getRowLength());
					if (width <= 0) {
						widthSpec = MeasureSpec.makeMeasureSpec(width,
								MeasureSpec.UNSPECIFIED);
					} else {
						widthSpec = MeasureSpec.makeMeasureSpec(width,
								MeasureSpec.AT_MOST);
					}
					if (heightToUse <= 0) {
						heightSpec = MeasureSpec.makeMeasureSpec(heightToUse,
								MeasureSpec.UNSPECIFIED);
					} else {
						heightSpec = MeasureSpec.makeMeasureSpec(heightToUse,
								MeasureSpec.AT_MOST);
					}
					child.measure(widthSpec, heightSpec);
					int childWidth = child.getMeasuredWidth();
					if (childWidth < width) {
						childWidth += child.getPaddingLeft() + child.getPaddingRight();
					}
					// Check to see if it hasn't been set
					if (column.getFixedWidth() == 0) {
						column.setWidth(childWidth);
					} else {
						column.setWidth(column.getFixedWidth());
					}
					totalColumnWidths += column.getWidth();
					if (debugging) {
						Logger.debug("View: " + child.getClass().toString() + " width:" + childWidth);
					}
					widthToUse -= column.getWidth();
					maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom());
				}
			}
			row.setNonExpandableColumns(nonExpandableColumns);
			if (expandableColumns > 0) {
				int remainderColumnWidth = widthToUse / expandableColumns;
				for (Column column : row.columns) {
					if (!column.isExpandable()) {
						continue;
					}
					View child = column.getView();
					if (child == null) {
						continue;
					}
					if (child.getVisibility() != GONE) {
						LayoutParams lp = (LayoutParams) child.getLayoutParams();
						int widthSpec = 0, heightSpec = 0;
						int width = remainderColumnWidth - child.getPaddingLeft() - child.getPaddingRight();
						if (width <= 0) {
							widthSpec = MeasureSpec.makeMeasureSpec(width,
									MeasureSpec.UNSPECIFIED);
						} else {
							widthSpec = MeasureSpec.makeMeasureSpec(width,
									MeasureSpec.EXACTLY);
						}
						if (heightToUse <= 0) {
							heightSpec = MeasureSpec.makeMeasureSpec(heightToUse,
									MeasureSpec.UNSPECIFIED);
						} else {
							heightSpec = MeasureSpec.makeMeasureSpec(heightToUse,
									MeasureSpec.AT_MOST);
						}
						child.measure(widthSpec, heightSpec);
						int childWidth = child.getMeasuredWidth();
						if (debugging) {
							Logger.debug("View: " + child.getClass().toString() + " width:" + childWidth);
						}
						// Check to see if it hasn't been set
						if (column.getFixedWidth() == 0) {
							column.setWidth(childWidth);
						} else {
							column.setWidth(column.getFixedWidth());
						}
						totalColumnWidths += column.getWidth();
						maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom());
						if (debugging) {
							Logger.debug("maxHeight:" + maxHeight);
						}
					}
				}
			}
			maxColumnWidth = Math.max(maxColumnWidth, totalColumnWidths);
		}
		// Go through all expandable rows to figure out the remaining size
		if (expandableRows > 0) {
			if (debugging) {
//                Logger.debug("Original Total Height:" + maxRowHeight);
				Logger.debug("totalRowHeights:" + totalRowHeights);
				Logger.debug("heightToUse:" + heightToUse);
				Logger.debug("expandableRows:" + expandableRows);
			}
			int remainderRowHeight = heightToUse / expandableRows;
//            float percent = (float)heightToUse/(float)totalExpandableHeight;
			for (Row row : rows) {
				if (row.isExpandable() && rowIsVisible(row)) {
					int maxHeight = 0, totalColumnWidths = 0;
					row.setHeight(remainderRowHeight);
					for (Column column : row.columns) {
						View child = column.getView();
						if (child == null) {
							continue;
						}
						if (child.getVisibility() != GONE) {
							int widthSpec = 0, heightSpec = 0;
							int width = child.getMeasuredWidth();
							widthSpec = MeasureSpec.makeMeasureSpec(width,
									MeasureSpec.EXACTLY);
							heightSpec = MeasureSpec.makeMeasureSpec(remainderRowHeight,
									MeasureSpec.AT_MOST);
							if (child instanceof GridLayout) {
								((GridLayout) child).measureExactHeight(heightSpec);
							} else {
								child.measure(widthSpec, heightSpec);
							}
							int childWidth = child.getMeasuredWidth();
							if (childWidth < width) {
								childWidth += child.getPaddingLeft() + child.getPaddingRight();
							}
							if (debugging) {
								Logger.debug("Expanding View: " + child.getClass().toString() + " width:" + childWidth);
							}
							// Check to see if it hasn't been set
							if (column.getFixedWidth() == 0) {
								column.setWidth(childWidth);
							} else {
								column.setWidth(column.getFixedWidth());
							}
							totalColumnWidths += column.getWidth();
						}

					}
					totalRowHeights += row.getHeight();
					maxColumnWidth = Math.max(maxColumnWidth, totalColumnWidths);
//                    remainderRowHeight -= row.getHeight();
//                    Logger.debug("setting RowHeight:" + remainderRowHeight);
				}
			}
		}

		if (debugging) {
			Logger.debug("setMeasuredDimension for GridLayout");
			Logger.debug("maxColumnWidth:" + maxColumnWidth);
			Logger.debug("totalRowHeights:" + totalRowHeights);
		}
		setMeasuredDimension(maxColumnWidth, totalRowHeights);
		fireMeasureListeners();
	}

	/**
	 * Measure all children with the exact size. Not meant to measure the gridlayout
	 *
	 * @param heightMeasureSpec
	 */
	protected void measureExactHeight(int heightMeasureSpec) {
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		for (Row row : rows) {
			boolean visibleRow = rowIsVisible(row);
			if (!visibleRow) {
				continue;
			}
			for (Column column : row.columns) {
				View child = column.getView();
				if (child == null) {
					continue;
				}
				if (child.getVisibility() != GONE) {
					int widthSpec = 0, heightSpec = 0;
					int width = child.getMeasuredWidth();
					widthSpec = MeasureSpec.makeMeasureSpec(width,
							MeasureSpec.EXACTLY);
					heightSpec = MeasureSpec.makeMeasureSpec(heightSize,
							MeasureSpec.EXACTLY);
					if (child instanceof GridLayout) {
						((GridLayout) child).measureExactHeight(heightSpec);
					} else {
						child.measure(widthSpec, heightSpec);
					}
					int childWidth = child.getMeasuredWidth();
					if (debugging) {
						Logger.debug("measureExactHeight:View: " + child.getClass().toString() + " width:" + childWidth);
					}
				}
			}
		}

	}

	protected int getVisibleRows() {
		int rowCount = 0;
		for (Row row : rows) {
			boolean visibleColumnFound = rowIsVisible(row);
			if (visibleColumnFound) {
				rowCount++;
			}
		}
		return rowCount;
	}

	protected boolean rowIsVisible(Row row) {
		boolean visibleColumnFound = false;
		for (Column column : row.columns) {
			View child = column.getView();
//            if (child instanceof GridLayout) {
//                for (Row childRow : ((GridLayout)child).rows) {
//                    if (rowIsVisible(childRow)) {
//                        visibleColumnFound = true;
//                        break;
//                    }
//                }
			if (child != null && child.getVisibility() != GONE) {
				visibleColumnFound = true;
				break;
			}
		}
		return visibleColumnFound;
	}

	private int lastWidth, lastHeight;

	public void debug() {
		Logger.debug("GridLayout has " + rows.size() + " rows");
		Logger.debug("Measured Width " + measuredWidth);
		Logger.debug("Measured height " + measuredHeight);
		Logger.debug("Layout Width " + layoutWidth);
		Logger.debug("Layout height " + layoutHeight);
		Logger.debug("Layout left " + layoutLeft);
		Logger.debug("Layout top " + layoutTop);
		Logger.debug("Layout right " + layoutRight);
		Logger.debug("Layout bottom " + layoutBottom);
		int rowCount = 0;
		for (Row row : rows) {
			int rectHeight = row.getHeight();
			Logger.debug("Row " + rowCount + " has " + row.columns.size() + " columns and is " + rectHeight + " high");
			int columnCount = 0;
			for (Column column : row.columns) {
				View child = column.getView();
				Logger.debug("Column " + columnCount + " has view " + child.getClass().getName());
				Logger.debug("Column " + columnCount + " is " + column.getWidth() + " wide");
				columnCount++;
				if (child instanceof GridLayout) {
					((GridLayout) child).debug();
				}
			}
			rowCount++;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = r - l;
		int height = b - t;
		layoutLeft = l;
		layoutTop = t;
		layoutRight = r;
		layoutBottom = b;
		layoutWidth = width;
		layoutHeight = height;
		if (debugging) {
			Logger.debug("onLayout");
			Logger.debug("layout left:" + l);
			Logger.debug("layout top:" + t);
			Logger.debug("layout width:" + width);
			Logger.debug("layout height:" + height);
//            int startTop = t + getPaddingTop(), cl=l, ct = 0, cr = 0, cb = 0;
		}
		int startTop = getPaddingTop(), cl = 0, ct = 0, cr = 0, cb = 0;
		for (Row row : rows) {
			int startLeft = 0;
			int rectHeight = row.getHeight();
			for (Column column : row.columns) {
				View child = column.getView();
				if (child == null) {
					continue;
				}
				if (child.getVisibility() != GONE) {
					LayoutParams lp = (LayoutParams) child.getLayoutParams();
					int rectWidth = column.getWidth();
					if (lp.gravity == Gravity.CENTER_HORIZONTAL || lp.gravity == Gravity.CENTER) {
						if (rectWidth > width) {
							cl = child.getPaddingLeft();
						} else if (width == rectWidth) {
							cl = child.getPaddingLeft();
						} else {
							cl = (width / 2) - (rectWidth / 2) + child.getPaddingLeft();
						}
					} else if (lp.gravity == Gravity.RIGHT) {
						if (width == rectWidth) {
							cl = width - getPaddingRight() - child.getPaddingRight();
						} else {
							cl = width - rectWidth - getPaddingRight() - child.getPaddingRight();
						}
					} else {
						cl = getPaddingLeft() + child.getPaddingLeft() + startLeft;
					}
					ct = child.getPaddingTop() + startTop;
					if (lp.gravity == Gravity.CENTER_VERTICAL || lp.gravity == Gravity.CENTER) {
						int extraHeight = (rectHeight / 2) - (child.getMeasuredHeight() / 2);
						if (extraHeight > 0 && ((extraHeight + ct) < rectHeight)) {
							ct += extraHeight;
						}
					}
//                    cr = cr + (rectWidth + child.getPaddingLeft() + child.getPaddingRight());
					cr = cl + rectWidth;
					cr = Math.min(cr, layoutWidth);
//                    cb = ct + (rectHeight + child.getPaddingTop() + child.getPaddingBottom());
					cb = ct + rectHeight;
					cb = Math.min(cb, layoutHeight);
					if (debugging) {
						Logger.debug("View: " + child.getClass().toString());
						Logger.debug("setting layout left:" + cl + " top:" + ct + " right:" + cr + " bottom: " + cb);
					}
					if (child instanceof GridLayout) {
						if (((GridLayout) child).getVisibleRows() != 0) {
							child.layout(cl, ct, cr, cb);
						}
					} else {
						child.layout(cl, ct, cr, cb);
					}
					startLeft += rectWidth;
				}
			}
			startTop += rectHeight;
		}
		lastWidth = width;
		lastHeight = height;
		fireLayoutListeners();
	}

	public android.view.ViewGroup.LayoutParams generateLayoutParams(
			AttributeSet attrs) {
		return LayoutParams.createLayout(1, 1);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return LayoutParams.createLayout(p.width, p.height, 1, 1);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return LayoutParams.createLayout(1, 1);
	}

	protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
		if (p instanceof LayoutParams) {
			int columns = getNumColumns(), rows = getNumRows();
			LayoutParams lp = (LayoutParams) p;
			if (lp.columnEnd > columns || lp.columnStart < 0)
				return false;
			if (lp.rowEnd > rows || lp.rowStart < 0)
				return false;
			return lp.columnEnd > lp.columnStart && lp.rowEnd > lp.rowStart;
		} else
			return false;
	}

	class Row {
		protected int rowStart;
		protected int nonExpandableColumns;
		protected int height;
		protected int fixedHeight;
		protected int y;
		private boolean expandable = false;
		protected ArrayList<Column> columns = new ArrayList<Column>();

		public ArrayList<Column> getColumns() {
			return columns;
		}

		public void setColumns(ArrayList<Column> columns) {
			this.columns = columns;
		}

		public int getRowStart() {
			return rowStart;
		}

		public void setRowStart(int rowStart) {
			this.rowStart = rowStart;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getFixedHeight() {
			return fixedHeight;
		}

		public void setFixedHeight(int fixedHeight) {
			this.fixedHeight = fixedHeight;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public void addColumn(Column column) {
			columns.add(column);
		}

		public Column getColumn(int column) {
			if (columns.size() < column) {
				return null;
			} else {
				return columns.get(column);
			}
		}

		public int getNonExpandableColumns() {
			return nonExpandableColumns;
		}

		public void setNonExpandableColumns(int nonExpandableColumns) {
			this.nonExpandableColumns = nonExpandableColumns;
		}

		public boolean isExpandable() {
			return expandable;
		}

		public void setExpandable(boolean expandable) {
			this.expandable = expandable;
		}

		public void remove(Column column) {
			columns.remove(column);
		}

		public int getColumnSize() {
			return columns.size();
		}

		public int getViewSize(int column) {
			if (columns.size() < column) {
				return 0;
			} else {
				return columns.get(column).getViewSize();
			}
		}

		public int getVisibleColumns() {
			int count = 0;
			for (Column column : columns) {
				View view = column.getView();
				if (view != null && view.getVisibility() != View.GONE) {
					count++;
				}
			}
			return count;
		}

		public int getVisibleNonExpandableColumns() {
			int count = 0;
			for (Column column : columns) {
				if (column.isExpandable()) {
					continue;
				}
				View view = column.getView();
				if (view != null && view.getVisibility() != View.GONE) {
					count++;
				}
			}
			return count;
		}

		public int getVisibleExpandableColumns() {
			int count = 0;
			for (Column column : columns) {
				if (!column.isExpandable()) {
					continue;
				}
				View view = column.getView();
				if (view != null && view.getVisibility() != View.GONE) {
					count++;
				}
			}
			return count;
		}
	}

	class Column {
		protected int width;
		protected int fixedWidth;
		protected int x;
		private View view;
		private int columnStart;
		private boolean expandable = false;
		private List<View> views = new ArrayList<View>();

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getFixedWidth() {
			return fixedWidth;
		}

		public void setFixedWidth(int fixedWidth) {
			this.fixedWidth = fixedWidth;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public void addView(View child) {
			views.add(child);
		}

		public View getView(int index) {
			if (views.size() > index) {
				return views.get(index);
			}
			return view;
		}

		public void setView(View child) {
			if (this.view != null) {
				addView(child);
				return;
			}
			this.view = child;
		}

		public View getView() {
			if (view != null && view.getVisibility() != GONE) {
				return view;
			}
			for (View childView : views) {
				if (childView.getVisibility() != GONE) {
					return childView;
				}
			}
			return view;
		}

		public List<View> getViews() {
			List<View> viewList = new ArrayList<View>();
			if (view != null) {
				viewList.add(view);
			}
			viewList.addAll(views);
			return viewList;
		}

		public boolean viewExists(View viewToFind) {
			if (view == viewToFind) {
				return true;
			}
			for (View childView : views) {
				if (childView == viewToFind) {
					return true;
				}
			}
			return false;
		}


		public int getColumnStart() {
			return columnStart;
		}

		public void setColumn(int columnStart) {
			this.columnStart = columnStart;
		}

		public boolean isExpandable() {
			return expandable;
		}

		public void setExpandable(boolean expandable) {
			this.expandable = expandable;
		}

		public int getViewSize() {
			int size = view == null ? 0 : 1;
			size += views.size();
			return size;
		}

		public void removeView(View view) {
			if (this.view == view) {
				this.view = null;
			} else {
				for (View myView : views) {
					if (myView == view) {
						views.remove(view);
						return;
					}
				}
			}
		}
	}
}
