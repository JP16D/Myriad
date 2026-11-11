package papilio.layout;

import arc.func.Cons;
import arc.scene.Element;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.util.Align;

/**
 * A grid layout item/cell that carries explicit unit coordinates and spans.
 * <p>
 * Uses package-private cover-up fields so Grid can access layout constraints
 * while keeping Cell's original fields hidden from papilio.layout use.
 */
public class GridItem<T extends Element> extends Cell<T>{
    static final float UNSET = Float.NEGATIVE_INFINITY;

    // cover-up fields (package-private for papilio.layout access)
    float minWidth = UNSET, minHeight = UNSET;
    float maxWidth = UNSET, maxHeight = UNSET;
    float padTop, padLeft, padBottom, padRight;
    float fillX, fillY;
    int align;
    int expandX, expandY;

    final T target;
    int gridX, gridY, spanW, spanH;

    float elementX, elementY, elementWidth, elementHeight;

    public GridItem(T element, int x, int y, int w, int h){
        this.target = element;
        this.gridX = x;
        this.gridY = y;
        this.spanW = w;
        this.spanH = h;
    }

    @Override
    public T get(){
        return target;
    }

    @Override
    public boolean hasElement(){
        return target != null;
    }

    @Override
    public float prefWidth(){
        float v = target == null ? 0f : target.getPrefWidth();
        float max = maxWidth();
        if(max > 0 && v > max) v = max;
        float min = minWidth();
        if(v < min) v = min;
        return v;
    }

    @Override
    public float prefHeight(){
        float v = target == null ? 0f : target.getPrefHeight();
        float max = maxHeight();
        if(max > 0 && v > max) v = max;
        float min = minHeight();
        if(v < min) v = min;
        return v;
    }

    @Override
    public float minWidth(){
        if(minWidth != UNSET) return minWidth;
        return target == null ? 0f : target.getMinWidth();
    }

    @Override
    public float minHeight(){
        if(minHeight != UNSET) return minHeight;
        return target == null ? 0f : target.getMinHeight();
    }

    @Override
    public float maxWidth(){
        if(maxWidth != UNSET) return maxWidth;
        return target == null ? 0f : target.getMaxWidth();
    }

    @Override
    public float maxHeight(){
        if(maxHeight != UNSET) return maxHeight;
        return target == null ? 0f : target.getMaxHeight();
    }

	@SuppressWarnings("all")
    float scl(float v){
        return Scl.scl(v);
    }

    @Override
    public GridItem<T> size(float size){
        minWidth = minHeight = maxWidth = maxHeight = scl(size);
        return this;
    }

    @Override
    public GridItem<T> size(float width, float height){
        minWidth = maxWidth = scl(width);
        minHeight = maxHeight = scl(height);
        return this;
    }

    @Override
    public GridItem<T> width(float width){
        minWidth = maxWidth = scl(width);
        return this;
    }

    @Override
    public GridItem<T> height(float height){
        minHeight = maxHeight = scl(height);
        return this;
    }

    @Override
    public GridItem<T> pad(float pad){
        padTop = padLeft = padBottom = padRight = scl(pad);
        return this;
    }

    @Override
    public GridItem<T> pad(float top, float left, float bottom, float right){
        padTop = scl(top);
        padLeft = scl(left);
        padBottom = scl(bottom);
        padRight = scl(right);
        return this;
    }

    @Override
    public GridItem<T> fill(){
        fillX = 1f;
        fillY = 1f;
        return this;
    }

    @Override
    public GridItem<T> fillX(){
        fillX = 1f;
        return this;
    }

    @Override
    public GridItem<T> fillY(){
        fillY = 1f;
        return this;
    }

    @Override
    public GridItem<T> fill(float x, float y){
        fillX = x;
        fillY = y;
        return this;
    }

    @Override
    public GridItem<T> align(int align){
        this.align = align;
        return this;
    }

    @Override
    public GridItem<T> center(){
        align = Align.center;
        return this;
    }

    @Override
    public GridItem<T> expand(){
        expandX = 1;
        expandY = 1;
        return this;
    }

    @Override
    public GridItem<T> expandX(){
        expandX = 1;
        return this;
    }

    @Override
    public GridItem<T> expandY(){
        expandY = 1;
        return this;
    }

    @Override
    public GridItem<T> grow(){
        expandX = 1;
        expandY = 1;
        fillX = 1f;
        fillY = 1f;
        return this;
    }

    @Override
    public GridItem<T> growX(){
        expandX = 1;
        fillX = 1f;
        return this;
    }

    @Override
    public GridItem<T> growY(){
        expandY = 1;
        fillY = 1f;
        return this;
    }

    @Override
    public GridItem<T> update(Cons<T> updater){
        if(target != null){
            T t = target;
            target.update(() -> updater.get(t));
        }
        return this;
    }

    @Override
    public GridItem<T> self(Cons<Cell<T>> cons){
        cons.get(this);
        return this;
    }

    @Override
    public GridItem<T> with(Cons<T> cons){
        if(target != null) cons.get(target);
        return this;
    }

    /** Sets grid span (in units). */
    public GridItem<T> span(int w, int h){
        this.spanW = Math.max(1, w);
        this.spanH = Math.max(1, h);
        return this;
    }
}
