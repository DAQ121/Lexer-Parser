package entity;

/**
 * 用于存储单行注释的信息
 */
@SuppressWarnings("all")
public class SingleComment {

    //相关定义
    private int row;//行
    private int column;//列

    //构造函数
    public SingleComment(int row, int column) {
        this.row = row;
        this.column = column;
    }
    //生成相应的 get-set方法
    public int getRow() {
        return row;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getColumn() {
        return column;
    }
    public void setColumn(int column) {
        this.column = column;
    }

}
