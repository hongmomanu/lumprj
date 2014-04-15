package lumprj.java.eqim;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import cn.gd.seismology.config.XmlLocation;
import cn.gd.seismology.config.XmlLocation.SourceParam;
import cn.gd.seismology.liss.client.LissClient;
import cn.gd.seismology.liss.client.LissException;
import cn.gd.seismology.liss.message.Result;



public class EqimConnectorTip {

    private LissClient client = null;

    private InputStream locInputStream = null;

    private boolean connectionOK = false;

    private boolean bQuit = false;

    private Thread clientThread;

    public void connectServer() throws IOException, LissException {

        //client = new LissClient("10.33.5.5", 5001);  //ip 端口
        client = new LissClient("10.33.8.174", 5001);  //ip 端口
        client.login("show","show");//user password
        locInputStream = client.retrieveResult("LOC");
    }

    public void quit() {
        try {
            client.quit();
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        this.bQuit = true;
    }



    public void loopConnect() {
        while (!bQuit) {
            try {
                connectServer();
                System.out.println("connect ok");
                connectionOK = true;
                break;
            } catch (Exception ex) {
                connectionOK = false;

                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException iEx) {
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void receiveAndPublish() {
        loopConnect();
        clientThread = new Thread("EqimMonitorDispatcher") {

            public void run() {
                System.out.println(Thread.currentThread().getName()+  " receiverAndPublish...");
                Result res = null;
                while (!bQuit) {
                    try {
                        res = null;

                        res = Result.buildResult(locInputStream);
                        if (res != null) {
                            parse(res);
                            System.out.println("receive a message, to do something ...");
                        } else
                            loopConnect();

                    } catch (InterruptedIOException iioEx) {

                        loopConnect();
                    } catch (IOException ioEx) {
                        ioEx.printStackTrace();

                        loopConnect();
                    }
                }
            }


        };
        clientThread.start();
    }

    private void parse(Result res) {
        XmlLocation xl = new XmlLocation();
        xl.parseLocation(new ByteArrayInputStream(res.getData()));
        SourceParam sp = xl.getSource();
        //sp.Location_cname
        System.out.println(sp.Location_cname);

    }

    public static void main(String[] args){
        EqimConnectorTip t=new EqimConnectorTip();
        t.receiveAndPublish();
    }

}
