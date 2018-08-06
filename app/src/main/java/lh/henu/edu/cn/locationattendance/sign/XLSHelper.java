package lh.henu.edu.cn.locationattendance.sign;

import android.content.Context;
import android.widget.Toast;

import com.tencent.tauth.Tencent;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by bowen on 2017/12/24.
 */

public class XLSHelper {
    private static final String PATH = "/data/data/lh.henu.edu.cn.locationattendance/files";

    public static void mkDirs(){
        File file = new File(PATH+"/xls");
        if(!file.exists()){
            file.mkdirs();
        }
    }

    public static void writeToFile(SignInDataList dataList, String groupId, Context context){
        String path = PATH+"/xls"+"/"+groupId+".xls";
        File file = new File(path);
        //创建文件如果存在就删除再创建
        try{
            if(file.exists()){
                file.delete();
                file.createNewFile();
            }else{
                file.createNewFile();
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        if(dataList!=null&&dataList.signInDataList!=null){
            //写入文件
            String colName[] ={"签到发起时间","名字","签到结果"};
            WorkbookSettings workbookSettings = new WorkbookSettings();
            Locale locale = new Locale("zh","CN");
            workbookSettings.setLocale(locale);
            workbookSettings.setEncoding("ISO-8859-1");


            try{
                WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);
                WritableSheet sheet = writableWorkbook.createSheet("sheet",0);

                for(int i=0;i<colName.length;i++){
                    sheet.addCell(new Label(i,0,colName[i],new WritableCellFormat()));
                }

                for(int i=0;i<dataList.signInDataList.size();i++){

                    SignInData data = dataList.signInDataList.get(i);
                    for(int j=0;j<colName.length;j++){
                        switch (j){
                            case 0:
                                sheet.addCell(new Label(j,i,data.time,new WritableCellFormat()));
                                break;
                            case 1:
                                sheet.addCell(new Label(j,i,data.receiver,new WritableCellFormat()));
                                break;
                            case 2:
                                if("1".equals(data.result)){
                                    sheet.addCell(new Label(j,i,"成功",new WritableCellFormat()));
                                }else{

                                    sheet.addCell(new Label(j,i,"失败",new WritableCellFormat()));
                                }
                                break;
                            default:
                                break;
                        }

                    }
                }
            }catch (IOException e){
                Toast.makeText(context,"创建文件失败",Toast.LENGTH_SHORT).show();
            } catch (RowsExceededException e) {
                Toast.makeText(context,"创建文件失败",Toast.LENGTH_SHORT).show();

            } catch (WriteException e) {
                Toast.makeText(context,"创建文件失败",Toast.LENGTH_SHORT).show();

            }
        }
    }



    public static String getPath(String groupId){
        return PATH+"/xls"+"/"+groupId+".xls";
    }
}
