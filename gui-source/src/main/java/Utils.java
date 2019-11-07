import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

class Utils {
    static boolean isUserDebug() {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/sh", "-c", "adb root"};
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String isRoot = input.readLine();
            return Constants.DEVICE_CHECK_IS_USER_DEBUG.equals(isRoot)
                    || Constants.DEVICE_CHECK_IS_USER_DEBUG_2.equals(isRoot);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static boolean isRoot() {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "adb shell su -c \"\""};
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String isRoot = input.readLine();
            return isRoot != null && !isRoot.contains(Constants.DEVICE_CHECK_NOT_ROOT)
                    && !Constants.DEVICE_CHECK_DISABLE_SHELL_ROOT.equals(isRoot);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static boolean isSupportApi() {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "adb shell getprop ro.build.version.sdk"};
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String sdk = input.readLine();
            return "27".equals(sdk);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static void replaceSystemLib(boolean isRoot) {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "sh ./replace_system_so.sh" + (isRoot ? " -r" : "")};
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static int getDevicesCount() {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "adb shell -c \"\""};
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            if (line == null) {
                return 0;
            }
            if (line.equals(Constants.DEVICE_COUNT_NONE)) {
                return 0;
            }
            if (line.contains(Constants.DEVICE_COUNT_MORE_THAN_ONE)) {
                return 2;
            }
            return 1;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
            return 0;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static void startMallocDebug(boolean isRoot, String packageName, int traceDeep) {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "sh ./setup_env.sh"
                    + " -n " + packageName                          // 包名
                    + (isRoot ? " -r" : "")                         // 是否是root手机
                    + (traceDeep > 0 ? " -d " + traceDeep : "")     // 收集的调用栈深度
            };
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static void stopMallocDebug(boolean isRoot, String packageName) {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "sh ./reset_env.sh"
                    + " -n " + packageName                          // 包名
                    + (isRoot ? " -r" : "")                         // 是否是root手机
            };
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    static void dumpHeap(boolean isRoot, String packageName, String symbolsDir) {
        BufferedReader input = null;
        try {
            String []cmdarry ={"/bin/bash", "-c", "sh ./dump.sh"
                    + " -n " + packageName                                                                   // 包名
                    + (isRoot ? " -r" : "")                                                                  // 是否是root手机
                    + ((symbolsDir == null || symbolsDir.trim().equals("")) ? "" : " -s " + symbolsDir)      // 带符号表so所在目录
            };
            Process process = Runtime.getRuntime().exec(cmdarry);
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            Runtime.getRuntime().exit(0);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
