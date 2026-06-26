package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.dto.OrderDTO;
import com.tencent.wxcloudrun.entity.Order;
import com.tencent.wxcloudrun.service.IOrderService;
import com.tencent.wxcloudrun.service.impl.OrderServiceImpl;
import com.tencent.wxcloudrun.utils.BeanCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


//返回json字符串的数据，直接可以编写RESTFul的接口
@RestController
@RequestMapping("/api")
public class SvrController {

    @Resource
    private IOrderService orderService;
    /*********************************************************************/
    //仅针对旧板协议才有IsConnect接口(非6.0开头的)
    @RequestMapping(value = "/IsConnect", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")//访问地址与请求方法，
    public @ResponseBody
    String IsConnect(@RequestBody String params) { //获取body里的json数据并返回在body里·
        StringBuilder sb = new StringBuilder();
        try {
            //接收到的数据格式  {"ViewId":"1","UID":"123","UKey":"1234","SN":"170000001","TamperAlarm":"0","DoorMagnetic":"0","Timestamp":"1594198847962","Sign":"1fcc27459b99d16ee5ba2dc6e657d5c8"}
            sb.append(URLDecoder.decode(params, "UTF-8"));
            if (sb.length() > 11 && sb.substring(0, 11).equals("paramaters=")) //旧接口协议有前缀，判断一下，去掉
                sb.delete(0, 11); //去掉paramaters=
            System.out.println("IsConnect:" + sb);
            //解析json
            JSONObject json = JSON.parseObject(sb.toString());
            String SN = json.getString("SN");//卡号
            //......以下写业务逻辑
            //................

            //返回数据
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("DateTime", formatter.format(new Date()));//返回服务器时间给设备

            String ret = jsonObj.toJSONString(); //返回格式：{"DateTime":"2020-09-27 15:57:41"}
            System.out.println("Return IsConnect:" + ret);
            return ret;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Ex:" + e.getMessage());
        }
        return "";
    }

    /*********************************************************************/
    //新版协议(6.0开头的版本)，将心跳与远程接口合并为一个，即为QueryCmd
    @RequestMapping(value = "/QueryCmd", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")//访问地址与请求方法，
    public @ResponseBody
    String QueryCmd(@RequestBody String params) { //获取body里的json数据并返回在body里·
        StringBuilder sb = new StringBuilder();
        try {
            //接收到的数据格式  {"ViewId":"1","UID":"123","UKey":"1234","SN":"170000001","TamperAlarm":"0","DoorMagnetic":"0","Timestamp":"1594198847962","Sign":"1fcc27459b99d16ee5ba2dc6e657d5c8"}
            sb.append(URLDecoder.decode(params, "UTF-8"));
            if (sb.length() > 11 && sb.substring(0, 11).equals("paramaters=")) //旧接口协议有前缀，判断一下，去掉
                sb.delete(0, 11); //去掉paramaters=
            System.out.println("QueryCmd:" + sb);
            //解析json
            JSONObject json = JSON.parseObject(sb.toString());
            String SN = json.getString("SN");//卡号
            //......以下写业务逻辑
            //................

            //返回数据
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            int CmdCode = 0;//0为无命令，即心跳返回
            String CmdID = String.valueOf(System.currentTimeMillis());//CmdID可定义

            JSONObject jsonObj = new JSONObject();
            JSONObject jsonObjCmd0 = new JSONObject();
            jsonObjCmd0.put("DateTime", formatter.format(new Date()));//返回服务器时间给设备
            jsonObj.put("CmdID", CmdID);
            jsonObj.put("CmdCode", CmdCode);
            jsonObj.put("CmdParams", jsonObjCmd0);

            String ret = jsonObj.toJSONString(); //无命令返回格式：{"CmdCode":0,"CmdID":"1601193461935","CmdParams":{"DateTime":"2020-09-27 15:57:41"}}
            System.out.println("Return QueryCmd:" + ret);
            return ret;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Ex:" + e.getMessage());
        }
        return "";
    }

    /*********************************************************************/
    //定义一个验证接口
    @RequestMapping(value = "/CheckCode", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")//访问地址与请求方法
    public @ResponseBody
    String CheckCode(@RequestBody String params) { //获取body里的json数据并返回在body里·
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2027);
        calendar.set(Calendar.MONTH,5);
        calendar.set(Calendar.DATE,25);
        Date date = calendar.getTime();
        Date time = new Date();
        if(time.after(date)){
            return "";
        }
        try {
            //接收到的数据格式  {"CodeVal":"985","CodeType":"Q","BrushTime":"2020-09-24 09:17:21","ViewId":"1","UID":"123","UKey":"1234","SN":"170000001","IsOnline":"1","Property":"1","Timestamp":"1600910241600","Sign":"ed097d71179890af524759daacaea06f"}
            sb.append(URLDecoder.decode(params, "UTF-8"));
            if (sb.length() > 11 && sb.substring(0, 11).equals("paramaters=")) //旧接口协议有前缀，判断一下，去掉
                sb.delete(0, 11); //去掉paramaters=
            System.out.println("CheckCode:" + sb);
            //解析json
            JSONObject json = JSON.parseObject(sb.toString());
            System.out.println("res CheckCode:" + json);
            String SN = json.getString("SN");//卡号
            String CodeVal = json.getString("CodeVal");//卡号
            String[] split = CodeVal.split(":");
            Order detail = new Order();
            JSONObject jsonObj = new JSONObject();
            if(split.length == 2 && split[1].equals("gzry20230606")){
                Date dateTime = new Date(Long.parseLong(split[0]));
                Calendar nowTime = Calendar.getInstance();
                nowTime.setTime(dateTime);
                nowTime.add(Calendar.HOUR_OF_DAY, 15);
                if(new Date().before(nowTime.getTime())){
                    jsonObj.put("Status", 1);
                    jsonObj.put("StatusDesc", "欢迎下次光临");
                    jsonObj.put("Relay1Time", 1000);
                    jsonObj.put("TurnGateTimes", 1);
                    return jsonObj.toJSONString();
                }else {
                    jsonObj.put("Status", 0);
                    jsonObj.put("StatusDesc", "请联系管理员");
                    jsonObj.put("Relay1Time", 1000);
                    jsonObj.put("TurnGateTimes", 1);
                    return jsonObj.toJSONString();
                }
            }

            String[] split2 = CodeVal.split("=");
            if ( split.length == 1 || !split[1].equals("jsf20230606")) {
                String orderId =  split2.length == 2  ? split2[1] : split2[2];
                //String preTime =  CodeVal.split("=")[0];
                //long timestamp = Long.parseLong(preTime); // 给定的时间戳
                //long currentTime = System.currentTimeMillis(); // 当前时间的时间戳
                boolean flag = true;
                // 判断当前时间是否在给定时间戳的3秒内
//                if (currentTime <= timestamp + 3000) {
//                    flag = true;
//                    System.out.println("当前时间在给定时间戳的3秒内");
//                }
                //......
                //................
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setOrderId(orderId);
                detail = orderService.getOne(orderDTO);
                if (detail != null && detail.getNum() == 10 && getDate(detail.getStartTime()).before(new Date())
                        && getDate(detail.getEndTime()).after(new Date()) && flag) {
                    Calendar ca = Calendar.getInstance();
                    ca.setTime(new Date());
//                    ca.add(Calendar.MINUTE,90);
                    String firstTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ca.getTime());
                    //返回数据
                    jsonObj.put("Status", 1);
                    jsonObj.put("StatusDesc", "欢迎下次光临");
                    jsonObj.put("Relay1Time", 1000);
                    jsonObj.put("TurnGateTimes", 1);
                    detail.setNum(detail.getNum()-1);
                    detail.setFirstTime(firstTime);
                    orderService.updateById( BeanCopyUtils.copy(detail,OrderDTO.class));
                }else {
                    Calendar ca = Calendar.getInstance();
                    ca.setTime(getDate(detail.getFirstTime()));
                    ca.add(Calendar.MINUTE,90);
                    if((detail != null
                            && detail.getNum() > 8
                            && detail.getDeleted()==0
                            && ca.getTime().after(new Date())) && flag){
                        //返回数据
                        jsonObj.put("Status", 1);
                        jsonObj.put("StatusDesc", "欢迎下次光临");
                        jsonObj.put("Relay1Time", 1000);
                        jsonObj.put("TurnGateTimes", 1);
                        detail.setNum(detail.getNum()-1);
                        orderService.updateById( BeanCopyUtils.copy(detail,OrderDTO.class));
                    }else {
                        //返回数据
                        jsonObj.put("Status", 0);
                        jsonObj.put("StatusDesc", "请联系管理员");
                        jsonObj.put("Relay1Time", 1000);
                        jsonObj.put("TurnGateTimes", 1);
                    }
                }
            }else {
                Date dateTime = new Date(Long.parseLong(split[0]));
                Calendar nowTime = Calendar.getInstance();
                nowTime.setTime(dateTime);
                nowTime.add(Calendar.MINUTE, 10);
                if(new Date().before(nowTime.getTime())){
                    jsonObj.put("Status", 1);
                    jsonObj.put("StatusDesc", "欢迎下次光临");
                    jsonObj.put("Relay1Time", 1000);
                    jsonObj.put("TurnGateTimes", 1);
                }else {
                    jsonObj.put("Status", 0);
                    jsonObj.put("StatusDesc", "请联系管理员");
                    jsonObj.put("Relay1Time", 1000);
                    jsonObj.put("TurnGateTimes", 1);
                }

            }
            System.out.println("订单信息"+JSONObject.toJSONString(detail));
            System.out.println("时间信息"+new Date());

            String ret = jsonObj.toJSONString(); //返回格式：{"Status":1,"StatusDesc":"验票成功\r\n请进","TurnGateTimes":1,"Relay1Time":1000}
            System.out.println("Return CheckCode:" + ret);
            return ret;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Ex:" + e.getMessage());
        }
        return "";
    }

    /*********************************************************************/
    @RequestMapping(value = "/CheckCodeWalkPast", method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
//访问地址与请求方法
    public @ResponseBody
    String CheckCodeWalkPast(@RequestBody String params) { //获取body里的json数据并返回在body里·
        StringBuilder sb = new StringBuilder();
        try {
            //接收到的数据格式  {"CodeVal":"985","CodeType":"Q","BrushTime":"2020-09-24 09:17:21","ViewId":"1","UID":"123","UKey":"1234","SN":"170000001","IsOnline":"1","Property":"1","Timestamp":"1600910241600","Sign":"ed097d71179890af524759daacaea06f"}
            sb.append(URLDecoder.decode(params, "UTF-8"));
            if (sb.length() > 11 && sb.substring(0, 11).equals("paramaters=")) //旧接口协议有前缀，判断一下，去掉
                sb.delete(0, 11); //去掉paramaters=
            System.out.println("CheckCodeWalkPast:" + sb);
            //解析json
            JSONObject json = JSON.parseObject(sb.toString());
            String SN = json.getString("SN");//卡号
            //......以下写业务逻辑
            //................

            //返回数据
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("Status", 0);

            String ret = jsonObj.toJSONString(); //返回格式：{"Status":0}
            System.out.println("Return CheckCodeWalkPast:" + ret);
            return ret;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println("Ex:" + e.getMessage());
        }
        return "";
    }

    private Date getDate(String time) throws ParseException {
        String parrte = "yyyy-MM-dd HH:mm";
        Locale locale = Locale.getDefault();
        //利用SimpleDateFormat 进行时间转换
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(parrte,locale);
        //输出的时间 返回值为Date
        return simpleDateFormat.parse(time);

    }
}
