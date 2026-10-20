package algorithms;

public final class SccResult {
    private final int[] component;
    private final int count;

    public SccResult(int[] component, int count){
        this.component = component.clone();
        this.count = count;
    }
    public int componentOf(int v){
        return component[v];
    }
    public int count(){
        return count;
    }
    public int size(){
        return component.length;
    }
}