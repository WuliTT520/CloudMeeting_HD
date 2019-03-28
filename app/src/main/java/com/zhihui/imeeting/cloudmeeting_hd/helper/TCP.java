package com.zhihui.imeeting.cloudmeeting_hd.helper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCP {

    public void post() throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        //1.建立TCP连接
        String ip="192.168.43.19";   //服务器端ip地址
        int port=8888;        //端口号
        Socket sck=new Socket(ip,port);
        //2.传输内容
        String content="open";
        byte[] bstream=content.getBytes("GBK");  //转化为字节流
        OutputStream os=sck.getOutputStream();   //输出流
        os.write(bstream);
        //3.关闭连接
        sck.close();
    }

}