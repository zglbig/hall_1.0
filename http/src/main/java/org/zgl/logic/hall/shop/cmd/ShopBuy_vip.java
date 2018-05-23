package org.zgl.logic.hall.shop.cmd;

import org.zgl.error.LogAppError;
import org.zgl.jetty.operation.OperateCommandAbstract;
import org.zgl.jetty.session.SessionManager;
import org.zgl.logic.hall.shop.data.CommodityDataTable;
import org.zgl.logic.hall.shop.manager.ShopManager;
import org.zgl.player.UserMap;
import org.zgl.utils.builder_clazz.ann.Protocol;

import javax.servlet.http.HttpServletResponse;

@Protocol("7")
public class ShopBuy_vip extends OperateCommandAbstract {
    private final int commodityId;
    public ShopBuy_vip(int commodityId, String account) {
        super(account);
        this.commodityId = commodityId;
    }

    @Override
    public Object execute() {
        CommodityDataTable dataTable = CommodityDataTable.get(commodityId);
        if(dataTable == null)
            new LogAppError("获取不到id为:"+commodityId+" 商城对应的物品");
        UserMap userMap = SessionManager.getSession(getAccount());
        return ShopManager.getInstance().bay(userMap,dataTable,commodityId);
    }
}
