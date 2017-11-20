package com.womai.wms.rf.manager.window.switchwh;

import com.womai.wms.rf.common.constants.Constants;
import com.womai.wms.rf.common.constants.KeyEnum;
import com.womai.wms.rf.common.constants.WhEnum;
import com.womai.wms.rf.common.util.HandlerUtil;
import com.womai.wms.rf.common.util.RFUtil;
import com.womai.wms.rf.common.util.RegExpUtil;
import com.womai.wms.rf.manager.util.ReceiveManager;
import com.womai.wms.rf.manager.util.UserCache;
import com.womai.wms.rf.manager.util.UserMenuAuthCache;
import com.womai.wms.rf.manager.window.UserAndSiteParamManager;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 切换仓库界面
 * User: zhangwei
 * Date: 2016-05-03
 * To change this template use File | Settings | File Templates.
 */
@Scope("prototype")
@Component("switchWarehouseShellManager")
public class SwitchWarehouseShellManagerImpl extends ReceiveManager {

	@Autowired
	UserMenuAuthCache userMenuAuthCache;

	private int currentPageNum;//站点当前页码
	private int currentTotalCount;//总条数
	private int siteInCurPage;//当前页显示的站点数目
	private String temp_str = "";//用于接收输入

	@Override
	public void channelActive(ChannelHandlerContext ctx)  throws Exception {
		currentPageNum = Constants.PAGE_START;
		showUserWareHouse(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
		if(super.anyKeyToLogIn){//如果父类中的标识为true则跳转回登录，此标识在BaseShellManager捕获异常的方法exceptionCaught中设置
			forward(Constants.LOGIN_SHELL_MANAGER, ctx);
			return;
		}
		String received = object.toString();
		String str = (null == temp_str ? "" : temp_str);
		if ((received.equals(KeyEnum.B_66.value) && received.getBytes()[0] == KeyEnum.B_66.code)
				|| (received.equals(KeyEnum.b_98.value) && received.getBytes()[0] == KeyEnum.b_98.code)) {
			//上一页，当前页码减一
			currentPageNum = (currentPageNum <= 1 ? 1 : currentPageNum - 1);
			showUserWareHouse(ctx);
		} else if ((received.equals(KeyEnum.N_78.value) && received.getBytes()[0] == KeyEnum.N_78.code)
				|| (received.equals(KeyEnum.n_110.value) && received.getBytes()[0] == KeyEnum.n_110.code)) {
			//下一页，当前页码加一，且不能大于总页数
			Integer currentPage = currentPageNum;
			Integer totalPage = currentTotalCount / Constants.PAGE_SIZE + 1;
			if (currentPage < totalPage) {
				currentPageNum = (currentPageNum + 1);
				showUserWareHouse(ctx);
			}
		} else if (received.getBytes()[0] == KeyEnum.CR_13.code) {
			if (!forwardMainView(ctx, str)) {
				if (!RegExpUtil.matchPureNum(str) || StringUtils.isBlank(str) || str.length() > 10) {
					printErrorMess(ctx, "");
				} else {
					Integer wareHouseSeq = Integer.parseInt(str);
					if (wareHouseSeq <= 0 || wareHouseSeq > currentTotalCount) {
						printErrorMess(ctx, "");
					} else {
						List<String> whCodeList = RFUtil.str2List(UserCache.getUser(getCurrentUserId(ctx)).getSite(), ",");
						String selectedSite = whCodeList.get(wareHouseSeq - 1);
						//切换站点后首先查询一次该站点是否存在权限
						boolean existAuthority = userMenuAuthCache.isExistAuthorityBySite(getCurrentUserId(ctx), selectedSite);// 根据菜单等级、menuid等参数刷新菜单内容
						if (existAuthority) {
							UserAndSiteParamManager us = (UserAndSiteParamManager) ctx.pipeline().first();
							us.setCurrentSite(selectedSite);
							//切换站点后回到主菜单
							forward(Constants.MENU_SHELL_MANAGER, ctx);
						} else {
							HandlerUtil.errorBeep(ctx);//系统错误，响铃
							String errorMess = WhEnum.getWhName(selectedSite) + Constants.MENU_IS_NULL_TIP_SUFFIX;
							printErrorMess(ctx, errorMess);
						}
					}
				}
			}
			temp_str = "";
		} else {
			temp_str = inputStr(ctx, received, str);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	/**
	 * 仓库数据分页
	 *
	 * @param ctx handler对象
	 * @throws Exception 编码转换异常
	 */
	private void showUserWareHouse(ChannelHandlerContext ctx) {
		List<String> whCodeList = RFUtil.str2List(UserCache.getUser(getCurrentUserId(ctx)).getSite(), ",");
		Integer start = (currentPageNum - 1) * Constants.PAGE_SIZE;//获取数组循环开始位，且不能小于0
		if (start <= 0) {
			start = 0;
		}
		Integer end = currentPageNum * Constants.PAGE_SIZE;//获取数组循环结束位，且不能大于数组size
		if (end >= whCodeList.size()) {
			end = whCodeList.size();
		}
		HandlerUtil.clearAll(ctx.channel());//清屏，重新输入提示语
		String[] outstr = {"", Constants.SWITCH_WAREHOUSE + "(当前:" + WhEnum.getWhName(getCurrentSite(ctx)) + ")", Constants.SPLIT, ""};
		HandlerUtil.writer(ctx, outstr, 0, 1);
		//循环数组获取仓库编码，输出仓库名称
		for (int i = start; i < end; i++) {
			String wareHouseName = i + 1 + ")" + WhEnum.getWhName(whCodeList.get(i)) + Constants.BREAK_LINE;
			HandlerUtil.write(ctx, wareHouseName);
		}
		//计算当前页码/总页码
		Integer currentPage = currentPageNum;
		Integer totalPage = whCodeList.size() / Constants.PAGE_SIZE + 1;
		if (currentTotalCount == 0) {
			currentTotalCount = whCodeList.size();
		}
		siteInCurPage = end-start;
		Integer totalCount = currentTotalCount == 0 ? whCodeList.size() : currentTotalCount;
		String pageInfo = Constants.PAGE_TURING_TIP + RFUtil.getPageInfo(currentPage, totalPage, totalCount) + Constants.BREAK_LINE;
		HandlerUtil.write(ctx, pageInfo);

		Map<String, Object> switchMap = new HashMap<String, Object>();
		switchMap.put("currentSiteName", WhEnum.getWhName(getCurrentSite(ctx)));
		String switchTip = RFUtil.composeMessage(Constants.SELECT_WAREHOUSE, switchMap);
		HandlerUtil.writeAndFlush(ctx, switchTip);

	}

	/**
	 * 打印错误信息，用于提示错误的仓库编码输入
	 *
	 * @param ctx
	 */
	private void printErrorMess(ChannelHandlerContext ctx, String errorMess) {
		if (StringUtils.isBlank(errorMess)) {
			HandlerUtil.errorBeep(ctx);//系统错误，响铃
			errorMess = Constants.SELECT_WAREHOUSE_ERROR;
		}
		HandlerUtil.clearOneRow(ctx);
		String[] out = {Constants.BREAK_LINE, errorMess, Constants.SELECT_WAREHOUSE_TIP};
		//数字4，包含第0行提示当前仓库，第1行提示一串横线(----)，一行BN提示，再一行当前仓库，siteInCurPage为当前页显示的仓库数量，
		HandlerUtil.writer(ctx, out, 4+siteInCurPage, 0);
	}

}
