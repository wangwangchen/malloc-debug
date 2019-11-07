public class MallocDebug extends JDialog {

    private final boolean mIsUserDebug;

    private JPanel contentPane;
    private JButton mStartBtn;
    private JButton mStopBtn;
    private JTextField mPackageName;
    private JTextField mSymbolsDir;
    private JButton mDumpBtn;
    private JButton mSelectSymbolsDir;
    private JTextField mTraceDeep;

    public MallocDebug() {

        // 监测当前手机系统是否可用
        mIsUserDebug = Utils.isUserDebug();
        if (!mIsUserDebug) {
            boolean isRoot = Utils.isRoot();
            if (!isRoot) {
                JOptionPane.showMessageDialog(null, "手机不是userdebug系统，或者root权限被禁用");
                Runtime.getRuntime().exit(0);
            }
        }

        if (!Utils.isSupportApi()) {
            JOptionPane.showMessageDialog(null, "抱歉，只支持android 8.1系统");
            Runtime.getRuntime().exit(0);
        }

        Utils.replaceSystemLib(!mIsUserDebug);

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(mStartBtn);

        mStartBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStart();
            }
        });

        mStopBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStop();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        mDumpBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDump();
            }
        });

        mDumpBtn.setEnabled(false);
        mStopBtn.setEnabled(false);

        mSymbolsDir.setEnabled(false);
        mSelectSymbolsDir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelectSymbolsDir();
            }
        });

        // 限制调用栈深度只能输入数字
        mTraceDeep.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if(keyChar < KeyEvent.VK_0 || keyChar > KeyEvent.VK_9){
                    e.consume(); //关键，屏蔽掉非法输入
                }
            }
        });

    }

    private void onDump() {
        final String packageName = mPackageName.getText();
        if (packageName == null || packageName.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "请输入包名");
            return;
        }

        String path = System.getenv("PATH");
        String[] split = path.split(":");
        boolean isObjdumpExit = false;
        boolean isAddr2lineExit = false;
        for (String dir : split) {
            if (new File(dir, "toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-objdump").exists()) {
                isObjdumpExit = true;
            }
            if (new File(dir, "toolchains/arm-linux-androideabi-4.9/prebuilt/darwin-x86_64/bin/arm-linux-androideabi-addr2line").exists()) {
                isAddr2lineExit = true;
            }
        }
        if (!isObjdumpExit || !isAddr2lineExit) {
            JOptionPane.showMessageDialog(null, "未配置NDK环境变量或者是ndk版本太低");
            return;
        }

        String symbolsDir = mSymbolsDir.getText();
        if (symbolsDir == null || symbolsDir.trim().equals("")) {
            int index = JOptionPane.showConfirmDialog(null, "未指定带符号表so目录，无法获取内存申请行号信息，马上选择？", "提示", JOptionPane.YES_NO_OPTION);
            if (index == 0) {
                onSelectSymbolsDir();
                symbolsDir = mSymbolsDir.getText().trim();
            }
        }
        mDumpBtn.setText("dumping...");
        mDumpBtn.setEnabled(false);
        mStopBtn.setEnabled(false);
        final String finalSymbolsDir = symbolsDir;
        new Thread(new Runnable() {
            public void run() {
                Utils.dumpHeap(!mIsUserDebug, packageName, finalSymbolsDir);
                mDumpBtn.setText("导出Heap");
                mDumpBtn.setEnabled(true);
                mStopBtn.setEnabled(true);
            }
        }).start();
    }

    private void onStop() {
        final String text = mPackageName.getText();
        if (text == null || text.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "请输入包名");
            return;
        }
        mStopBtn.setText("stopping...");
        mDumpBtn.setEnabled(false);
        mStopBtn.setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
                Utils.stopMallocDebug(!mIsUserDebug, text);
                mPackageName.setEnabled(true);
                mStopBtn.setText("停止");
                mStartBtn.setText("启动");
                mStartBtn.setEnabled(true);
            }
        }).start();
    }

    private void onSelectSymbolsDir() {
        JFileChooser jfc=new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY );
        jfc.showDialog(new JLabel(), "选择");
        try {
            File file = jfc.getSelectedFile();
            mSymbolsDir.setText(file.getAbsolutePath());
        } catch (Exception ignore) {
        }
    }

    private void onStart() {
        final String text = mPackageName.getText();
        if (text == null || text.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "请输入包名");
            return;
        }
        mStartBtn.setText("启动中...");
        mStartBtn.setEnabled(false);
        String traceDeep = mTraceDeep.getText();
        int traceDeepInt = -1;
        if (traceDeep != null && !traceDeep.trim().equals("")) {
            try {
                traceDeepInt = Integer.parseInt(traceDeep);
            } catch (Exception ignore) {
            }
        }
        final int finalTraceDeepInt = traceDeepInt;
        new Thread(new Runnable() {
            public void run() {
                Utils.startMallocDebug(!mIsUserDebug, text, finalTraceDeepInt);
                mPackageName.setEnabled(false);
                mStartBtn.setText("已启动");
                mDumpBtn.setEnabled(true);
                mStopBtn.setEnabled(true);
            }
        }).start();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        MallocDebug dialog = new MallocDebug();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        System.exit(0);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
