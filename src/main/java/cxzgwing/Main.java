package cxzgwing;

import org.apache.log4j.BasicConfigurator;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {
    public static void main(String[] args) {
        // 解决日志报错问题
        BasicConfigurator.configure();

        // 关闭日志
        // Logger.getRootLogger().shutdown();

        // 加载dll
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");

        Window frame = new Window();

    }

}
