package com.seventyseven.adskiper.service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import com.seventyseven.adskiper.db.RecordDao;

public class SkipService extends AccessibilityService {
    
    private static final String btn_id = "android.widget.Button";
    private static final String tv_id = "android.widget.TextView";
    private static int DURATION = 3000;//跳过间隔
    private static long LastTime = 0;

    //互斥处理
    private static boolean isIntercepting = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //服务连接
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    //接收事件
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo rootNode = this.getRootInActiveWindow();
        if(rootNode == null){
            return;
        }
        switch (accessibilityEvent.getEventType()){
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED :
                Toast.makeText(getApplicationContext(),"Notification ",Toast.LENGTH_SHORT).show();
                Log.d("Notification", " ");
                //通知栏事件
                dealNotification(rootNode,accessibilityEvent);
                break;
            default:
                dealADs(rootNode,accessibilityEvent);
                break;
        }
    }

    private void dealADs(AccessibilityNodeInfo rootNode, AccessibilityEvent accessibilityEvent) {
        //过滤掉桌面，白名单
        String packageName = String.valueOf(rootNode.getPackageName());
        if(packageName.isEmpty() || packageName.indexOf("launcher") > -1){
            return;
        }
        if(packageName.indexOf("system") > -1){
            return;
        }
        if(packageName.indexOf("seventyseven") > -1){
            return;
        }
        long d = System.currentTimeMillis() - LastTime;
        // 时间间隔在频率范围内不处理
        if(d < DURATION){
            return;
        }
        if(!isIntercepting){
            isIntercepting = true;
            AccessibilityNodeInfo node = findCurrentNode(rootNode);
            if(node != null){
                Log.d("db***"," clicked");
                //点击跳过控件
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LastTime = System.currentTimeMillis();
                RecordDao.saveRecordByPackageName(packageName,this);
            }
        }
        isIntercepting = false;
    }

    //查找包含跳过字样的Button/TextView
    private AccessibilityNodeInfo findCurrentNode(AccessibilityNodeInfo info){

        int count = info.getChildCount();
        if(count > 0){
            for(int i = 0; i < count; i++){
                AccessibilityNodeInfo childNode = info.getChild(i);
                if(childNode == null){
                    continue;
                }
                //递归查找
                AccessibilityNodeInfo currentNode = findCurrentNode(childNode);
                if(currentNode != null){
                    return currentNode;
                }
            }
        } else {
            //直接查找Button Textview
            String text = String.valueOf(info.getText());
            if(btn_id.equals(info.getClassName()) || tv_id.equals(info.getClassName())){
                // 包含跳过文本的才是目标node
                if(!text.isEmpty() && text.indexOf("跳过") > -1){
                    if (info.isClickable()) {
                        return info;
                    } else {
                        AccessibilityNodeInfo parent = info.getParent();
                        if (parent.isClickable()) {
                            return parent;
                        }
                    }
                }
            }
        }
        return null;
    }

    //通知栏事件
    private void dealNotification(AccessibilityNodeInfo rootNode, AccessibilityEvent accessibilityEvent) {
        Parcelable data = accessibilityEvent.getParcelableData();
        if (data instanceof Notification) {
            Notification notification = (Notification) data;
            Bundle extras = notification.extras;
            if (extras != null) {
                String title = extras.getString(Notification.EXTRA_TITLE, "");
                String content = extras.getString(Notification.EXTRA_TEXT, "");
                Log.d("Notifications", " " + title + "  " + content);
            }
        }
    }

    // 服务中断
    @Override
    public void onInterrupt() {
        Toast.makeText(getApplicationContext(),"广告拦截服务已关闭",Toast.LENGTH_SHORT).show();
    }
}
