package pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * 定义tocken序列的实体类
 */
@SuppressWarnings("all")
public class TokenList {

    private List<String> tokenList = new ArrayList<>();//用一个arraylist来存储token序列
    private int row;//记录每一行的记号
    private List<String> identifier;//为语法分析做准备存储的记号类型

    //第一次实验词法分析所用到的构造函数
    public TokenList(int row,List<String> tokenList) {
        this.row = row;
        this.tokenList = tokenList;
    }

    //生成相应的get，set方法
    public void setRow(int row) {
        this.row = row;
    }
    public int getRow() {
        return row;
    }
    public void setTokenList(List<String> tokenList) {
        this.tokenList = tokenList;
    }
    public List<String> getTokenList() {
        return tokenList;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
