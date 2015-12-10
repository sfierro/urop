package edu.mit.dlab.ppganalyzer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Creates a recorder to start thread
 */
public class Recorder {
    static LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<String>();
    static Context ctx;
    static String fileName="";
    static String toRecord="RawData";
    static RecorderThread rt=null;
    static String filePath = "";


    
    public Recorder(){}

    static public void setContext(Context ctx){
        Recorder.ctx=ctx;
    }
    static public void startRecording() throws FileNotFoundException{

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss", Locale.US);
//        Date date = new Date();
//        String message=Recorder.toRecord+" "+dateFormat.format(date)+"\n\r";
        rt = new RecorderThread(lbq,ctx,fileName,filePath);
        rt.start();
    }
    
    static public void stopRecording(){
        if (Recorder.rt!=null){
            Recorder.rt.shutdown();
            Recorder.rt=null;
            lbq = new LinkedBlockingQueue<String>();
        }
    }
    
    static public void recordString(String s){
        if (Recorder.rt!=null){
            lbq.add(s);
        }
    }
    
    static public String baseFileName(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm", Locale.US);
        Date date = new Date();
        return Recorder.toRecord+"_"+dateFormat.format(date)+".txt";
    }
}

/**
 * Creates a thread to write to
 */
class RecorderThread extends Thread {
    private volatile Boolean stopped=false;
    private LinkedBlockingQueue<String> lbq;
    private Context ctx;
    private String fileName="";
//    private String recordingMessage;

    // Output file path
    private String filePath = null;
    
    public RecorderThread(LinkedBlockingQueue<String> lbq,Context ctx,String fileName,String filePath){
        this.lbq=lbq;
        this.ctx=ctx;
        this.fileName=fileName;
        this.filePath=filePath;
//        this.recordingMessage=recordingMessage;
    }
    
    public void shutdown(){
        this.stopped=true;
    }

    @SuppressLint("WorldWriteableFiles")
    @Override
    public void run() {
        try {
            @SuppressWarnings("deprecation")
            FileOutputStream outputStream= ctx.openFileOutput(this.fileName,
                    Context.MODE_WORLD_READABLE);
//            outputStream.write(recordingMessage.getBytes());
            BufferedOutputStream mOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
            String toAdd;
            while (!stopped) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {e.printStackTrace();
                }
                toAdd="";
                while(lbq.size()>0){
                    toAdd+=lbq.remove()+"\n";
                }
                mOutputStream.write(toAdd.getBytes());
                outputStream.write(toAdd.getBytes());
            }
            outputStream.close();
            mOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
