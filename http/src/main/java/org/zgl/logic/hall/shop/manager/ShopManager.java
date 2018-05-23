package org.zgl.logic.hall.shop.manager;


import org.zgl.error.AppErrorCode;
import org.zgl.error.GenaryAppError;
import org.zgl.error.LogAppError;
import org.zgl.logic.hall.frineds.po.SQLFrinedsModel;
import org.zgl.logic.hall.shop.ShopEnum;
import org.zgl.logic.hall.shop.data.CommodityDataTable;
import org.zgl.logic.hall.task.manager.TaskManager;
import org.zgl.logic.hall.vip.dto.VIPDto;
import org.zgl.logic.hall.vip.po.VipDataTable;
import org.zgl.logic.hall.weath.dto.WeathResourceDto;
import org.zgl.logic.hall.weath.po.SQLMoenyTree;
import org.zgl.logic.hall.weath.po.SQLWeathModel;
import org.zgl.orm.core.Query;
import org.zgl.orm.core.QueryFactory;
import org.zgl.orm.po.User;
import org.zgl.player.UserMap;
import org.zgl.utils.DateUtils;

public class ShopManager {
    private static ShopManager instance;

    public static ShopManager getInstance() {
        if(instance == null)
            instance = new ShopManager();
        return instance;
    }
    public Object bay(UserMap um, CommodityDataTable dataTable, int commodityId){
        SQLWeathModel weath = um.getWeath();
        ShopEnum shopEnum = ShopEnum.getEnum(dataTable.getShopId());
        int count = dataTable.getCount();
        Query query = QueryFactory.createQuery();
//        User user = um.entity2map();
        Object o = null;
        switch (shopEnum){
            case GOLD:
                o = new WeathResourceDto(count,0,0);
                break;
            case AUTO:
                bayAuto(weath,dataTable,count);
                TaskManager.getInstance().listener(um,11);//购买座驾
                User user = um.entity2map();
                query.update(user,new String[]{"task"});
                break;
            case PROP:
                TaskManager.getInstance().listener(um,9);//道具
                User user2 = um.entity2map();
                query.update(user2,new String[]{"task"});
                break;
            case VIP:
                //返回vip当前等级和经验
                o = bayVip(weath,dataTable,count);
                //好友上限人数
                VipDataTable vipDataTable = VipDataTable.get(((VIPDto) o).getVipLv());
                SQLFrinedsModel frineds = um.getFriends();
                int frintCount = frineds.getCountLimit();
                if(vipDataTable != null){
                    frintCount = vipDataTable.getFriendCount();
                }
                frineds.setCountLimit(frintCount);
                User user1 = um.entity2map();
                query.update(user1,new String[]{"friends"});
                query.update(user1,new String[]{"weath"});
                TaskManager.getInstance().listener(um,10);//vip勋章
                break;
            case EXCHANGE:
                exchange(weath,dataTable,count);
                TaskManager.getInstance().listener(um,6);
                break;
            case MONEY_TREE:
                moneyTree(weath);
                TaskManager.getInstance().listener(um,12);//获得摇钱树
                break;
        }

        if(dataTable.getConsumeType() == 1){
            weath.addIntegral(dataTable.getIntegral());//加积分
        }
        weath.addResource(shopEnum,commodityId,count);
        //TODO...
        if(shopEnum != ShopEnum.VIP)
            weath.update(um,true);
        return o;
    }

    /**
     * 购买座驾
     */
    private Object bayAuto(SQLWeathModel weath,CommodityDataTable dataTable,int count){
        if(weath.getVipLv() < dataTable.getVIPLimitLv())
            new GenaryAppError(AppErrorCode.VIP_LV_ERR);//throw new RuntimeException("您的vip等级不足");
        if(!weath.reduceGold(dataTable.getSelling()*count))
            new GenaryAppError(AppErrorCode.GOLD_NOT_ERR);
        //购买第一个时默认使用
        if(weath.getAuto() == 0)
            weath.setAuto(dataTable.getId());
        return null;
    }

    /**
     * 购买道具
     */
    private void bayProp(){}

    /**
     * 购买vip
     */
    private VIPDto bayVip(SQLWeathModel weath,CommodityDataTable dataTable,int count){
        int exp = 0;
        try {
            exp = Integer.parseInt(dataTable.getEffect());
        }catch (NumberFormatException e){
            new LogAppError("获取不到vip的经验值");
        }
        //vip等级、经验
        int vipLv = weath.getVipLv();
        long vipExp = weath.getVipExp();
        long vipBuyExp = exp * count;//购买到的经验
        //vip升到下一级所需经验
        vipExp += vipBuyExp;//vip本次购买之后的总经验（用这个去算能升几级）
        while (true) {
            VipDataTable vipDataTable = VipDataTable.get(vipLv + 1);
            //升级下一级所需经验减去vip当前经验+本次购买获得的经验如果小于0说明要升级
            long vipTemp = vipDataTable.getExp() - vipExp;
            if (vipTemp < 0) {
                //vip等级+1
                vipLv++;
                vipExp -= vipDataTable.getExp();
                //经验值归0
            }else {
                break;
            }
        }
        //设置vip等级
        weath.setVipLv(vipLv);
        //设置vip经验
        weath.setVipExp(vipExp);
        return new VIPDto(vipLv,vipExp);
    }

    /**
     * 兑换
     */
    private void exchange(SQLWeathModel weath,CommodityDataTable dataTable,int count){
        if(weath.getVipLv() < dataTable.getVIPLimitLv())
            new GenaryAppError(AppErrorCode.VIP_LV_ERR);
        if(weath.reduceIntegral(dataTable.getSelling()*count))
            new GenaryAppError(AppErrorCode.INTEGRAL_NOT_ERR);

    }

    /**
     * 购买摇钱树
     */
    private void moneyTree(SQLWeathModel weath){
        if(weath.getVipLv() <= 0)
            new GenaryAppError(AppErrorCode.VIP_LV_ERR);
        SQLMoenyTree moenyTree = weath.getMoneyTree();
        moenyTree.setLv(weath.getVipLv());
        moenyTree.setTimer(DateUtils.currentTime());
//        DateUtils
    }
}
