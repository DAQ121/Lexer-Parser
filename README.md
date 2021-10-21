# :gift:Lexer-Parser
用java实现的词法分析和语法分析的小程序:flags:

## 概述
- **词法分析：** 根据输入的字符序列，将字符序列转换为单词`Token`序列，识别每个字符，并给出相应的类型
- **语法分析：** 根据给定的文法，判断是否是`LL(1)文法`，自顶向下分析。采用预测分析法：从文法开始符S 出发，从左到右扫描源程序，每次通过向前查看 1 个字符，选择合适的产生式，生成句子的最左推导。
- **步骤：**   
1. 从文件读入文法自动区分 终结符号`VT` 和 非终结符号`VN`
2. 消除直接左递归
3. 生成`FIRST`和`FOLLOW`集合
4. 判断是否是`LL(1)`文法
5. 构建预测分析表
6.  输入要分析的单词串自动输出分析过程



## :tv:结果图
- **词法分析**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200611141000270.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)
- **语法分析**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200611141009521.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200611141013882.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)

- **判断是否是LL（1）文法**
 
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200611141023906.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)




## 代码结构

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200610162702789.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)


## 使用说明

- 将`grammer`文件夹放入F盘下，我在代码包中也附带了一个名为`文法测试.txt`的文件，里面有对应的四种文法以及需要输入的字符串示例。
- 运行Main主函数，即可...

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200611141035997.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)






