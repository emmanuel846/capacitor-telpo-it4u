package com.it4u.telpo.com;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Bitmap;

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

import com.sunyard.api.printer.IPrinter;
import com.sunyard.api.printer.OnPrintListener;
import com.sunyard.api.printer.PrintConstant;
import com.it4u.telpo.com.service.DeviceService;
import com.it4u.telpo.com.service.DeviceServiceGet;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;
import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_LEFT;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_RIGHT;

@CapacitorPlugin(name = "TpePrint")
public class TpePrintPlugin extends Plugin {
    private boolean boundPos = false;
    private final int NOPAPER = 3;
    private boolean isSunyardServiceBound = false;
    private final int LOWBATTERY = 4;
    private final int OVERHEAT = 12;
    UsbThermalPrinter printer;
    @Override
    public void load() {
        printer = new UsbThermalPrinter(this.getActivity().getApplicationContext());
        if(!boundPos){
            ServiceManager.bindPosServer(this.getActivity().getApplicationContext());
            Log.i("SM", "BindPOS");
        }
        super.load();
    }

    @Override
    protected void handleOnStart() {
        super.handleOnStart();

        printer = new UsbThermalPrinter(this.getActivity().getApplicationContext());
    }

    /*@PluginMethod
    public void print(PluginCall call) {
        JSObject data = call.getObject("receipt");
        printReceipt(data);
        call.resolve();
    }*/

    public boolean isSunyard(){
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        return manufacturer.contains("sunyard") || model.startsWith("s");
    }
    private void printOnSunyard(JSONObject data2Print, Context context) {
        // Bind au service si pas encore fait
        if (!isSunyardServiceBound) {
            DeviceService.init(context, new DeviceServiceGet.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected() {
                    try {
                        isSunyardServiceBound = true;
                        // Une fois connecté, lancer l'impression
                        printSunYard(data2Print, context);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        presentToast(context, "Erreur Callback: " + t.getMessage(), ToastType.LONG);
                    }
                }

                @Override
                public void onServiceDisConnected() {
                    isSunyardServiceBound = false;
                    presentToast(context, "Service Sunyard déconnecté", ToastType.SHORT);
                }
            });
        } else {
            // Service déjà connecté, imprimer directement
            printSunYard(data2Print, context);
        }
    }
    @PluginMethod
    public void print(PluginCall call) {
        JSObject data = call.getObject("receipt");
        try{
            printOnSunyard(new JSONObject(data.toString()),this.getContext());
        }catch(Exception e){
            
        }
        
        try{
            JSObject data2 = call.getObject("receipt");
            printReceipt(data2);
        }catch(Exception e){
            
        }
        try{
            Printer printer = Printer.getInstance(this.getActivity().getApplicationContext());
            printFeitianReceipt(printer, data);
        }catch(Exception e){

        }
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
    private void presentToast(Context context, String message, ToastType type) {
        int duration = (type == ToastType.LONG) ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(context, message, duration).show();
    }

    private enum ToastType {
        LONG,
        SHORT
    }

    public void logMsg(String msg) {
        Log.i("Printer", msg);
    }

    void printFeitianReceipt(Printer printer, JSONObject data) {

        try {
            int ret;
            ret = printer.open();
            if(ret != ERR_SUCCESS){
                logMsg("open failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return;
            }

            ret = printer.startCaching();
            if(ret != ERR_SUCCESS){
                logMsg("startCaching failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return;
            }

            ret = printer.setGray(3);
            if(ret != ERR_SUCCESS){
                logMsg("startCaching failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return;
            }

            PrintStatus printStatus = new PrintStatus();
            ret = printer.getStatus(printStatus);
            if(ret != ERR_SUCCESS){
                logMsg("getStatus failed"+ String.format(" errCode = 0x%x\n" , ret) );
                return;
            }

            logMsg("Temperature = "+ printStatus.getmTemperature() + "\n");
            logMsg("Gray = "+ printStatus.getmGray() + "\n");
            if(!printStatus.getmIsHavePaper()){
                logMsg("Printer out of paper\n");
                return;
            }

            String title = data.getString("title");
            String footer = data.getString("footer");
            JSONArray lines = data.getJSONArray("lines");
            JSONObject header = data.getJSONObject("header");
            String agencyName = header.getString("agencyName");
            String agencyContact = header.getString("agencyContact");
            String agencyAdress = header.getString("agencyAdress");

            printer.setAlignStyle(PRINT_STYLE_CENTER);
            printer.setSpace(0,30);
            printer.printStr(agencyName+"\n");
            printer.printStr("Tel: "+agencyContact+"\n");
            printer.printStr("Adresse: "+agencyAdress+"\n");
            printer.setSpace(0,15);
            printer.printStr("***********************************");
            printer.setAlignStyle(PRINT_STYLE_CENTER);
            printer.printStr(title+"\n");
            printer.printStr("***********************************\n");
            for (int i=0; i < lines.length(); i++){
                if(i==0){
                    printer.setSpace(0, 15);
                }
                if(i == (lines.length() - 1)){
                    printer.setSpace(0, 30);
                }
                JSONObject val = lines.getJSONObject(i);
                String key = val.getString("title");
                String value = val.getString("value");
                printer.setAlignStyle(PRINT_STYLE_LEFT);
                printer.printStr(key);
                printer.setAlignStyle(PRINT_STYLE_RIGHT);
                printer.printStr(value+"\n");
            }

            printer.setAlignStyle(PRINT_STYLE_CENTER);
            printer.printStr(footer+"\n");

            ret = printer.getUsedPaperLenManage();
            if(ret < 0){
                logMsg("getUsedPaperLenManage failed"+ String.format(" errCode = 0x%x\n" , ret) );
            }
            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    logMsg("print success\n");
                    printer.feed(32);
                }

                @Override
                public void onError(int i) {
                    logMsg("printBmp failed"+ String.format(" errCode = 0x%x\n", i) );
                }
            });

        } catch (JSONException e){
            e.printStackTrace();
            logMsg("print failed"+ e.toString()+"\n");
        }
        catch (Exception e) {
            e.printStackTrace();
            logMsg("print failed"+ e.toString()+"\n");
        }
    }

    void printSunYard(JSONObject data, Context context){
        try {
            IPrinter printer = DeviceServiceGet.getInstance().getPrinter();

            if (printer == null) {
                presentToast(context, "Erreur: Imprimante non disponible", ToastType.LONG);
                return;
            }

            // Configuration
            printer.setGray(5);

            // Bundle pour le texte
            android.os.Bundle textCenterBundle = new android.os.Bundle();
            android.os.Bundle textLeftBundle = new android.os.Bundle();
            android.os.Bundle textRightBundle = new android.os.Bundle();

            textCenterBundle.putInt("align", PrintConstant.Align.CENTER);
            textCenterBundle.putInt("font", PrintConstant.FontSize.NORMAL);
            textCenterBundle.putInt("fontTemplate", PrintConstant.FontTemplate.DEFAULT);

            textLeftBundle.putInt("align", PrintConstant.Align.LEFT);
            textLeftBundle.putInt("font", PrintConstant.FontSize.NORMAL);
            textLeftBundle.putInt("fontTemplate", PrintConstant.FontTemplate.DEFAULT);

            textRightBundle.putInt("align", PrintConstant.Align.RIGHT);
            textRightBundle.putInt("font", PrintConstant.FontSize.NORMAL);
            textRightBundle.putInt("fontTemplate", PrintConstant.FontTemplate.DEFAULT);

            // 1. Imprimer le logo
            // try {
            //     InputStream inputStream = context.getAssets().open("bcc-logo.bmp");
            //     Bitmap logoBitmap = BitmapFactory.decodeStream(inputStream);

            //     android.os.Bundle logoBundle = new android.os.Bundle();
            //     logoBundle.putInt("offset", 72); // Centrer le logo

            //     printer.addImage(logoBundle, bitmapToByteArray(logoBitmap));
            //     inputStream.close();
            // } catch (IOException e) {
            //     // Continuer sans logo si erreur
            //     e.printStackTrace();
            // }
            //entêtes
            String title = data.getString("title");
            String footer = data.getString("footer");
            JSONArray lines = data.getJSONArray("lines");
            JSONObject header = data.getJSONObject("header");
            String agencyName = header.getString("agencyName");
            String agencyContact = header.getString("agencyContact");
            String agencyAdress = header.getString("agencyAdress");
            printer.addText(textCenterBundle, agencyName +"\n");
            printer.addText(textCenterBundle, "Tel: "+agencyContact);
            printer.addText(textCenterBundle, "Adresse: "+agencyAdress );
            printer.addText(textCenterBundle, title );
            printer.feedLine(5);

            // 2. Imprimer les lignes de texte
            for (int i = 0; i < lines.length(); i++) {
                JSONObject val = lines.getJSONObject(i);
                String key = val.getString("title");
                String value = val.getString("value");
                printer.addText(textLeftBundle, key + ": " + value + "\n");
            }

            // 3. Imprimer le QR code
            // Bitmap qrCode = generateQRCodeBitmap(qrString);
            // if (qrCode != null) {
            //     android.os.Bundle qrBundle = new android.os.Bundle();
            //     qrBundle.putInt("align", PrintConstant.Align.CENTER);
            //     qrBundle.putInt("expectedHeight", 200);

            //     printer.addQrCode(qrBundle, qrString);
            // }

            // 4. Ajouter des lignes vides à la fin
            printer.feedLine(10);

            // 5. Lancer l'impression
            printer.startPrint(new OnPrintListener.Stub() {
                @Override
                public void onFinish() throws RemoteException {
                    // Impression réussie
                }

                @Override
                public void onError(int errorCode) throws RemoteException {
                    presentToast(context, "Erreur d'impression: " + errorCode, ToastType.LONG);
                }
            });

        } catch (RemoteException e) {
            e.printStackTrace();
            presentToast(context, "Erreur: " + e.getMessage(), ToastType.LONG);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

