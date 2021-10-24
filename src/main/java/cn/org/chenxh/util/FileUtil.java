package cn.org.chenxh.util;

import cn.org.chenxh.constant.FileTypeEnum;
import org.apache.tika.Tika;
import org.springframework.ui.ModelMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtil
{
    public static long  getFileSize(File f) throws Exception{//取得文件大小
        long s=0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s= fis.available();
        } else {
            f.createNewFile();
//            System.out.println("文件不存在");
        }
        return s;
    }
    // 递归
    public static long getFileSizes(File f)throws Exception//取得文件夹大小
    {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++)
        {
            if (flist[i].isDirectory())
            {
                size = size + getFileSizes(flist[i]);
            } else
            {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    public static  String FormetFileSize(long fileS) {//转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public long getlist(File f){//递归求取目录文件个数
        long size = 0;
        File flist[] = f.listFiles();
        size=flist.length;
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getlist(flist[i]);
                size--;
            }
        }
        return size;


    }

    public static void  traverse(File file){
        File []files=file.listFiles();
        if (files==null){
            return;
        }
        for (File file1:files){
            if (file1.isFile()){
                System.out.println(file1.getPath());
            }
            else{
                traverse(file1);
            }
        }
    }

    public static void  traverseDir(File file){
        File []files=file.listFiles();
        if (files==null){
            return;
        }
        for (File file1:files){
            if (file1.isDirectory()){
                System.out.println(file1.getPath());
                traverseDir(file1);
            }
        }
    }

    public static void  searchFile(File file,String keyword,List<ModelMap> list){
        File []files=file.listFiles();
        if (files==null){
            return;
        }
        ModelMap  modelMap = null;
        for (File file1:files){
            if(file1.getName().indexOf(keyword) != -1){
                modelMap  = new ModelMap();
                // 文件名称
                modelMap.put( "name", file1.getName() );
                // 修改时间
                modelMap.put( "updateTime", file1.lastModified() );
                // 是否是目录
                modelMap.put( "isDir", file1.isDirectory() );

                // 是否支持在线查看
                boolean flag = false;
                try {
                    if (FileTypeUtil.canOnlinePreview(new Tika().detect(file1))) {
                        flag = true;
                    }
                    modelMap.put( "preview", flag );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String type;
                // 文件地址,取相对路径
                String path  = file1.getPath();
                path = path.replaceAll("E:\\\\fms\\\\root\\\\","");
                path = path.replaceAll("E:/fms/root/","");
                path = path.replaceAll("\\\\","/");
                modelMap.put( "url", path);
                if (file1.isDirectory()){
                    modelMap.put( "type", "dir" );
                    searchFile(file1,keyword,list);
                }else{
                    // 获取文件类型
                    String contentType = null;
                    String suffix = file1.getName().substring( file1.getName().lastIndexOf(".") + 1 );
                    try {
                        contentType = new Tika().detect(file1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // 获取文件图标
                    modelMap.put("type", getFileType(suffix, contentType));
                }



                list.add(modelMap);
            }

            if (file1.isDirectory()){
                searchFile(file1,keyword,list);
            }
        }
    }
    /**
     * 获取文件类型
     *
     * @param suffix
     * @param contentType
     * @return
     */
    public static String getFileType(String suffix, String contentType) {
        String type;
        if (FileTypeEnum.PPT.getName().equalsIgnoreCase(suffix) || FileTypeEnum.PPTX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PPT.getName();
        } else if (FileTypeEnum.DOC.getName().equalsIgnoreCase(suffix) || FileTypeEnum.DOCX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.DOC.getName();
        } else if (FileTypeEnum.XLS.getName().equalsIgnoreCase(suffix) || FileTypeEnum.XLSX.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.XLS.getName();
        } else if (FileTypeEnum.PDF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.PDF.getName();
        } else if (FileTypeEnum.HTML.getName().equalsIgnoreCase(suffix) || FileTypeEnum.HTM.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.HTM.getName();
        } else if (FileTypeEnum.TXT.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.TXT.getName();
        } else if (FileTypeEnum.SWF.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.FLASH.getName();
        } else if (FileTypeEnum.ZIP.getName().equalsIgnoreCase(suffix) || FileTypeEnum.RAR.getName().equalsIgnoreCase(suffix) || FileTypeEnum.SEVENZ.getName().equalsIgnoreCase(suffix)) {
            type = FileTypeEnum.ZIP.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.AUDIO.getName() + "/")) {
            type = FileTypeEnum.MP3.getName();
        } else if (contentType != null && contentType.startsWith(FileTypeEnum.VIDEO.getName() + "/")) {
            type = FileTypeEnum.MP4.getName();
        } else {
            type = FileTypeEnum.FILE.getName();
        }
        return type;
    }
    public static List<ModelMap>  traverseDirTreeData(File file){
        File []files=file.listFiles();
        if (files==null){
            return null;
        }
        List<ModelMap> res = new ArrayList<>();
        ModelMap modelMap = null;
        for (File file1:files){
            if (file1.isDirectory()){
                modelMap = new ModelMap();
                modelMap.put("id",file1.getName().hashCode());
                modelMap.put("name",file1.getName());
                modelMap.put("url",file1.getPath());
                modelMap.put("spread",true); //设置默认展开
                modelMap.put("children",traverseDirTreeData(file1)); //设置默认展开
                res.add(modelMap);
            }
        }
        return res;
    }

    public static void copyDir(String oldPath, String newPath) throws IOException {
        File file = new File(oldPath);        //文件名称列表

        if (!(new File(newPath)).exists()) {
            (new File(newPath)).mkdir();
        }

        if(file.isDirectory()){
            String[] filePath = file.list();
            for (int i = 0; i < filePath.length; i++) {
                if ((new File(oldPath + file.separator + filePath[i])).isDirectory()) {
                    copyDir(oldPath  + file.separator  + filePath[i], newPath  + file.separator + filePath[i]);
                }

                if (new File(oldPath  + file.separator + filePath[i]).isFile()) {
                    copyFile(oldPath + file.separator + filePath[i], newPath + file.separator + filePath[i]);
                }

            }

        }else if(file.isFile()){
            String filename = "";
            if(oldPath.indexOf("/")!=-1) {
                filename = oldPath.substring(oldPath.lastIndexOf("/")+1,oldPath.length());
            }else if(oldPath.indexOf("\\")!=-1){
                filename = oldPath.substring(oldPath.lastIndexOf("\\")+1,oldPath.length());
            }
            //如果目标路径没有包含指定文件名称，仅仅是路径的话，就拼接文件名。如果路径已经包含了文件名就使用目标路径
            String target = newPath.indexOf(filename)== -1?newPath+ file.separator+filename:newPath;
            copyFile(oldPath , target);
        }
    }

    public static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File file = new File(newPath);
        FileInputStream in = new FileInputStream(oldFile);
        FileOutputStream out = new FileOutputStream(file);;

        byte[] buffer=new byte[2097152];

        while((in.read(buffer)) != -1){
            out.write(buffer);
        }
        out.close();
        in.close();
    }
    public static void deleteFile(File file){
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()){
//            flag = 0;
            return;
        }
        if(file.isDirectory()){
            //取得这个目录下的所有子文件对象
            File[] files = file.listFiles();
            //遍历该目录下的文件对象
            for (File f: files){
                //打印文件名
                String name = file.getName();
                //判断子目录是否存在子目录,如果是文件则删除
                if (f.isDirectory()){
                    deleteFile(f);
                }else {
                    f.delete();
                }
            }
        }
        //删除空文件夹  for循环已经把上一层节点的目录清空。
        file.delete();
    }

    public static void main(String args[])
    {
        FileUtil g = new FileUtil();
        long startTime = System.currentTimeMillis();
        try
        {
            /*===============获取文件大小================================*/
            long l = 0;
            String path = "E:/fms/root/";
            File ff = new File(path);
            if (ff.isDirectory()) { //如果路径是文件夹的时候
                System.out.println("文件个数           " + g.getlist(ff));
                System.out.println("目录");
                l = g.getFileSizes(ff);
                System.out.println(path + "目录的大小为：" + g.FormetFileSize(l));
            } else {
                System.out.println("     文件个数           1");
                System.out.println("文件");
                l = g.getFileSize(ff);
                System.out.println(path + "文件的大小为：" + g.FormetFileSize(l));
            }
        long endTime = System.currentTimeMillis();
        System.out.println("总共花费时间为：" + (endTime - startTime) + "毫秒...");


        /*================获取文件路径==========================*/
//            traverseDir(ff);
           List<ModelMap> modles =  traverseDirTreeData(ff);
            System.out.print(modles);


            /*================复制移动文件======================*/
            String sourcePath = "E:\\music\\《数码宝贝》主题曲 - Butterfly.mp3";
            String targetpath = "E:\\Download";

            copyDir(sourcePath, targetpath);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
