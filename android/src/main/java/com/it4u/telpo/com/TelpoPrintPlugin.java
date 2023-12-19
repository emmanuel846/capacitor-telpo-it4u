package com.it4u.telpo.com;

import android.util.Log;

import com.common.apiutil.CommonException;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.common.apiutil.printer.UsbThermalPrinter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "TelpoPrint")
public class TelpoPrintPlugin extends Plugin {
    private final int NOPAPER = 3;
    private final int LOWBATTERY = 4;
    private final int OVERHEAT = 12;
    private TelpoPrint implementation = new TelpoPrint();
    UsbThermalPrinter printer;
    @Override
    public void load() {
        printer = new UsbThermalPrinter(this.getActivity().getApplicationContext());
        super.load();
    }

    @Override
    protected void handleOnStart() {
        super.handleOnStart();
        printer = new UsbThermalPrinter(this.getActivity().getApplicationContext());
    }

    @PluginMethod
    public void print(PluginCall call) {
        JSObject data = call.getObject("receipt");
        printReceipt(data);
        call.resolve();
    }
    void printReceipt(JSONObject data){
        try {
            printer.reset();
            printer.start(1);
            //printer.setMonoSpace(true);
            Log.i("VERSION",printer.getVersion());
            printer.setGray(7);
            try {
                String title = data.getString("title");
                String footer = data.getString("footer");
                JSONArray lines = data.getJSONArray("lines");
                JSONObject header = data.getJSONObject("header");
                String agencyName = header.getString("agencyName");
                String agencyContact = header.getString("agencyContact");
                String agencyAdress = header.getString("agencyAdress");
                printer.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                printer.setTextSize(30);
                printer.addString(agencyName +"\n");
                printer.setTextSize(20);
                printer.addString("Tel: "+agencyContact + "\n");
                printer.addString("Adresse: "+agencyAdress + "\n");
                //printer.setLineSpace(15);
                printer.addString(title +"\n");
                for (int i=0; i < lines.length(); i++){
                    if(i==0){
                        printer.walkPaper( 5);
                    }
                    if(i == (lines.length() - 1)){
                        printer.walkPaper( 8);
                    }
                    JSONObject val = lines.getJSONObject(i);
                    String key = val.getString("title");
                    String value = val.getString("value");
                    int kv = printer.measureText(key+""+value);
                    int kv2 = printer.measureText(" ");
                    int SpaceNumber=(384-kv)/kv2;
                    String spaceString = "";
                    for (int j=0;j<SpaceNumber;j++){
                        spaceString+=" ";
                    }
                    printer.setAlgin(UsbThermalPrinter.ALGIN_LEFT);
                    printer.addString(key+""+spaceString+""+value);
                }
                printer.setAlgin(UsbThermalPrinter.ALGIN_MIDDLE);
                printer.addString(footer+"\n");
                printer.printString();
                printer.walkPaper(10);

            }catch (JSONException e){

            }
        }catch (CommonException telpoException){
            telpoException.printStackTrace();
        }
    }
}
