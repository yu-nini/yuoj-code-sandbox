package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox {
    private static  final String GLOBAL_CODE_DIR_NAME="tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static void main(String[] args) throws IOException {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2"," 3 4"));
        String code = ResourceUtil.readStr("testCode.simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest excuteCodeRequest) throws IOException {
        List<String> inputList = excuteCodeRequest.getInputList();
        String code = excuteCodeRequest.getCode();
        String language = excuteCodeRequest.getLanguage();
        String property = System.getProperty("user.dir");
        String globalCodePathName = property+ File.separator+GLOBAL_CODE_DIR_NAME;
        //没有则新建
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        //存在则将用户代码保存为文件
        //把用户的代码分级存放隔离
        String UserCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String UserCodeFilePath = UserCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File file = FileUtil.writeString(code, UserCodeFilePath, StandardCharsets.UTF_8);
        //编译代码，得到class文件
        String compliteCmd = String.format("javac -encoding utf-8 %s",file.getAbsolutePath());
        try{
            Process compliteProcess = Runtime.getRuntime().exec(compliteCmd);
            //java获取控制台的输入
            //直到程序运行完成
            int exitValue = compliteProcess.waitFor();
            if (exitValue == 0){
                //正常退出
                System.out.println("执行成功！"+exitValue);
                //分批获取进程的正常输出
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(compliteProcess.getInputStream()));
                //逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine() )!= null){
                    System.out.println(compileOutputLine);
                }
            }else {
                System.out.println("执行失败！"+exitValue);
                //分批获取进程的error输出
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(compliteProcess.getInputStream()));
                //逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine() )!= null){
                    System.out.println(compileOutputLine);
                }
                //分批获取进程的输出
                BufferedReader bufferedErrorReader =
                        new BufferedReader(new InputStreamReader(compliteProcess.getErrorStream()));
                //逐行读取
                String compileOutputErrorLine;
                while ((compileOutputErrorLine = bufferedErrorReader.readLine() )!= null){
                    System.out.println(compileOutputErrorLine);
                }
            }

        }catch (Exception e){
            throw new ExportException(e.toString());
        }
        return null;
    }
}
