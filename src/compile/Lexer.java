package compile;

import entity.MultiComment;
import entity.SingleComment;
import entity.TokenList;
import frame.OutText;
import frame.ReadText;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @description 这是词法分析的具体实现
 */
@SuppressWarnings("all")
public class Lexer extends MainComplier{

    //词法分析解释规则,定义字符数组
    String[] keyWords = {"if", "else", "while", "read", "write", "int", "double","for"};  //关键字数组
    String[] operator = {"+", "-", "*", "/"};//运算符数组
    String[] roperator = {">", "<", "==", "<>"};//关系运算符数组
    String[] sepretor = {";", "{", "}", "(", ")", "."};//分隔符数组
    String RegexToId = "^[a-zA-Z]([a-zA-Z_0-9])*[a-zA-Z0-9]$||[a-zA-Z]";//标识符的正则表达式
    String RegexToNumber = "^^-?\\d+$";//整数的正则表达式
    String RegexToFloat = "^(-?\\d+)(\\.\\d+)?$";//浮点数的正则表达式
    String RegexToArray = "[a-zA-Z]+(\\[[0-9][1-9]*\\])+";//数组变量的正则表达式

    //将父类的readText, outText继承过来
    public Lexer(ReadText readText, OutText outText) throws HeadlessException {
        super(readText, outText);
    }

    //分析过程，此处为语法分析和词法分析提供已经修饰过的源程序
    public List<TokenList> getTokens() {
        List<TokenList> tokenLists = new ArrayList<>();//用于记录Token的信息
        String inputText = readText.getText();
        StringTokenizer totalStrt = new StringTokenizer(inputText, "\r\n");
        int row = 0;//行号
        //获取所有的记号以及记号的信息
        while (totalStrt.hasMoreTokens()) {
            List<String> Tokens = new ArrayList<>();//行记号
            StringTokenizer rowOfStrt = new StringTokenizer(totalStrt.nextToken(), " \n\r\t;(){}\"\'+-<>/=*", true);
            //所有可能的界符，初步得到所有的Token,但需要进一步的合并
            while (rowOfStrt.hasMoreTokens()) {
                Tokens.add(rowOfStrt.nextToken());
            }
            TokenList tokenList = new TokenList(row, Tokens);
            tokenLists.add(tokenList);
            row++;
        }
        //对于初步得到的记号集合的进一步判断与整合,用于区别注释和*,/；以及=与==,以及<与<>
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//获取行记号组
            for (int j = 0; j < tokenList.size() - 1; j++) {
                if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("/")) {
                    //单行注释记号的识别
                    tokenList.set(j, "//");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("*")) {
                    //多行注释的识别
                    tokenList.set(j, "/*");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("*") && tokenList.get(j + 1).equals("/")) {
                    //多行注释的识别
                    tokenList.set(j, "*/");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("=") && tokenList.get(j + 1).equals("=")) {
                    tokenList.set(j, "==");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("<") && tokenList.get(j + 1).equals(">")) {
                    tokenList.set(j, "<>");
                    tokenList.remove(j + 1);//判断不等于符号
                }
            }
        }
        //第二次对记号进行判断整合，主要用于去除各种分隔符
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//获取行记号组
            String Pattern = "\\s+|\t|\r\n";
            int j = 0;
            while(j<tokenList.size())
            {
                if(tokenList.get(j).matches(Pattern))
                {
                    tokenList.remove(j);
                }
                else
                {
                    j++;
                }
            }
        }
        //第三次对记号进行去除注释，得到真正的完整的记号
        List<MultiComment> multiComments = new ArrayList<>();//存放多行注释的位置信息
        List<SingleComment> singleComments = new ArrayList<>();//存放单行注释的位置信息
        for (int i = 0; i < tokenLists.size(); i++)//多行注释的记号获取
        {
            List<String> TokenOfrow = tokenLists.get(i).getTokenList();
            int rowCount = tokenLists.get(i).getRow();//多行注释行号
            for (int j = 0; j < TokenOfrow.size(); j++) {
                if (TokenOfrow.get(j).equals("//")) {
                    SingleComment singleComment = new SingleComment(rowCount, j);
                    singleComments.add(singleComment);//记录单行注释位置
                }
                if (TokenOfrow.get(j).equals("/*")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "/*");//j为列号
                    multiComments.add(multiComment);
                } else if (TokenOfrow.get(j).equals("*/")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "*/");
                    multiComments.add(multiComment);
                }
            }
        }
        for (int i = 0; i < multiComments.size(); i = i + 2)//去除多行注释中的整行注释
        {
            if ((multiComments.size() % 2) == 0 && i <= multiComments.size() - 2)//判断注释是否未闭合
            {
                if (multiComments.get(i).getComment().equals("/*") && multiComments.get(i + 1).getComment().equals("*/")) {
                    for (int j = multiComments.get(i).getRow() + 1; j < multiComments.get(i + 1).getRow(); j++) {
                        tokenLists.remove(j);
                    }
                    List<String> StartLine = tokenLists.get(multiComments.get(i).getRow()).getTokenList();//注释行起始
                    List<String> EndLine = tokenLists.get(multiComments.get(i + 1).getRow()).getTokenList();//注释行结束
                    for (int j = multiComments.get(i).getColumn(); j < StartLine.size(); )//因为随着元素的删除减少，size大小也会发生改变
                    {
                        StartLine.remove(j);
                    }
                    int position = multiComments.get(i).getColumn();//位置指针
                    for (int j = 0; j <= position; )//同理，元素的数量的减少导致size改变
                    {
                        EndLine.remove(j);
                        position--;
                    }
                }
            } else {
                outText.append("无法继续分析");
                outText.append("第" + multiComments.get(i).getRow() + "行第" + multiComments.get(i).getColumn() + "处的注释未闭合");
                break;
            }
        }
        for (int i = 0; i < singleComments.size(); i++) {
            List<String> SignleLine = tokenLists.get(singleComments.get(i).getRow()).getTokenList();
            for (int j = singleComments.get(i).getColumn(); j < SignleLine.size(); ) {
                SignleLine.remove(j);//去除单行注释
            }
        }
        return tokenLists;
    }

    //所有的记号处理都做好，此处纯分析记号
    public void Analysis() {
        List<TokenList> tokenLists = getTokens();
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();
            outText.append("--------------------------------------------------分析第" + (i + 1) + "行--------------------------------------------------" + "\r\n");
            for (int j = 0; j < tokenList.size(); j++) {
                int Count = 0;
                for (int k = 0; k < keyWords.length; k++) {
                    if (tokenList.get(j).equals(keyWords[k])) {
                        outText.append(tokenList.get(j) + " 是关键字" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < operator.length; k++) {
                    if (tokenList.get(j).equals(operator[k])) {
                        outText.append(tokenList.get(j) + " 是运算符" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < roperator.length; k++) {
                    if (tokenList.get(j).equals(roperator[k])) {
                        outText.append(tokenList.get(j) + " 是关系运算符" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < sepretor.length; k++) {
                    if (tokenList.get(j).equals(sepretor[k])) {
                        outText.append(tokenList.get(j) + " 是分隔符" + "\r\n");
                        Count++;
                    }
                }
                if (tokenList.get(j).matches(RegexToId) && (Count == 0)) {
                    outText.append(tokenList.get(j) + " 是标识符" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToNumber)) {
                    outText.append(tokenList.get(j) + " 是整数" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToFloat)) {
                    outText.append(tokenList.get(j) + " 是浮点数" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToArray)) {
                    outText.append(tokenList.get(j) + " 是数组变量" + "\r\n");
                } else if (tokenList.get(j).equals("=")) {
                    outText.append(tokenList.get(j) + " 是等于号" + "\r\n");
                } else if (Count == 0) {
                    outText.append(tokenList.get(j) + " 标识符命名错误" + "\r\n");
                }
            }
        }
    }
}
