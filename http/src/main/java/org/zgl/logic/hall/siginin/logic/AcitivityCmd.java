package org.zgl.logic.hall.siginin.logic;

import org.zgl.jetty.operation.OperateCommandAbstract;
import org.zgl.jetty.session.SessionManager;
import org.zgl.logic.hall.shop.data.CommodityDataTable;
import org.zgl.logic.hall.shop.manager.ShopManager;
import org.zgl.logic.hall.weath.po.SQLWeathModel;
import org.zgl.player.UserMap;
import org.zgl.utils.builder_clazz.ann.Protocol;

import java.util.HashMap;
import java.util.Map;

@Protocol("10")
public class AcitivityCmd extends OperateCommandAbstract {
    public enum ActivityEnum{
        NONE(0),
        GIFT_BAG_10(1),
        GIFT_BAG_100(2),
        SYSTEM_ANNOUNCEMENT(3),
        MY_MESSAGE(4);
        private int id;
        private static final Map<Integer,ActivityEnum> map;
        static {
            map = new HashMap<>(6);
            for(ActivityEnum e:ActivityEnum.values()){
                map.putIfAbsent(e.id,e);
            }
        }
        ActivityEnum(int id) {
            this.id = id;
        }
        public int id(){
            return id;
        }
        public static ActivityEnum getEnum(int i){
            return map.get(i);
        }
    }
    /**活动中点那个按钮 1：10元礼包 2：星爷礼包 3：系统公告 4：我的消息（邮件）*/
    private final int id;
    public AcitivityCmd(int id, String account) {
        super(account);
        this.id = id;
    }

    @Override
    public Object execute() {
        ActivityEnum activityEnum = ActivityEnum.getEnum(id);
        UserMap userMap = SessionManager.getSession(getAccount());
        SQLWeathModel weath =userMap.getWeath();
        switch (activityEnum){
            case GIFT_BAG_10:
                CommodityDataTable dataTable = CommodityDataTable.get(22);
                ShopManager.getInstance().bay(userMap,dataTable,2);//vip铜卡
                weath.addIntegral(10);
                weath.addGoldOrDiamond(1,1200000);
                weath.update(userMap,true);
                break;
            case GIFT_BAG_100:
                CommodityDataTable dataTable1 = CommodityDataTable.get(24);
                ShopManager.getInstance().bay(userMap,dataTable1,2);//vip铜卡
                weath.addIntegral(100);
                weath.addGoldOrDiamond(1,13000000);
                weath.update(userMap,true);
                break;
            case SYSTEM_ANNOUNCEMENT:
                break;
            case MY_MESSAGE:
                break;
        }
        return null;
    }

    /**
     * 星爷礼包
     */
    private void giftBag100(){}

    /**
     * 系统公告
     */
    private void systemAnnouncement(){}

    /**
     * 我的消息
     */
    private void myMessage(){}
}
