/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ctflab.locker.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ctflab.locker.R;
import com.ctflab.locker.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 * 
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class LockPatternView extends View {
	// Aspect to use when rendering this view
	private static final int ASPECT_SQUARE = 0; // View will be the minimum of
												// width/height
	private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will
													// be minimum of (w,h)
	private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will
														// be minimum of (w,h)

	// Vibrator pattern for creating a tactile bump
	private static final long[] DEFAULT_VIBE_PATTERN = { 0, 1, 40, 41 };

	private static final boolean PROFILE_DRAWING = false;
	private boolean mDrawingProfilingStarted = false;

	private Paint mPaint = new Paint();
	private Paint mPathPaint = new Paint();

	// TODO: make this common with PhoneWindow
	static final int STATUS_BAR_HEIGHT = 25;

	/**
	 * How many milliseconds we spend animating each circle of a lock pattern if
	 * the animating mode is set. The entire animation should take this constant
	 * * the length of the pattern to complete.
	 */
	private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;

	private OnPatternListener mOnPatternListener;
	private ArrayList<Cell> mPattern = new ArrayList<Cell>(9);

	/**
	 * Lookup table for the circles of the pattern we are currently drawing.
	 * This will be the cells of the complete pattern unless we are animating,
	 * in which case we use this to hold the cells we are drawing for the in
	 * progress animation.
	 */
	private boolean[][] mPatternDrawLookup = new boolean[3][3];

	/**
	 * the in progress point: - during interaction: where the user's finger is -
	 * during animation: the current tip of the animating line
	 */
	private float mInProgressX = -1;
	private float mInProgressY = -1;

	private long mAnimatingPeriodStart;

	private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
	private boolean mInputEnabled = true;
	private boolean mInStealthMode = false;
	private boolean mTactileFeedbackEnabled = false;
	private boolean mPatternInProgress = false;

	private float mDiameterFactor = 0.5f;
	private float mHitFactor = 0.6f;

	private float mSquareWidth;
	private float mSquareHeight;

	private Bitmap mBitmapBtnDefault;
	private Bitmap mBitmapBtnTouched;
	private Bitmap mBitmapCircleDefault;
	private Bitmap mBitmapCircleGreen;
	private Bitmap mBitmapCircleRed;

	private Bitmap mBitmapArrowGreenUp;
	private Bitmap mBitmapArrowRedUp;

	private final Path mCurrentPath = new Path();
	private final Rect mInvalidate = new Rect();

	private int mBitmapWidth;
	private int mBitmapHeight;

	private Vibrator vibe; // Vibrator for creating tactile feedback

	private long[] mVibePattern;

	private int mAspect;

	private int mWidth,mHeight;

	/**
	 * Represents a cell in the 3 X 3 matrix of the unlock pattern view.
	 */
	public static class Cell {
		int row;
		int column;

		// keep # objects limited to 9
		static Cell[][] sCells = new Cell[3][3];
		static {
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					sCells[i][j] = new Cell(i, j);
				}
			}
		}

		/**
		 * @param row
		 *            The row of the cell.
		 * @param column
		 *            The column of the cell.
		 */
		private Cell(int row, int column) {
			checkRange(row, column);
			this.row = row;
			this.column = column;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		/**
		 * @param row
		 *            The row of the cell.
		 * @param column
		 *            The column of the cell.
		 */
		public static synchronized Cell of(int row, int column) {
			checkRange(row, column);
			return sCells[row][column];
		}

		private static void checkRange(int row, int column) {
			if (row < 0 || row > 2) {
				throw new IllegalArgumentException("row must be in range 0-2");
			}
			if (column < 0 || column > 2) {
				throw new IllegalArgumentException(
						"column must be in range 0-2");
			}
		}

		public String toString() {
			return "(row=" + row + ",clmn=" + column + ")";
		}
	}

	/**
	 * How to display the current pattern.
	 */
	public enum DisplayMode {

		/**
		 * The pattern drawn is correct (i.e draw it in a friendly color)
		 */
		Correct,

		/**
		 * Animate the pattern (for demo, and help).
		 */
		Animate,

		/**
		 * The pattern is wrong (i.e draw a foreboding color)
		 */
		Wrong
	}

	/**
	 * The call back interface for detecting patterns entered by the user.
	 */
	public static interface OnPatternListener {

		/**
		 * A new pattern has begun.
		 */
		void onPatternStart();

		/**
		 * The pattern was cleared.
		 */
		void onPatternCleared();

		/**
		 * The user extended the pattern currently being drawn by one cell.
		 * 
		 * @param pattern
		 *            The pattern with newly added cell.
		 */
		void onPatternCellAdded(List<Cell> pattern);

		/**
		 * A pattern was detected from the user.
		 * 
		 * @param pattern
		 *            The pattern.
		 */
		void onPatternDetected(List<Cell> pattern);
	}

	public LockPatternView(Context context) {
		this(context, null);
	}

	public LockPatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.LockPatternView);

		final String aspect = a.getString(R.styleable.LockPatternView_aspect);

		if ("square".equals(aspect)) {
			mAspect = ASPECT_SQUARE;
		} else if ("lock_width".equals(aspect)) {
			mAspect = ASPECT_LOCK_WIDTH;
		} else if ("lock_height".equals(aspect)) {
			mAspect = ASPECT_LOCK_HEIGHT;
		} else {
			mAspect = ASPECT_SQUARE;
		}

		setClickable(true);

		mPathPaint.setAntiAlias(true);
		mPathPaint.setDither(true);
		mPathPaint.setAlpha(128);
		mPathPaint.setStyle(Paint.Style.STROKE);
		mPathPaint.setStrokeJoin(Paint.Join.ROUND);
		mPathPaint.setStrokeCap(Paint.Cap.ROUND);
		mPathPaint.setStrokeWidth(SystemUtil.dip2px(context,4));

		// lot's of bitmaps!
		mBitmapBtnDefault = scaleBitmap(R.drawable.patternlock_inner_default);
		mBitmapBtnTouched = scaleBitmap(R.drawable.patternlock_inner_touched);
		// mBitmapCircleDefault =
		// getBitmapFor(R.drawable.patternlock_outer_default);
		mBitmapCircleGreen = scaleBitmap(R.drawable.patternlock_outer_green);
		mBitmapCircleRed = scaleBitmap(R.drawable.patternlock_outer_red);

		mBitmapArrowGreenUp = getBitmapFor(R.drawable.patternlock_direction_green_up);
		mBitmapArrowRedUp = getBitmapFor(R.drawable.patternlock_direction_red_up);

		// we assume all bitmaps have the same size
//		mBitmapWidth = mBitmapBtnDefault.getWidth();
//		mBitmapHeight = mBitmapBtnDefault.getHeight();
		mBitmapWidth = mBitmapCircleGreen.getWidth();
		mBitmapHeight = mBitmapCircleGreen.getHeight();
		// allow vibration pattern to be customized
		mVibePattern = loadVibratePattern(R.array.config_virtualKeyVibePattern);
		
		if(a!=null){
			a.recycle();
			a = null;
		}
	}

	public List<Cell> getPattern() {
		return mPattern;
	}

	private long[] loadVibratePattern(int id) {
		int[] pattern = null;
		try {
			pattern = getResources().getIntArray(id);
		} catch (Resources.NotFoundException e) {
//			Log.e("LockPatternView", "Vibrate pattern missing, using default",
//					e);
		}
		if (pattern == null) {
			return DEFAULT_VIBE_PATTERN;
		}

		long[] tmpPattern = new long[pattern.length];
		for (int i = 0; i < pattern.length; i++) {
			tmpPattern[i] = pattern[i];
		}
		return tmpPattern;
	}

	private Bitmap getBitmapFor(int resId) {
		return BitmapFactory.decodeResource(getContext().getResources(), resId);
	}

	private Bitmap scaleBitmap(int resId){
		Bitmap bitmap = getBitmapFor(resId);
		Matrix matrix = new Matrix();
		matrix.setScale(0.75f,0.75f);
		return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
	}

	/**
	 * @return Whether the view is in stealth mode.
	 */
	public boolean isInStealthMode() {
		return mInStealthMode;
	}

	/**
	 * Set whether the view is in stealth mode. If true, there will be no
	 * visible feedback as the user enters the pattern.
	 * 
	 * @param inStealthMode
	 *            Whether in stealth mode.
	 */
	public void setInStealthMode(boolean inStealthMode) {
		mInStealthMode = inStealthMode;
	}

	/**
	 * Set whether the view will use tactile feedback. If true, there will be
	 * tactile feedback as the user enters the pattern.
	 * 
	 * @param tactileFeedbackEnabled
	 *            Whether tactile feedback is enabled
	 */
	public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
		mTactileFeedbackEnabled = tactileFeedbackEnabled;
	}

	/**
	 * @return Whether tactile feedback for the pattern is enabled.
	 */
	public boolean isTactileFeedbackEnabled() {
		return mTactileFeedbackEnabled;
	}

	// /**
	// * Set whether tactile feedback for the pattern is enabled.
	// */
	// public void setTactileFeedbackEnabled(boolean enabled) {
	// mTactileFeedbackEnabled = enabled;
	// //setBoolean(Settings.Secure.LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED,
	// enabled);
	// }

	/**
	 * Set the call back for pattern detection.
	 * 
	 * @param onPatternListener
	 *            The call back.
	 */
	public void setOnPatternListener(OnPatternListener onPatternListener) {
		mOnPatternListener = onPatternListener;
	}

	/**
	 * Set the pattern explicitely (rather than waiting for the user to input a
	 * pattern).
	 * 
	 * @param displayMode
	 *            How to display the pattern.
	 * @param pattern
	 *            The pattern.
	 */
	public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
		mPattern.clear();
		mPattern.addAll(pattern);
		clearPatternDrawLookup();
		for (Cell cell : pattern) {
			mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
		}

		setDisplayMode(displayMode);
	}

	/**
	 * Set the display mode of the current pattern. This can be useful, for
	 * instance, after detecting a pattern to tell this view whether change the
	 * in progress result to correct or wrong.
	 * 
	 * @param displayMode
	 *            The display mode.
	 */
	public void setDisplayMode(DisplayMode displayMode) {
		mPatternDisplayMode = displayMode;
		if (displayMode == DisplayMode.Animate) {
			if (mPattern.size() == 0) {
				throw new IllegalStateException(
						"you must have a pattern to "
								+ "animate if you want to set the display mode to animate");
			}
			mAnimatingPeriodStart = SystemClock.elapsedRealtime();
			final Cell first = mPattern.get(0);
			mInProgressX = getCenterXForColumn(first.getColumn());
			mInProgressY = getCenterYForRow(first.getRow());
			clearPatternDrawLookup();
		}
		invalidate();
	}

	/**
	 * Clear the pattern.
	 */
	public void clearPattern() {
		resetPattern();
	}

	/**
	 * Reset all pattern state.
	 */
	private void resetPattern() {
		mPattern.clear();
		clearPatternDrawLookup();
		mPatternDisplayMode = DisplayMode.Correct;
		invalidate();
	}

	/**
	 * Clear the pattern lookup table.
	 */
	private void clearPatternDrawLookup() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				mPatternDrawLookup[i][j] = false;
			}
		}
	}

	/**
	 * Disable input (for instance when displaying a message that will timeout
	 * so user doesn't get view into messy state).
	 */
	public void disableInput() {
		mInputEnabled = false;
	}

	/**
	 * Enable input.
	 */
	public void enableInput() {
		mInputEnabled = true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;
		final int width = w - this.getPaddingLeft() - this.getPaddingRight();
		mSquareWidth = width / 3.0f;

		final int height = h - this.getPaddingTop() - this.getPaddingBottom();
		mSquareHeight = height / 3.0f;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		int viewWidth = width;
		int viewHeight = height;
//		switch (mAspect) {
//		case ASPECT_SQUARE:
//			viewWidth = viewHeight = Math.min(width, height);
//			break;
//		case ASPECT_LOCK_WIDTH:
//			viewWidth = width;
//			viewHeight = height != 0 ? Math.min(width, height) : width;
//			break;
//		case ASPECT_LOCK_HEIGHT:
//			viewWidth = Math.min(width, height);
//			viewHeight = height;
//			break;
//		}
		if (mBitmapHeight>0){
			viewHeight = mBitmapHeight*3+SystemUtil.dip2px(getContext(),45)*2;
		}
		setMeasuredDimension(viewWidth, viewHeight);
	}

	/**
	 * Determines whether the point x, y will add a new point to the current
	 * pattern (in addition to finding the cell, also makes heuristic choices
	 * such as filling in gaps based on current pattern).
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 */
	private Cell detectAndAddHit(float x, float y) {
		final Cell cell = checkForNewHit(x, y);
		if (cell != null) {

			// check for gaps in existing pattern
			Cell fillInGapCell = null;
			final ArrayList<Cell> pattern = mPattern;
			if (!pattern.isEmpty()) {
				final Cell lastCell = pattern.get(pattern.size() - 1);
				int dRow = cell.row - lastCell.row;
				int dColumn = cell.column - lastCell.column;

				int fillInRow = lastCell.row;
				int fillInColumn = lastCell.column;

				if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
					fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
				}

				if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
					fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
				}

				fillInGapCell = Cell.of(fillInRow, fillInColumn);
			}

			if (fillInGapCell != null
					&& !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
				addCellToPattern(fillInGapCell);
			}
			addCellToPattern(cell);
			if (mTactileFeedbackEnabled) {
				vibe.vibrate(mVibePattern, -1); // Generate tactile feedback
			}
			return cell;
		}
		return null;
	}

	private void addCellToPattern(Cell newCell) {
		mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
		mPattern.add(newCell);
		if (mOnPatternListener != null) {
			mOnPatternListener.onPatternCellAdded(mPattern);
		}
	}

	// helper method to find which cell a point maps to
	private Cell checkForNewHit(float x, float y) {

		final int rowHit = getRowHit(y);
		if (rowHit < 0) {
			return null;
		}
		final int columnHit = getColumnHit(x);
		if (columnHit < 0) {
			return null;
		}

		if (mPatternDrawLookup[rowHit][columnHit]) {
			return null;
		}
		return Cell.of(rowHit, columnHit);
	}

	/**
	 * Helper method to find the row that y falls into.
	 * 
	 * @param y
	 *            The y coordinate
	 * @return The row that y falls in, or -1 if it falls in no row.
	 */
	private int getRowHit(float y) {

		for (int i = 0; i < 3; i++) {

			final float localY = getCenterYForRow(i);
			if (y >= localY - mBitmapHeight/2 && y <= localY + mBitmapHeight/2) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Helper method to find the column x fallis into.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @return The column that x falls in, or -1 if it falls in no column.
	 */
	private int getColumnHit(float x) {
		for (int i = 0; i < 3; i++) {

			final float localX = getCenterXForColumn(i);
			if (x >= localX-mBitmapWidth/2 && x <= localX+mBitmapWidth/2) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		if (!mInputEnabled || !isEnabled()) {
			return false;
		}

		final float x = motionEvent.getX();
		final float y = motionEvent.getY();
		Cell hitCell;
		switch (motionEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			resetPattern();
			hitCell = detectAndAddHit(x, y);
			if (hitCell != null && mOnPatternListener != null) {
				mPatternInProgress = true;
				mPatternDisplayMode = DisplayMode.Correct;
				mOnPatternListener.onPatternStart();
			} else if (mOnPatternListener != null) {
				mPatternInProgress = false;
				mOnPatternListener.onPatternCleared();
			}
			if (hitCell != null) {
				final float startX = getCenterXForColumn(hitCell.column);
				final float startY = getCenterYForRow(hitCell.row);

				final float widthOffset = mBitmapWidth / 2f;
				final float heightOffset = mSquareHeight / 2f;

				invalidate((int) (startX - widthOffset),
						(int) (startY - heightOffset),
						(int) (startX + widthOffset),
						(int) (startY + heightOffset));
			}
			mInProgressX = x;
			mInProgressY = y;
			if (PROFILE_DRAWING) {
				if (!mDrawingProfilingStarted) {
					Debug.startMethodTracing("LockPatternDrawing");
					mDrawingProfilingStarted = true;
				}
			}
			return true;
		case MotionEvent.ACTION_UP:
			// report pattern detected
			if (!mPattern.isEmpty() && mOnPatternListener != null) {
				mPatternInProgress = false;
				mOnPatternListener.onPatternDetected(mPattern);
				invalidate();
			}
			if (PROFILE_DRAWING) {
				if (mDrawingProfilingStarted) {
					Debug.stopMethodTracing();
					mDrawingProfilingStarted = false;
				}
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			final int patternSizePreHitDetect = mPattern.size();
			hitCell = detectAndAddHit(x, y);
			final int patternSize = mPattern.size();
			if (hitCell != null && (mOnPatternListener != null)
					&& (patternSize == 1)) {
				mPatternInProgress = true;
				mOnPatternListener.onPatternStart();
			}
			// note current x and y for rubber banding of in progress
			// patterns
			final float dx = Math.abs(x - mInProgressX);
			final float dy = Math.abs(y - mInProgressY);
			if (dx + dy > mBitmapWidth * 0.01f) {
				float oldX = mInProgressX;
				float oldY = mInProgressY;

				mInProgressX = x;
				mInProgressY = y;

				if (mPatternInProgress && patternSize > 0) {
					final ArrayList<Cell> pattern = mPattern;
					final float radius = mBitmapBtnDefault.getWidth();
					final Cell lastCell = pattern.get(patternSize - 1);

					float startX = getCenterXForColumn(lastCell.column);
					float startY = getCenterYForRow(lastCell.row);

					float left;
					float top;
					float right;
					float bottom;

					final Rect invalidateRect = mInvalidate;

					if (startX < x) {
						left = startX;
						right = x;
					} else {
						left = x;
						right = startX;
					}

					if (startY < y) {
						top = startY;
						bottom = y;
					} else {
						top = y;
						bottom = startY;
					}

					// Invalidate between the pattern's last cell and the
					// current location
					invalidateRect.set((int) (left - radius),
							(int) (top - radius), (int) (right + radius),
							(int) (bottom + radius));

					if (startX < oldX) {
						left = startX;
						right = oldX;
					} else {
						left = oldX;
						right = startX;
					}

					if (startY < oldY) {
						top = startY;
						bottom = oldY;
					} else {
						top = oldY;
						bottom = startY;
					}

					// Invalidate between the pattern's last cell and the
					// previous location
					invalidateRect.union((int) (left - radius),
							(int) (top - radius), (int) (right + radius),
							(int) (bottom + radius));

					// Invalidate between the pattern's new cell and the
					// pattern's previous cell
					if (hitCell != null) {
						startX = getCenterXForColumn(hitCell.column);
						startY = getCenterYForRow(hitCell.row);

						if (patternSize >= 2) {
							// (re-using hitcell for old cell)
							hitCell = pattern.get(patternSize - 1
									- (patternSize - patternSizePreHitDetect));
							oldX = getCenterXForColumn(hitCell.column);
							oldY = getCenterYForRow(hitCell.row);

							if (startX < oldX) {
								left = startX;
								right = oldX;
							} else {
								left = oldX;
								right = startX;
							}

							if (startY < oldY) {
								top = startY;
								bottom = oldY;
							} else {
								top = oldY;
								bottom = startY;
							}
						} else {
							left = right = startX;
							top = bottom = startY;
						}

						final float widthOffset = mBitmapWidth / 2f;
						final float heightOffset = mBitmapHeight / 2f;

						invalidateRect.set((int) (left - widthOffset),
								(int) (top - heightOffset),
								(int) (right + widthOffset),
								(int) (bottom + heightOffset));
					}

					// invalidate(invalidateRect);
					invalidate();
				} else {
					invalidate();
				}
			}
			return true;
		case MotionEvent.ACTION_CANCEL:
			resetPattern();
			if (mOnPatternListener != null) {
				mPatternInProgress = false;
				mOnPatternListener.onPatternCleared();
			}
			if (PROFILE_DRAWING) {
				if (mDrawingProfilingStarted) {
					Debug.stopMethodTracing();
					mDrawingProfilingStarted = false;
				}
			}
			return true;
		}
		return false;
	}

	private float getCenterXForColumn(int column) {
		if (column == 0)
			return mWidth/2-mBitmapWidth-SystemUtil.dip2px(getContext(),45);
		else if (column == 1)
			return mWidth/2;
		else
			return mWidth/2+mBitmapWidth+SystemUtil.dip2px(getContext(),45);
	}

	private float getCenterYForRow(int row) {
		if (row == 0)
			return mBitmapHeight/2;
		else if (row == 1)
			return mHeight/2;
		else
			return mHeight - mBitmapHeight/2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final ArrayList<Cell> pattern = mPattern;
		final int count = pattern.size();
		final boolean[][] drawLookup = mPatternDrawLookup;

		if (mPatternDisplayMode == DisplayMode.Animate) {

			// figure out which circles to draw

			// + 1 so we pause on complete pattern
			final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
			final int spotInCycle = (int) (SystemClock.elapsedRealtime() - mAnimatingPeriodStart)
					% oneCycle;
			final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

			clearPatternDrawLookup();
			for (int i = 0; i < numCircles; i++) {
				final Cell cell = pattern.get(i);
				drawLookup[cell.getRow()][cell.getColumn()] = true;
			}

			// figure out in progress portion of ghosting line

			final boolean needToUpdateInProgressPoint = numCircles > 0
					&& numCircles < count;

			if (needToUpdateInProgressPoint) {
				final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
						/ MILLIS_PER_CIRCLE_ANIMATING;

				final Cell currentCell = pattern.get(numCircles - 1);
				final float centerX = getCenterXForColumn(currentCell.column);
				final float centerY = getCenterYForRow(currentCell.row);

				final Cell nextCell = pattern.get(numCircles);
				final float dx = percentageOfNextCircle
						* (getCenterXForColumn(nextCell.column) - centerX);
				final float dy = percentageOfNextCircle
						* (getCenterYForRow(nextCell.row) - centerY);
				mInProgressX = centerX + dx;
				mInProgressY = centerY + dy;
			}
			// TODO: Infinite loop here...
			invalidate();
		}

		final float squareWidth = mSquareWidth;
		final float squareHeight = mSquareHeight;

		float radius = mBitmapBtnDefault.getWidth();
//		if (radius < 0.5)
//			radius = (squareWidth * mDiameterFactor * 0.5f);
		final Path currentPath = mCurrentPath;
		currentPath.rewind();

		// TODO: the path should be created and cached every time we hit-detect
		// a cell
		// only the last segment of the path should be computed here
		// draw the path of the pattern (unless the user is in progress, and
		// we are in stealth mode)
		final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);
		if (drawPath) {
			if (mPatternDisplayMode == DisplayMode.Wrong) {
				mPathPaint.setColor(getResources().getColor(R.color.text_red));
			}else {
				mPathPaint.setColor(getResources().getColor(R.color.common_blue));
			}
			boolean anyCircles = false;
			for (int i = 0; i < count; i++) {
				Cell cell = pattern.get(i);

				// only draw the part of the pattern stored in
				// the lookup table (this is only different in the case
				// of animation).
				if (!drawLookup[cell.row][cell.column]) {
					break;
				}
				anyCircles = true;

				float centerX = getCenterXForColumn(cell.column);
				float centerY = getCenterYForRow(cell.row);
				if (i == 0) {
					currentPath.moveTo(centerX, centerY);
				} else {
					currentPath.lineTo(centerX, centerY);
				}
			}

			// add last in progress section
			if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
					&& anyCircles) {
				currentPath.lineTo(mInProgressX, mInProgressY);
			}
			canvas.drawPath(currentPath, mPathPaint);
		}

		// draw the circles
		final int paddingTop = this.getPaddingTop();
		final int paddingLeft = this.getPaddingLeft();

		for (int i = 0; i < 3; i++) {
//			float topY = paddingTop + i * squareHeight;
//			float topY = i*(mBitmapHeight+SystemUtil.dip2px(getContext(),45));
			float topY = getCenterYForRow(i);
			// float centerY = mPaddingTop + i * mSquareHeight + (mSquareHeight
			// / 2);
			for (int j = 0; j < 3; j++) {
//				float leftX = paddingLeft + j * squareWidth;
				float leftX = getCenterXForColumn(j);
//				if (j==0)
//					leftX = mWidth/2 -SystemUtil.dip2px(getContext(), 45) - mBitmapWidth*3/2;
//				else if (j==1)
//					leftX = mWidth/2 - mBitmapWidth/2;
//				else
//					leftX = mWidth/2 + mBitmapWidth/2 + SystemUtil.dip2px(getContext(), 45);
				drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
			}
		}

		// draw the arrows associated with the path (unless the user is in
		// progress, and
		// we are in stealth mode)
		boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
		mPaint.setFilterBitmap(true); // draw with higher quality since we
										// render with transforms
		if (drawPath) {
			for (int i = 0; i < count - 1; i++) {
				Cell cell = pattern.get(i);
				Cell next = pattern.get(i + 1);

				// only draw the part of the pattern stored in
				// the lookup table (this is only different in the case
				// of animation).
				if (!drawLookup[next.row][next.column]) {
					break;
				}

				float leftX = paddingLeft + cell.column * squareWidth;
				float topY = paddingTop + cell.row * squareHeight;

//				drawArrow(canvas, leftX, topY, cell, next);
			}
		}
		mPaint.setFilterBitmap(oldFlag); // restore default flag
	}

	private void drawArrow(Canvas canvas, float leftX, float topY, Cell start,
			Cell end) {
		boolean green = mPatternDisplayMode != DisplayMode.Wrong;
		if (green)
			return;

		final int endRow = end.row;
		final int startRow = start.row;
		final int endColumn = end.column;
		final int startColumn = start.column;

		// offsets for centering the bitmap in the cell
		final int offsetX = ((int) mSquareWidth - mBitmapWidth) / 2;
		final int offsetY = ((int) mSquareHeight - mBitmapHeight) / 2;

		// compute transform to place arrow bitmaps at correct angle inside
		// circle.
		// This assumes that the arrow image is drawn at 12:00 with it's top
		// edge
		// coincident with the circle bitmap's top edge.
		Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
		Matrix matrix = new Matrix();
		final int cellWidth = mBitmapCircleGreen.getWidth();
		final int cellHeight = mBitmapCircleGreen.getHeight();

		// the up arrow bitmap is at 12:00, so find the rotation from x axis and
		// add 90 degrees.
		final float theta = (float) Math.atan2((double) (endRow - startRow),
				(double) (endColumn - startColumn));
		final float angle = (float) Math.toDegrees(theta) + 90.0f;

		// compose matrix
		matrix.setTranslate(leftX + offsetX, topY + offsetY); // transform to
																// cell position
		matrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f); // rotate
																		// about
																		// cell
																		// center
		matrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, 0.0f); // translate
																			// to
																			// 12:00
																			// pos
		canvas.drawBitmap(arrow, matrix, mPaint);
	}

	/**
	 * @param canvas
	 * @param leftX
	 * @param topY
	 * @param partOfPattern
	 *            Whether this circle is part of the pattern.
	 */
	private void drawCircle(Canvas canvas, int leftX, int topY,
			boolean partOfPattern) {
		Bitmap outerCircle;
		Bitmap innerCircle = null;

		if (!partOfPattern
				|| (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
			// unselected circle
			outerCircle = mBitmapCircleDefault;
			innerCircle = mBitmapBtnDefault;
		} else if (mPatternInProgress) {
			// user is in middle of drawing a pattern
			outerCircle = mBitmapCircleGreen;
			innerCircle = mBitmapBtnTouched;
		} else if (mPatternDisplayMode == DisplayMode.Wrong) {
			// the pattern is wrong
			outerCircle = mBitmapCircleRed;
//			innerCircle = mBitmapBtnDefault;
		} else if (mPatternDisplayMode == DisplayMode.Correct
				|| mPatternDisplayMode == DisplayMode.Animate) {
			// the pattern is correct
			outerCircle = mBitmapCircleGreen;
//			innerCircle = mBitmapBtnDefault;
		} else {
			throw new IllegalStateException("unknown display mode "
					+ mPatternDisplayMode);
		}

//		final int width = mBitmapWidth;
//		final int height = mBitmapHeight;

//		final float squareWidth = mSquareWidth;
//		final float squareHeight = mSquareHeight;
//
//		int offsetX = (int) ((squareWidth - width) / 2f);
//		int offsetY = (int) ((squareHeight - height) / 2f);
//		int offsetX2 = (int) ((squareWidth - mBitmapBtnDefault.getWidth()) / 2f);
//		int offsetY2 = (int) ((squareHeight - mBitmapBtnDefault.getHeight()) / 2f);
		if (innerCircle != null)
			canvas.drawBitmap(innerCircle, leftX - innerCircle.getWidth()/2, topY - innerCircle.getHeight()/2, mPaint);
		if (outerCircle != null)
			canvas.drawBitmap(outerCircle, leftX - mBitmapWidth/2, topY - mBitmapHeight/2, mPaint);

	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState,
				LockPatternUtil.patternToString(mPattern),
				mPatternDisplayMode.ordinal(), mInputEnabled, mInStealthMode,
				mTactileFeedbackEnabled);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setPattern(DisplayMode.Correct,
				LockPatternUtil.stringToPattern(ss.getSerializedPattern()));
		mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
		mInputEnabled = ss.isInputEnabled();
		mInStealthMode = ss.isInStealthMode();
		mTactileFeedbackEnabled = ss.isTactileFeedbackEnabled();
	}

	/**
	 * The parecelable for saving and restoring a lock pattern view.
	 */
	private static class SavedState extends BaseSavedState {

		private final String mSerializedPattern;
		private final int mDisplayMode;
		private final boolean mInputEnabled;
		private final boolean mInStealthMode;
		private final boolean mTactileFeedbackEnabled;

		/**
		 * Constructor called from {@link LockPatternView#onSaveInstanceState()}
		 */
		private SavedState(Parcelable superState, String serializedPattern,
				int displayMode, boolean inputEnabled, boolean inStealthMode,
				boolean tactileFeedbackEnabled) {
			super(superState);
			mSerializedPattern = serializedPattern;
			mDisplayMode = displayMode;
			mInputEnabled = inputEnabled;
			mInStealthMode = inStealthMode;
			mTactileFeedbackEnabled = tactileFeedbackEnabled;
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(Parcel in) {
			super(in);
			mSerializedPattern = in.readString();
			mDisplayMode = in.readInt();
			mInputEnabled = (Boolean) in.readValue(null);
			mInStealthMode = (Boolean) in.readValue(null);
			mTactileFeedbackEnabled = (Boolean) in.readValue(null);
		}

		public String getSerializedPattern() {
			return mSerializedPattern;
		}

		public int getDisplayMode() {
			return mDisplayMode;
		}

		public boolean isInputEnabled() {
			return mInputEnabled;
		}

		public boolean isInStealthMode() {
			return mInStealthMode;
		}

		public boolean isTactileFeedbackEnabled() {
			return mTactileFeedbackEnabled;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(mSerializedPattern);
			dest.writeInt(mDisplayMode);
			dest.writeValue(mInputEnabled);
			dest.writeValue(mInStealthMode);
			dest.writeValue(mTactileFeedbackEnabled);
		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
