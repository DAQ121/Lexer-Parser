package window;

import java.awt.*;

/**
 * 输出域，将词法分析结果或者语法分析结果输出到这个域中
 */
public class OutText extends TextArea{
    public OutText(int rows,int columns) {
        setBackground(Color.white);
        setForeground(Color.black);
        setRows(rows);
        setColumns(columns);
        setFont(new Font("Courier",1,12));
    }
}
