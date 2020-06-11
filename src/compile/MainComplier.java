package compile;

import frame.OutText;
import frame.ReadText;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * @description 这是主运行窗口
 */
@SuppressWarnings("all")
public class MainComplier extends JFrame {

    //UI组件,运行窗口一些按钮
    JButton jButton1 = new JButton("词法分析");
    JButton jButton2 = new JButton("清空");
    JButton jButton3 = new JButton("文法1");
    JButton jButton4 = new JButton("文法2");
    JButton jButton5 = new JButton("文法3");
    JButton jButton6 = new JButton("文法4");
    JLabel inLabel = new JLabel("待分析文件");
    JLabel outLabel = new JLabel("分析结果");
    ReadText readText = new ReadText(7, 65);//输入区
    OutText outText = new OutText(30, 65);//输出区

    //供子类继承使用
    public MainComplier(ReadText readText, OutText outText) throws HeadlessException {
        this.readText = readText;
        this.outText = outText;
    }
    //事件监听器
    public void initListener() {
        ActListener actListener = new ActListener();
        jButton1.addActionListener(actListener);
        jButton2.addActionListener(actListener);
        jButton3.addActionListener(actListener);
        jButton4.addActionListener(actListener);
        jButton5.addActionListener(actListener);
        jButton6.addActionListener(actListener);
    }
    //构建事件监听器
    class ActListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jButton1) {
                new Lexer(readText, outText).Analysis();
            } else if (e.getSource() == jButton2) {
                readText.setText("");
                outText.setText("");
            } else if (e.getSource() == jButton3) {
               new Parser(readText, outText,1).Main();
            }
            else if (e.getSource() == jButton4) {
                new Parser(readText, outText,2).Main();
            }
            else if (e.getSource() == jButton5) {
                new Parser(readText, outText,3).Main();
            }
            else if (e.getSource() == jButton6) {
                new Parser(readText, outText,4).Main();
            }
        }
    }
    //主窗口
    public MainComplier() throws IOException {
        super("词法分析器");
        setSize(550, 730);
        this.setLocation(320, 70);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));//中间对齐
        this.add(this.inLabel);
        this.add(this.readText);
        this.add(this.outLabel);
        this.add(this.outText);
        this.add(this.jButton1);
        this.add(this.jButton2);
        this.add(this.jButton3);
        this.add(this.jButton4);
        this.add(this.jButton5);
        this.add(this.jButton6);
        initListener();
        this.setVisible(true);//设置窗口可输入文本
    }
}