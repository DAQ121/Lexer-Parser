package frame;

import java.awt.*;

/**
 * 输入区域，读取文本域的信息
 */
public class ReadText extends TextArea{
    public ReadText(int rows,int columns) {
        setBackground(Color.white);
        setForeground(Color.black);
        setRows(rows);
        setColumns(columns);
    }
}
