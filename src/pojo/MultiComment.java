package pojo;

//专门用来存储多行注释的行列信息,/*存放在奇数位置,*/存放在偶数位置
@SuppressWarnings("all")
public class MultiComment {
    private int row;//多行注释的行号
    private int column;//多行注释的列号
    private String comment;//注释

    //构造方法
    public MultiComment(int row,int column,String comment) {
        this.row = row;
        this.column = column;
        this.comment = comment;
    }

    //生成相应的get  set方法
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return comment;
    }
    public void setRow(int row) {
        this.row = row;
    }
    public int getRow() {
        return row;
    }
    public void setColumn(int column) {
        this.column = column;
    }
    public int getColumn() {
        return column;
    }
}
