package utils;

import java.io.IOException;
import java.io.OutputStream;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSWriter {

    private FileSystem hdfs;
    private final String dir;
    private OutputStream out;
    private long currentFileSize;
    private final long MAXFILESIZE;

    public HDFSWriter(String dir, String dns, int port, long maxFileSize) throws IOException {
        this.dir = dir;
        this.MAXFILESIZE = maxFileSize;
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));

        Configuration conf = new Configuration();
        conf.set("fs.default.name", String.format("hdfs://%s:%d", dns, port));
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        hdfs = FileSystem.get(conf);
        createOutputStream();
    }

    private String getFullDir() {
        Calendar ct = Calendar.getInstance();
        return String.format(dir + "/%d/%d/%d",
                ct.get(Calendar.YEAR),
                ct.get(Calendar.MONTH)+1,
                ct.get(Calendar.DATE)
        );
    }

    private void createOutputStream() throws IOException {
        String fullDir = getFullDir();
        Path path = new Path(fullDir);
        if (!hdfs.exists(path)) hdfs.mkdirs(path);
        Calendar ct = Calendar.getInstance();
        String HMS = ct.get(Calendar.HOUR_OF_DAY) + "_" + ct.get(Calendar.MINUTE) + '_' + ct.get(Calendar.SECOND);
        out = hdfs.create(new Path(fullDir + "/" + HMS + ".json"));
        currentFileSize = 0;
    }

    public void write(String line) throws IOException {
        byte[] bytes = (line + "\n").getBytes();
        out.write(bytes);
        currentFileSize += bytes.length;

        if (currentFileSize > MAXFILESIZE) {
            System.out.println("Flushing data");
            out.close();
            createOutputStream();
        }
    }
}
