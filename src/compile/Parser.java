package compile;

import frame.OutText;
import frame.ReadText;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.PublicKey;
import java.util.*;

/**
 * @description 这是词法分析的具体实现
 */
@SuppressWarnings("all")
public class Parser extends MainComplier{
    //语法分析的规则
    public static final String PATH = "./grammar2";// 文法
    private static String START; // 开始符号
    private static HashSet<String> VN, VT; // 非终结符号集、终结符号集
    private static HashMap<String, ArrayList<ArrayList<String>>> MAP;// key:产生式左边 value:产生式右边(含多条)
    private static HashMap<String, String> oneLeftFirst;// "|" 分开的单条产生式对应的FIRST集合,用于构建预测分析表
    private static HashMap<String, HashSet<String>> FIRST, FOLLOW; // FIRST、FOLLOW集合
    private static String[][] FORM; // 存放预测分析表的数组，用于输出
    private static HashMap<String, String> preMap;// 存放预测分析表的map，用于快速查找
    private int choice;

    //将父类的readText, outText继承过来
    public Parser(ReadText readText, OutText outText, int choice) throws HeadlessException {
        super(readText, outText);
        this.choice=choice;
    }

    //程序入口
    public void Main() {
        init(); // 初始化变量
        identifyVnVt(readFile(new File(PATH)));// 符号分类,并以key-value形式存于MAP中
        reformMap();// 消除左递归和提取左公因子
        findFirst(); // 求FIRST集合
        findFollow(); // 求FOLLOW集合
        if (isLL1()) {
            preForm(); // 构建预测分析表
            printAutoPre(readText.getText());
        }
    }
    // 从文件读文法
    public ArrayList<String> readFile(File file) {
        BufferedReader br = null;
        outText.append("从文件读入的文法为:"+"\r\n");
        ArrayList<String> result = new ArrayList<>();
        try {
            if (choice == 1) {
                br = new BufferedReader(new FileReader("F:/grammer//a1.txt"));
            } else if (choice == 2){
                br = new BufferedReader(new FileReader("F:/grammer//a2.txt"));
            } else if (choice == 3){
                br = new BufferedReader(new FileReader("F:/grammer//a3.txt"));
            } else if (choice == 4){
                br = new BufferedReader(new FileReader("F:/grammer//a4.txt"));
            }
            String s = null;
            while ((s = br.readLine()) != null) {
                outText.append("\t" + s+"\r\n");
                result.add(s.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    // 变量初始化
    private static void init() {
        VN = new HashSet<>();
        VT = new HashSet<>();
        MAP = new HashMap<>();
        FIRST = new HashMap<>();
        FOLLOW = new HashMap<>();
        oneLeftFirst = new HashMap<>();
        preMap = new HashMap<>();
    }
    // 符号分类
    private void identifyVnVt(ArrayList<String> list) {
        START = list.get(0).charAt(0) + "";// 存放开始符号

        for (int i = 0; i < list.size(); i++) {
            String oneline = list.get(i);
            String[] vnvt = oneline.split("→");// 用定义符号分割
            String left = vnvt[0].trim(); // 文法的左边
            VN.add(left);

            // 文法右边
            ArrayList<ArrayList<String>> mapValue = new ArrayList<>();
            ArrayList<String> right = new ArrayList<>();

            for (int j = 0; j < vnvt[1].length(); j++) { // 用 “|”分割右边
                if (vnvt[1].charAt(j) == '|') {
                    VT.addAll(right);
                    mapValue.add(right);
                    // right.clear();// 清空之后，依然是同一个地址，需要重新new对象
                    right = null;
                    right = new ArrayList<>();
                    continue;
                }
                // 如果产生式某字符的左边含有中文或英文的单引号，则视为同一个字符
                if (j + 1 < vnvt[1].length() && (vnvt[1].charAt(j + 1) == '\'' || vnvt[1].charAt(j + 1) == '’')) {
                    right.add(vnvt[1].charAt(j) + "" + vnvt[1].charAt(j + 1));
                    j++;
                } else {
                    right.add(vnvt[1].charAt(j) + "");
                }
            }
            VT.addAll(right);
            mapValue.add(right);

            MAP.put(left, mapValue);
        }
        VT.removeAll(VN); // 从终结字符集中移除非终结符
        // 打印Vn、Vt
        outText.append("\nVn集合:\r\n\t{" + String.join("、", VN.toArray(new String[VN.size()])) + "}"+"\r\n");
        outText.append("Vt集合:\n\t{" + String.join("、", VT.toArray(new String[VT.size()])) + "}"+"\r\n");

    }
    // 消除直接左递归
    private void reformMap() {
        boolean isReForm = false;// MAP是否被修改
        Set<String> keys = new HashSet<>();
        keys.addAll(MAP.keySet());
        Iterator<String> it = keys.iterator();
        ArrayList<String> nullSign = new ArrayList<>();
        nullSign.add("ε");
        while (it.hasNext()) {
            String left = it.next();
            boolean flag = false;// 是否有左递归
            ArrayList<ArrayList<String>> rightList = MAP.get(left);
            ArrayList<String> oldRightCell = new ArrayList<>(); // 旧产生的右边
            ArrayList<ArrayList<String>> newLeftNew = new ArrayList<>();// 存放新的左边和新的右边

            // 消除直接左递归
            for (int i = 0; i < rightList.size(); i++) {
                ArrayList<String> newRightCell = new ArrayList<>(); // 新产生式的右边
                if (rightList.get(i).get(0).equals(left)) {
                    for (int j = 1; j < rightList.get(i).size(); j++) {
                        newRightCell.add(rightList.get(i).get(j));
                    }
                    flag = true;
                    newRightCell.add(left + "\'");
                    newLeftNew.add(newRightCell);
                } else {
                    for (int j = 0; j < rightList.get(i).size(); j++) {
                        oldRightCell.add(rightList.get(i).get(j));
                    }
                    oldRightCell.add(left + "\'");
                }
            }
            // 如果有左递归，则更新MAP
            if (flag) {
                isReForm = true;
                newLeftNew.add(nullSign);
                MAP.put(left + "\'", newLeftNew);
                VN.add(left + "\'"); // 加入新的VN
                VT.add("ε"); // 加入ε到VT
                ArrayList<ArrayList<String>> newLeftOld = new ArrayList<>();// 存放原先，但是产生新的右边
                newLeftOld.add(oldRightCell);
                MAP.put(left, newLeftOld);
            }
        }
        // 如果文法被修改，则输出修改后的文法
        if (isReForm) {
            outText.append("消除文法的左递归:"+"\r\n");
            Set<String> kSet = new HashSet<>(MAP.keySet());
            Iterator<String> itk = kSet.iterator();
            while (itk.hasNext()) {
                String k = itk.next();
                ArrayList<ArrayList<String>> leftList = MAP.get(k);
                outText.append("\t" + k + "→");
                for (int i = 0; i < leftList.size(); i++) {
                    outText.append(String.join("", leftList.get(i).toArray(new String[leftList.get(i).size()])));
                    if (i + 1 < leftList.size()) {
                        outText.append("|");
                    }
                }
                outText.append("\r\n");
            }
        }
    }
    // 求每个非终结符号的FIRST集合 和 分解单个产生式的FIRST集合
    private void findFirst() {
        outText.append("\nFIRST集合:"+"\r\n");
        Iterator<String> it = VN.iterator();
        while (it.hasNext()) {
            HashSet<String> firstCell = new HashSet<>();// 存放单个非终结符号的FIRST
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);
            // System.out.println(key+":");
            // 遍历单个产生式的左边
            for (int i = 0; i < list.size(); i++) {
                ArrayList<String> listCell = list.get(i);// listCell为“|”分割出来
                HashSet<String> firstCellOne = new HashSet<>();// 产生式左边用“ | ”分割的单个式子的First(弃用)
                String oneLeft = String.join("", listCell.toArray(new String[listCell.size()]));
                // System.out.println("oneLeft: "+oneLeft);
                if (VT.contains(listCell.get(0))) {
                    firstCell.add(listCell.get(0));
                    firstCellOne.add(listCell.get(0));
                    oneLeftFirst.put(key + "$" + listCell.get(0), key + "→" + oneLeft);
                } else {
                    boolean[] isVn = new boolean[listCell.size()];// 标记是否有定义为空,如果有则检查下一个字符
                    isVn[0] = true;// 第一个为非终结符号
                    int p = 0;
                    while (isVn[p]) {
                        // System.out.println(p+" "+listCell.size());
                        if (VT.contains(listCell.get(p))) {
                            firstCell.add(listCell.get(p));
                            firstCellOne.add(listCell.get(p));
                            oneLeftFirst.put(key + "$" + listCell.get(p), key + "→" + oneLeft);
                            break;
                        }
                        String vnGo = listCell.get(p);//
                        Stack<String> stack = new Stack<>();
                        stack.push(vnGo);
                        while (!stack.isEmpty()) {
                            ArrayList<ArrayList<String>> listGo = MAP.get(stack.pop());
                            for (int k = 0; k < listGo.size(); k++) {
                                ArrayList<String> listGoCell = listGo.get(k);
                                if (VT.contains(listGoCell.get(0))) { // 如果第一个字符是终结符号
                                    if ("ε".equals(listGoCell.get(0))) {
                                        if (!key.equals(START)) { // 开始符号不能推出空
                                            firstCell.add(listGoCell.get(0));
                                            firstCellOne.add(listGoCell.get(0));
                                            oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "→" + oneLeft);
                                        }
                                        if (p + 1 < isVn.length) {// 如果为空，可以查询下一个字符
                                            isVn[p + 1] = true;
                                        }
                                    } else { // 非空的终结符号加入对应的FIRST集合
                                        firstCell.add(listGoCell.get(0));
                                        firstCellOne.add(listGoCell.get(0));
                                        oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "→" + oneLeft);
                                    }
                                } else {// 不是终结符号，入栈
                                    stack.push(listGoCell.get(0));
                                }
                            }
                        }
                        p++;
                        if (p > isVn.length - 1) {
                            break;
                        }
                    }
                }
                FIRST.put(key + "→" + oneLeft, firstCellOne);
            }
            FIRST.put(key, firstCell);
            // 输出key的FIRST集合
            outText.append(
                    "\tFIRST(" + key + ")={" + String.join("、", firstCell.toArray(new String[firstCell.size()])) + "}"+"\r\n");
        }
    }
    // 求每个非终结符号的FLLOW集合
    private void findFollow() {
        outText.append("\nFOLLOW集合:"+"\r\n");
        Iterator<String> it = VN.iterator();
        HashMap<String, HashSet<String>> keyFollow = new HashMap<>();

        ArrayList<HashMap<String, String>> vn_VnList = new ArrayList<>();// 用于存放/A->...B 或者 A->...Bε的组合

        HashSet<String> vn_VnListLeft = new HashSet<>();// 存放vn_VnList的左边和右边
        HashSet<String> vn_VnListRight = new HashSet<>();
        // 开始符号加入#
        keyFollow.put(START, new HashSet<String>() {
            private static final long serialVersionUID = 1L;
            {
                add(new String("#"));
            }
        });

        while (it.hasNext()) {
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);
            ArrayList<String> listCell;

            // 先把每个VN作为keyFollow的key，之后在查找添加其FOLLOW元素
            if (!keyFollow.containsKey(key)) {
                keyFollow.put(key, new HashSet<>());
            }
            keyFollow.toString();

            for (int i = 0; i < list.size(); i++) {
                listCell = list.get(i);

                // (1)直接找非总结符号后面跟着终结符号
                for (int j = 1; j < listCell.size(); j++) {
                    HashSet<String> set = new HashSet<>();
                    if (VT.contains(listCell.get(j))) {
                        // System.out.println(listCell.get(j - 1) + ":" + listCell.get(j));
                        set.add(listCell.get(j));
                        if (keyFollow.containsKey(listCell.get(j - 1))) {
                            set.addAll(keyFollow.get(listCell.get(j - 1)));
                        }
                        keyFollow.put(listCell.get(j - 1), set);
                    }
                }
                // (2)找...VnVn...组合
                for (int j = 0; j < listCell.size() - 1; j++) {
                    HashSet<String> set = new HashSet<>();
                    if (VN.contains(listCell.get(j)) && VN.contains(listCell.get(j + 1))) {
                        set.addAll(FIRST.get(listCell.get(j + 1)));
                        set.remove("ε");

                        if (keyFollow.containsKey(listCell.get(j))) {
                            set.addAll(keyFollow.get(listCell.get(j)));
                        }
                        keyFollow.put(listCell.get(j), set);
                    }
                }

                // (3)A->...B 或者 A->...Bε(可以有n个ε)的组合存起来
                for (int j = 0; j < listCell.size(); j++) {
                    HashMap<String, String> vn_Vn;
                    if (VN.contains(listCell.get(j)) && !listCell.get(j).equals(key)) {// 是VN且A不等于B
                        boolean isAllNull = false;// 标记VN后是否为空
                        if (j + 1 < listCell.size()) {// 即A->...Bε(可以有n个ε)
                            for (int k = j + 1; k < listCell.size(); k++) {
                                if ((FIRST.containsKey(listCell.get(k)) ? FIRST.get(listCell.get(k)).contains("ε")
                                        : false)) {// 如果其后面的都是VN且其FIRST中包含ε
                                    isAllNull = true;
                                } else {
                                    isAllNull = false;
                                    break;
                                }
                            }
                        }
                        // 如果是最后一个为VN,即A->...B
                        if (j == listCell.size() - 1) {
                            isAllNull = true;
                        }
                        if (isAllNull) {
                            vn_VnListLeft.add(key);
                            vn_VnListRight.add(listCell.get(j));

                            // 往vn_VnList中添加，分存在和不存在两种情况
                            boolean isHaveAdd = false;
                            for (int x = 0; x < vn_VnList.size(); x++) {
                                HashMap<String, String> vn_VnListCell = vn_VnList.get(x);
                                if (!vn_VnListCell.containsKey(key)) {
                                    vn_VnListCell.put(key, listCell.get(j));
                                    vn_VnList.set(x, vn_VnListCell);
                                    isHaveAdd = true;
                                    break;
                                } else {
                                    // 去重
                                    if (vn_VnListCell.get(key).equals(listCell.get(j))) {
                                        isHaveAdd = true;
                                        break;
                                    }
                                    continue;
                                }
                            }
                            if (!isHaveAdd) {// 如果没有添加，表示是新的组合
                                vn_Vn = new HashMap<>();
                                vn_Vn.put(key, listCell.get(j));
                                vn_VnList.add(vn_Vn);
                            }
                        }
                    }
                }
            }
        }

        keyFollow.toString();

        // (4)vn_VnListLeft减去vn_VnListRight,剩下的就是入口产生式，
        vn_VnListLeft.removeAll(vn_VnListRight);
        Queue<String> keyQueue = new LinkedList<>();// 用栈或者队列都行
        Iterator<String> itVnVn = vn_VnListLeft.iterator();
        while (itVnVn.hasNext()) {
            keyQueue.add(itVnVn.next());
        }
        while (!keyQueue.isEmpty()) {
            String keyLeft = keyQueue.poll();
            for (int t = 0; t < vn_VnList.size(); t++) {
                HashMap<String, String> vn_VnListCell = vn_VnList.get(t);
                if (vn_VnListCell.containsKey(keyLeft)) {
                    HashSet<String> set = new HashSet<>();
                    // 原来的FOLLOW加上左边的FOLLOW
                    if (keyFollow.containsKey(keyLeft)) {
                        set.addAll(keyFollow.get(keyLeft));
                    }
                    if (keyFollow.containsKey(vn_VnListCell.get(keyLeft))) {
                        set.addAll(keyFollow.get(vn_VnListCell.get(keyLeft)));
                    }
                    keyFollow.put(vn_VnListCell.get(keyLeft), set);
                    keyQueue.add(vn_VnListCell.get(keyLeft));

                    // 移除已处理的组合
                    vn_VnListCell.remove(keyLeft);
                    vn_VnList.set(t, vn_VnListCell);
                }
            }
        }

        // 此时keyFollow为完整的FOLLOW集
        FOLLOW = keyFollow;
        // 打印FOLLOW集合
        Iterator<String> itF = keyFollow.keySet().iterator();
        while (itF.hasNext()) {
            String key = itF.next();
            HashSet<String> f = keyFollow.get(key);
            outText.append("\tFOLLOW(" + key + ")={" + String.join("、", f.toArray(new String[f.size()])) + "}"+"\r\n");
        }
    }
    // 判断是否是LL(1)文法
    private boolean isLL1() {
        outText.append("\n正在判断是否是LL(1)文法...."+"\r\n");
        boolean flag = true;// 标记是否是LL(1)文法
        Iterator<String> it = VN.iterator();
        while (it.hasNext()) {
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);// 单条产生式
            if (list.size() > 1) { // 如果单条产生式的左边包含两个式子以上，则进行判断
                for (int i = 0; i < list.size(); i++) {
                    String aLeft = String.join("", list.get(i).toArray(new String[list.get(i).size()]));
                    for (int j = i + 1; j < list.size(); j++) {
                        String bLeft = String.join("", list.get(j).toArray(new String[list.get(j).size()]));
                        if ("ε".equals(aLeft) || "ε".equals(bLeft)) { // (1)若b＝ε,则要FIRST(A)∩FOLLOW(A)=φ
                            HashSet<String> retainSet = new HashSet<>();
                            // retainSet=FIRST.get(key);//需要要深拷贝，否则修改retainSet时FIRST同样会被修改
                            retainSet.addAll(FIRST.get(key));
                            if (FOLLOW.get(key) != null) {
                                retainSet.retainAll(FOLLOW.get(key));
                            }
                            if (!retainSet.isEmpty()) {
                                flag = false;// 不是LL(1)文法，输出FIRST(a)FOLLOW(a)的交集
                                outText.append("\tFIRST(" + key + ") ∩ FOLLOW(" + key + ") = {"
                                        + String.join("、", retainSet.toArray(new String[retainSet.size()])) + "}\r\n");
                                break;
                            } else {
                                outText.append("\tFIRST(" + key + ") ∩ FOLLOW(" + key + ") = φ"+"\r\n");
                            }
                        } else { // (2)b!＝ε若,则要FIRST(a)∩FIRST(b)= Ф
                            HashSet<String> retainSet = new HashSet<>();
                            retainSet.addAll(FIRST.get(key + "→" + aLeft));
                            retainSet.retainAll(FIRST.get(key + "→" + bLeft));
                            if (!retainSet.isEmpty()) {
                                flag = false;// 不是LL(1)文法，输出FIRST(a)FIRST(b)的交集
                                outText.append("\tFIRST(" + aLeft + ") ∩ FIRST(" + bLeft + ") = {"
                                        + String.join("、", retainSet.toArray(new String[retainSet.size()])) + "}"+"\r\n");
                                break;
                            } else {
                                outText.append("\tFIRST(" + aLeft + ") ∩ FIRST(" + bLeft + ") = φ"+"\r\n");
                            }
                        }
                    }
                }
            }
        }
        if(flag) {
            outText.append("\t是LL(1)文法,继续分析!"+"\r\n");
        }else {
            outText.append("\t不是LL(1)文法,退出分析!"+"\r\n");
        }
        return flag;
    }
    // 构建预测分析表FORM
    private void preForm() {
        HashSet<String> set = new HashSet<>();
        set.addAll(VT);
        set.remove("ε");
        FORM = new String[VN.size() + 1][set.size() + 2];
        Iterator<String> itVn = VN.iterator();
        Iterator<String> itVt = set.iterator();

        // (1)初始化FORM,并根据oneLeftFirst(VN$VT,产生式)填表
        for (int i = 0; i < FORM.length; i++){
            for (int j = 0; j < FORM[0].length; j++) {
                if (i == 0 && j > 0) {// 第一行为Vt
                    if (itVt.hasNext()) {
                        FORM[i][j] = itVt.next();
                    }
                    if (j == FORM[0].length - 1) {// 最后一列加入#
                        FORM[i][j] = "#";
                    }
                }
                if (j == 0 && i > 0) {// 第一列为Vn
                    if (itVn.hasNext()) {
                        FORM[i][j] = itVn.next();
                    }
                }
                if (i > 0 && j > 0) {// 其他情况先根据oneLeftFirst填表
                    String oneLeftKey = FORM[i][0] + "$" + FORM[0][j];// 作为key查找其First集合
                    FORM[i][j] = oneLeftFirst.get(oneLeftKey);
                }
            }
        }

        // (2)如果有推出了ε，则根据FOLLOW填表
        for (int i = 1; i < FORM.length; i++) {
            String oneLeftKey = FORM[i][0] + "$ε";
            if (oneLeftFirst.containsKey(oneLeftKey)) {
                HashSet<String> followCell = FOLLOW.get(FORM[i][0]);
                Iterator<String> it = followCell.iterator();
                while (it.hasNext()) {
                    String vt = it.next();
                    for (int j = 1; j < FORM.length; j++) {
                        for (int k = 1; k < FORM[0].length; k++) {
                            if (FORM[j][0].equals(FORM[i][0]) && FORM[0][k].equals(vt)) {
                                FORM[j][k] = oneLeftFirst.get(oneLeftKey);
                            }
                        }
                    }
                }
            }
        }

        // (3)打印预测表,并存于Map的数据结构中用于快速查找
        outText.append("\n该文法的预测分析表为："+"\r\n");
        for (int i = 0; i < FORM.length; i++) {
            for (int j = 0; j < FORM[0].length; j++) {
                if (FORM[i][j] == null) {
                    outText.append(" " + "\t");
                }
                else {
                    outText.append(FORM[i][j] + "\t");
                    if (i > 0 && j > 0) {
                        String[] tmp = FORM[i][j].split("→");
                        preMap.put(FORM[i][0] + "" + FORM[0][j], tmp[1]);
                    }
                }
            }
            outText.append("\r\n");
        }
        outText.append("\r\n");
    }
    // 输入的单词串分析推导过程
    public void printAutoPre(String str) {
        outText.append(str + "的分析过程:"+"\r\n");
        Queue<String> queue = new LinkedList<>();// 句子拆分存于队列
        for (int i = 0; i < str.length(); i++) {
            String t = str.charAt(i) + "";
            if (i + 1 < str.length() && (str.charAt(i + 1) == '\'' || str.charAt(i + 1) == '’')) {
                t += str.charAt(i + 1);
                i++;
            }
            queue.offer(t);
        }
        queue.offer("#");// "#"结束
        // 分析栈
        Stack<String> stack = new Stack<>();
        stack.push("#");// "#"开始
        stack.push(START);// 初态为开始符号
        boolean isSuccess = false;
        int step = 1;
        while (!stack.isEmpty()) {
            String left = stack.peek();
            String right = queue.peek();
            // (1)分析成功
            if (left.equals(right) && "#".equals(right)) {
                isSuccess = true;
                outText.append((step++) + "\t#\t#\t" + "分析成功"+"\r\n");
                break;
            }
            // (2)匹配栈顶和当前符号，均为终结符号，消去
            if (left.equals(right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\t匹配成功" + left + "\r\n");
                stack.pop();
                queue.poll();
                continue;
            }
            // (3)从预测表中查询
            if (preMap.containsKey(left + right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\t用" + left + "→"
                        + preMap.get(left + right) + "," + right + "逆序进栈" + "\r\n");
                stack.pop();
                String tmp = preMap.get(left + right);
                for (int i = tmp.length() - 1; i >= 0; i--) {// 逆序进栈
                    String t = "";
                    if (tmp.charAt(i) == '\'' || tmp.charAt(i) == '’') {
                        t = tmp.charAt(i-1)+""+tmp.charAt(i);
                        i--;
                    }else {
                        t=tmp.charAt(i)+"";
                    }
                    if (!"ε".equals(t)) {
                        stack.push(t);
                    }
                }
                continue;
            }
            break;// (4)其他情况失败并退出
        }
        if (!isSuccess) {
            outText.append((step++) + "\t#\t#\t" + "分析失败"+"\r\n");
        }
    }
}
