package papilio.layout;

import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;

/**
 * A table-like layout that positions children on an explicit unit grid.
 * <p>
 * Elements can be placed at {@code (unitX, unitY)} coordinates and may span
 * multiple horizontal and/or vertical grid units. Any unit positions that are
 * not directly occupied are treated as empty cells for layout purposes so the
 * grid preserves its unit structure.
 */
public class Grid extends Table{
    /** Grid dimensions in units. */
    protected int unitsX, unitsY;

    private final Seq<GridItem<?>> items = new Seq<>();

    public Grid(){
        super();
    }

    public Grid(Drawable background){
        super(background);
    }

    /** Sets the minimum grid size in units. Actual size may grow as elements are added. */
    public Grid units(int x, int y){
        this.unitsX = Math.max(this.unitsX, Math.max(0, x));
        this.unitsY = Math.max(this.unitsY, Math.max(0, y));
        invalidateHierarchy();
        return this;
    }

    /** Adds an element at the given unit coordinates with a size of 1x1 units. */
    public <T extends Element> GridItem<T> add(T element, int x, int y){
        return add(element, x, y, 1, 1);
    }

    /**
     * Adds an element at the given unit coordinates.
     * @param x horizontal unit index (column)
     * @param y vertical unit index (row), top-to-bottom
     * @param w number of horizontal units to span
     * @param h number of vertical units to span
     */
    public <T extends Element> GridItem<T> add(T element, int x, int y, int w, int h){
        if(x < 0 || y < 0) throw new IllegalArgumentException("Grid coordinates must be >= 0: " + x + "," + y);
        if(w < 1 || h < 1) throw new IllegalArgumentException("Grid spans must be >= 1: " + w + "," + h);

        GridItem<T> item = new GridItem<>(element, x, y, w, h);
        items.add(item);
        unitsX = Math.max(unitsX, x + w);
        unitsY = Math.max(unitsY, y + h);

        if(element != null){
            element.remove();
            addChild(element);
        }

        invalidateHierarchy();
        return item;
    }

    /** Adds an element at the next free 1x1 unit. */
    public <T extends Element> GridItem<T> addFree(T element){
        int[] pos = findNextFree(1, 1);
        return add(element, pos[0], pos[1], 1, 1);
    }

    /** No-op for coordinate grids; kept for API compatibility. */
    @Override
    public Table row(){
        return this;
    }

    @Override
    public void clearChildren(){
        items.clear();
        unitsX = 0;
        unitsY = 0;
        super.clearChildren();
    }

    @Override
    public void reset(){
        items.clear();
        unitsX = 0;
        unitsY = 0;
        super.reset();
    }

    @Override
    public boolean removeChild(Element element, boolean unfocus){
        if(!super.removeChild(element, unfocus)) return false;
        for(int i = items.size - 1; i >= 0; i--){
            if(items.get(i).target == element){
                items.remove(i);
                invalidateHierarchy();
                break;
            }
        }
        return true;
    }

    private int[] findNextFree(int w, int h){
        for(int y = 0; y <= unitsY; y++){
            for(int x = 0; x <= unitsX; x++){
                if(canPlace(x, y, w, h)) return new int[]{x, y};
            }
        }
        return new int[]{0, unitsY};
    }

    private boolean canPlace(int x, int y, int w, int h){
        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(x < e.gridX + e.spanW && x + w > e.gridX && y < e.gridY + e.spanH && y + h > e.gridY) return false;
        }
        return true;
    }

    @Override
    public float getPrefWidth(){
        if(sizeInvalid) computeSize();
        float width = tablePrefWidth;
        if(getBackground() != null) return Math.max(width, getBackground().getMinWidth());
        return width;
    }

    @Override
    public float getPrefHeight(){
        if(sizeInvalid) computeSize();
        float height = tablePrefHeight;
        if(getBackground() != null) return Math.max(height, getBackground().getMinHeight());
        return height;
    }

    @Override
    public float getMinWidth(){
        if(sizeInvalid) computeSize();
        return tableMinWidth;
    }

    @Override
    public float getMinHeight(){
        if(sizeInvalid) computeSize();
        return tableMinHeight;
    }

    @Override
    public void layout(){
        if(sizeInvalid) computeSize();

        layoutGrid(0, 0, getWidth(), getHeight());

        // round for crisp rendering and record final bounds
        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.target == null) continue;
            float w = Math.round(e.elementWidth);
            float h = Math.round(e.elementHeight);
            float x = Math.round(e.elementX);
            float y = Math.round(e.elementY);
            e.elementX = x;
            e.elementY = y;
            e.elementWidth = w;
            e.elementHeight = h;
            e.target.setBounds(x, y, w, h);
        }

        Seq<Element> children = getChildren();
        for(int i = 0, n = children.size; i < n; i++){
            children.get(i).validate();
        }
    }

    @Override
    protected void computeSize(){
        sizeInvalid = false;

        int cols = Math.max(unitsX, 0);
        int rows = Math.max(unitsY, 0);
        float[] colMin = new float[cols];
        float[] colPref = new float[cols];
        float[] rowMin = new float[rows];
        float[] rowPref = new float[rows];

        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.spanW == 1){
                float hpad = e.padLeft + e.padRight;
                colMin[e.gridX] = Math.max(colMin[e.gridX], e.minWidth() + hpad);
                colPref[e.gridX] = Math.max(colPref[e.gridX], e.prefWidth() + hpad);
            }
            if(e.spanH == 1){
                float vpad = e.padTop + e.padBottom;
                rowMin[e.gridY] = Math.max(rowMin[e.gridY], e.minHeight() + vpad);
                rowPref[e.gridY] = Math.max(rowPref[e.gridY], e.prefHeight() + vpad);
            }
        }

        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.spanW > 1){
                distribute(e.minWidth() + e.padLeft + e.padRight, e.prefWidth() + e.padLeft + e.padRight, colMin, colPref, e.gridX, e.spanW);
            }
            if(e.spanH > 1){
                distribute(e.minHeight() + e.padTop + e.padBottom, e.prefHeight() + e.padTop + e.padBottom, rowMin, rowPref, e.gridY, e.spanH);
            }
        }

        float tMinW = 0, tPrefW = 0, tMinH = 0, tPrefH = 0;
        for(int i = 0; i < cols; i++){ tMinW += colMin[i]; tPrefW += colPref[i]; }
        for(int i = 0; i < rows; i++){ tMinH += rowMin[i]; tPrefH += rowPref[i]; }

        float hpadding = getMarginLeft() + getMarginRight();
        float vpadding = getMarginTop() + getMarginBottom();
        tableMinWidth = tMinW + hpadding;
        tableMinHeight = tMinH + vpadding;
        tablePrefWidth = Math.max(tPrefW + hpadding, tableMinWidth);
        tablePrefHeight = Math.max(tPrefH + vpadding, tableMinHeight);
    }

    private void distribute(float needMin, float needPref, float[] mins, float[] prefs, int start, int span){
        float currentMin = 0, currentPref = 0;
        for(int i = 0; i < span; i++){
            currentMin += mins[start + i];
            currentPref += prefs[start + i];
        }
        float extraMin = needMin - currentMin;
        float extraPref = needPref - currentPref;
        if(extraMin > 0){
            float each = extraMin / span;
            for(int i = 0; i < span; i++) mins[start + i] += each;
        }
        if(extraPref > 0){
            float each = extraPref / span;
            for(int i = 0; i < span; i++) prefs[start + i] += each;
        }
    }

    private void layoutGrid(float layoutX, float layoutY, float layoutWidth, float layoutHeight){
        int cols = Math.max(unitsX, 0);
        int rows = Math.max(unitsY, 0);
        if(cols == 0 || rows == 0) return;

        float padLeft = getMarginLeft();
        float padTop = getMarginTop();
        float hpadding = padLeft + getMarginRight();
        float vpadding = padTop + getMarginBottom();

        float[] colMin = new float[cols];
        float[] colPref = new float[cols];
        float[] rowMin = new float[rows];
        float[] rowPref = new float[rows];
        int[] colExpand = new int[cols];
        int[] rowExpand = new int[rows];

        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.spanW == 1){
                float hpad = e.padLeft + e.padRight;
                colMin[e.gridX] = Math.max(colMin[e.gridX], e.minWidth() + hpad);
                colPref[e.gridX] = Math.max(colPref[e.gridX], e.prefWidth() + hpad);
            }
            if(e.spanH == 1){
                float vpad = e.padTop + e.padBottom;
                rowMin[e.gridY] = Math.max(rowMin[e.gridY], e.minHeight() + vpad);
                rowPref[e.gridY] = Math.max(rowPref[e.gridY], e.prefHeight() + vpad);
            }
            if(e.expandX != 0){
                for(int c = e.gridX; c < e.gridX + e.spanW; c++) if(colExpand[c] == 0) colExpand[c] = e.expandX;
            }
            if(e.expandY != 0){
                for(int r = e.gridY; r < e.gridY + e.spanH; r++) if(rowExpand[r] == 0) rowExpand[r] = e.expandY;
            }
        }
        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.spanW > 1) distribute(e.minWidth() + e.padLeft + e.padRight, e.prefWidth() + e.padLeft + e.padRight, colMin, colPref, e.gridX, e.spanW);
            if(e.spanH > 1) distribute(e.minHeight() + e.padTop + e.padBottom, e.prefHeight() + e.padTop + e.padBottom, rowMin, rowPref, e.gridY, e.spanH);
        }

        float totalMinW = 0, totalPrefW = 0, totalMinH = 0, totalPrefH = 0;
        for(int i = 0; i < cols; i++){ totalMinW += colMin[i]; totalPrefW += colPref[i]; }
        for(int i = 0; i < rows; i++){ totalMinH += rowMin[i]; totalPrefH += rowPref[i]; }

        float[] colW = new float[cols];
        float[] rowH = new float[rows];

        float extraW = Math.max(0, layoutWidth - hpadding - totalMinW);
        float growW = Math.max(0, totalPrefW - totalMinW);
        if(growW > 0){
            float give = Math.min(extraW, growW);
            for(int i = 0; i < cols; i++){
                float g = colPref[i] - colMin[i];
                colW[i] = colMin[i] + (g / growW) * give;
            }
        }else{
            for(int i = 0; i < cols; i++) colW[i] = colMin[i];
        }

        float extraH = Math.max(0, layoutHeight - vpadding - totalMinH);
        float growH = Math.max(0, totalPrefH - totalMinH);
        if(growH > 0){
            float give = Math.min(extraH, growH);
            for(int i = 0; i < rows; i++){
                float g = rowPref[i] - rowMin[i];
                rowH[i] = rowMin[i] + (g / growH) * give;
            }
        }else{
            for(int i = 0; i < rows; i++) rowH[i] = rowMin[i];
        }

        distributeExpand(colW, colExpand, layoutWidth - hpadding, cols);
        distributeExpand(rowH, rowExpand, layoutHeight - vpadding, rows);

        float tableW = hpadding, tableH = vpadding;
        for(int i = 0; i < cols; i++) tableW += colW[i];
        for(int i = 0; i < rows; i++) tableH += rowH[i];

        int talign = getAlign();
        float x = layoutX + padLeft;
        if((talign & Align.right) != 0) x += layoutWidth - tableW;
        else if((talign & Align.left) == 0) x += (layoutWidth - tableW) / 2f;

        float y = layoutY + padTop;
        if((talign & Align.bottom) != 0) y += layoutHeight - tableH;
        else if((talign & Align.top) == 0) y += (layoutHeight - tableH) / 2f;

        float[] colX = new float[cols];
        float[] rowY = new float[rows];
        float cx = x;
        for(int i = 0; i < cols; i++){ colX[i] = cx; cx += colW[i]; }
        float cy = y;
        for(int i = 0; i < rows; i++){ rowY[i] = cy; cy += rowH[i]; }

        float totalH = getHeight();
        for(int i = 0; i < items.size; i++){
            GridItem<?> e = items.get(i);
            if(e.target == null) continue;

            float cellX = colX[e.gridX];
            float cellY = rowY[e.gridY];
            float cellW = 0, cellH = 0;
            for(int c = e.gridX; c < e.gridX + e.spanW; c++) cellW += colW[c];
            for(int r = e.gridY; r < e.gridY + e.spanH; r++) cellH += rowH[r];

            float innerW = Math.max(0, cellW - e.padLeft - e.padRight);
            float innerH = Math.max(0, cellH - e.padTop - e.padBottom);

            float ew = e.prefWidth();
            float eh = e.prefHeight();
            float minW = e.minWidth(), minH = e.minHeight();
            float maxW = e.maxWidth(), maxH = e.maxHeight();
            if(ew < minW) ew = minW;
            if(eh < minH) eh = minH;
            if(maxW > 0 && ew > maxW) ew = maxW;
            if(maxH > 0 && eh > maxH) eh = maxH;

            if(e.fillX > 0){
                ew = Math.max(innerW * e.fillX, minW);
                if(maxW > 0) ew = Math.min(ew, maxW);
            }
            if(e.fillY > 0){
                eh = Math.max(innerH * e.fillY, minH);
                if(maxH > 0) eh = Math.min(eh, maxH);
            }
            ew = Math.min(ew, innerW);
            eh = Math.min(eh, innerH);
            if(ew < 0) ew = 0;
            if(eh < 0) eh = 0;

            int a = e.align;
            float ex = cellX + e.padLeft;
            if((a & Align.right) != 0) ex = cellX + cellW - e.padRight - ew;
            else if((a & Align.left) == 0) ex = cellX + e.padLeft + (innerW - ew) / 2f;

            float ey = cellY + e.padTop;
            if((a & Align.bottom) != 0) ey = cellY + cellH - e.padBottom - eh;
            else if((a & Align.top) == 0) ey = cellY + e.padTop + (innerH - eh) / 2f;

            float drawY = totalH - ey - eh;
            e.elementX = ex;
            e.elementY = drawY;
            e.elementWidth = ew;
            e.elementHeight = eh;
            e.target.setBounds(ex, drawY, ew, eh);
        }
    }

    private void distributeExpand(float[] sizes, int[] expand, float available, int count){
        float used = 0;
        int total = 0;
        for(int i = 0; i < count; i++){ used += sizes[i]; total += expand[i]; }
        if(total <= 0) return;
        float extra = available - used;
        if(extra <= 0) return;
        float added = 0;
        int last = -1;
        for(int i = 0; i < count; i++){
            if(expand[i] == 0) continue;
            float a = extra * ((float)expand[i] / total);
            sizes[i] += a;
            added += a;
            last = i;
        }
        if(last >= 0) sizes[last] += extra - added;
    }
}
