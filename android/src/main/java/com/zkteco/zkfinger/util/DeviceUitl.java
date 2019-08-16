package com.zkteco.zkfinger.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeviceUitl {
    /**
     * 获取所有的外接设备目录//Get all the external device directories
     *
     * @return
     */

    public static List<String> getDeviceFiles() {

        List<Partitions> partitionList = getPartitions();//获得挂载点列表

        List<String> devicePathList = new ArrayList<String>();


        if (partitionList != null && partitionList.size() > 0) {//有挂载点


            File mountsFile = new File("/proc/mounts");


            if (mountsFile.exists() && mountsFile.isFile() && mountsFile.canRead()) {

                BufferedReader reader = null;

                try {

                    reader = new BufferedReader(new FileReader(mountsFile));
                    List<String> devList = new ArrayList<String>();
                    String tempString = null;
                    while ((tempString = reader.readLine()) != null) {
                        if (tempString.startsWith("/dev/block/vold/")) {
                            devList.add(tempString);
                        }
                    }
                    for (String strs : devList) {
                        String[] args = strs.split(" ");
                        if (args != null && args.length > 2) {
                            if (isThePartitionPath(partitionList, args[0])) {
                                devicePathList.add(args[1]);
                            }
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    if (reader != null) {

                        try {

                            reader.close();

                        } catch (IOException e1) {

                        }

                    }

                }

            }

        }


        return devicePathList;

    }
    /**
     * 获取手机系统中所有被挂载的TF卡，包括OTG等//Get all mounted TF cards in the mobile phone system, including OTG, etc.
     *
     * @return
     */
    public static List<String> getAllExterSdcardPath() {
        List<String> SdList = new ArrayList<String>();

        String firstPath = Environment.getExternalStorageDirectory().getPath();

        try {
            Runtime runtime = Runtime.getRuntime();
            // 运行mount命令，获取命令的输出，得到系统中挂载的所有目录
            //// Run the mount command to get the output of the command and get all the directories mounted in the system.
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                Log.d("", line);
                // 将常见的linux分区过滤掉
                // Filter common linux partitions
                if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
                        || line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
                    continue;
                }

                // 下面这些分区是我们需要的
                //// The following partitions are what we need
                if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs"))) {
                    // 将mount命令获取的列表分割，items[0]为设备名，items[1]为挂载路径
                    //// Split the list obtained by the mount command, items[0] is the device name,
                    // and items[1] is the mount path.
                    String items[] = line.split(" ");
                    if (items != null && items.length > 1) {
                        String path = items[1].toLowerCase(Locale.getDefault());
                        // 添加一些判断，确保是sd卡，如果是otg等挂载方式，可以具体分析并添加判断条件
                        /// / Add some judgment to ensure that it is sd card, if it is otg
                        // and other mounting methods, you can analyze and add judgment conditions
                        if (path != null && !SdList.contains(path) && path.contains("sd"))
                            SdList.add(items[1]);
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (!SdList.contains(firstPath)) {
            SdList.add(firstPath);
        }

        return SdList;
    }


    /**
     * 验证该挂载点是否是移动设备 //Verify that the mount point is a mobile device
     *
     * @param partitionList 移动设备挂载点列表 //Mobile device mount point list
     * @param point         挂载点 //Mount point
     * @return
     */

    private static boolean isThePartitionPath(List<Partitions> partitionList, String point) {

        boolean isPartition = false;

        if (partitionList != null && point != null) {

            for (Partitions p : partitionList) {

                if (point.endsWith(p.getMajor() + ":" + p.getMinior())) {

                    isPartition = true;

                    break;

                }

            }

        }

        return isPartition;

    }


    /**
     * 获取外接设备挂载点 //Get the mount point of the external device
     *
     * @return
     */

    public static List<Partitions> getPartitions() {


        File partitonsFile = new File("/proc/partitions");

        List<Partitions> partitionList = new ArrayList<Partitions>();


        if (partitonsFile.exists() && partitonsFile.isFile()) {

            List<String> lineList = new ArrayList<String>();

            BufferedReader reader = null;

            try {

                reader = new BufferedReader(new FileReader(partitonsFile));

                String tempString = null;

                while ((tempString = reader.readLine()) != null) {

                    lineList.add(tempString);

//                Log.d("--------partitions tempString:"+tempString);

                }

                reader.close();

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                if (reader != null) {

                    try {

                        reader.close();

                    } catch (IOException e1) {

                        e1.printStackTrace();

                    }

                }

            }


            for (String line : lineList) {

                String[] strs = line.trim().split(" ");

                Partitions partitions = new Partitions();

                for (int i = 0; i < strs.length; i++) {

                    if (!"".equals(strs[i])) {

                        try {

                            if (partitions.getMajor() == null) {

                                partitions.setMajor(Integer.valueOf(strs[i]));

                                continue;

                            }

                            if (partitions.getMinior() == null) {

                                partitions.setMinior(Integer.valueOf(strs[i]));

                                continue;

                            }

                            if (partitions.getBlocks() == null) {

                                partitions.setBlocks(Long.valueOf(strs[i]));

                                continue;

                            }

                            if (partitions.getName() == null) {

                                partitions.setName(strs[i]);

                                continue;

                            }

                        } catch (Exception e) {

//e.printStackTrace();

                            continue;

                        }


                    }

                }

//名称不能为空，不能是mtd//The name cannot be empty, it cannot be mtd

                if (partitions.getName() != null && !partitions.getName().trim().equals("") && !partitions.getName().startsWith("mtd")) {

                    partitionList.add(partitions);

                }


            }

        }


        return partitionList;

    }


    /**
     * @title 挂载点//挂载点
     */

    public static class Partitions {

        private Integer major;//父节点//Parent node

        private Integer minior;//分区节点//Partition node

        private Long blocks;//容量//capacity

        private String name;//名称//name


        public Integer getMajor() {

            return major;

        }

        public void setMajor(Integer major) {

            this.major = major;

        }

        public Integer getMinior() {

            return minior;

        }

        public void setMinior(Integer minior) {

            this.minior = minior;

        }

        public Long getBlocks() {

            return blocks;

        }

        public void setBlocks(Long blocks) {

            this.blocks = blocks;

        }

        public String getName() {

            return name;

        }

        public void setName(String name) {

            this.name = name;

        }


    }
}
