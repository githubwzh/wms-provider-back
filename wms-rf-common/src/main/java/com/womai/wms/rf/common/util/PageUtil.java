package com.womai.wms.rf.common.util;

import com.womai.common.framework.domain.PageModel;
import com.womai.wms.rf.common.constants.CheckReasonEnum;
import com.womai.wms.rf.common.constants.Constants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 分页查询工具类
 * Created by wangzhanhua on 2016/7/5.
 */
public class PageUtil {
	public static final String PARA_PAGE_MAP = "para_page_map";//查询分页的Map
	public static final String PAGE_MODEL = "page_model";//存放分页模型
	public final static int ROW_FIXED = 5;//列表固定的行（表头+表尾）
	public static final int HIDDEN_SERIALNO = -1;//隐藏序号
	public static final int NO_BEYOND_LENGTH = -1;//字段值没有超过列宽，不用换行
	public static final String LINES_NUM_CLEAN_KEY = "lines_num_clean_key";//翻页时，需要清除的数据行数
	public static final String SKU_STATUS = "skuStatus";//查询库存分页时，显示该属性对应的中文意思

	/**
	 * 显示分页列表
	 * （pn-1）*pageSize+index+1==serialno
	 * pn:页码，pageSize：一页显示的数据数。index 当前页面数据集合的下标。serialno 序号
	 *
	 * @param ctx
	 * @param pageModel        页：总共多少页，第几页，一页显示多少数据，总共多少数据，数据集合
	 * @param tableHeader      列表头名字
	 * @param colNames         表头对应的显示字段
	 * @param isShowSerialno   是否显示序号
	 * @param isAutoChangeLine 是否自动换行
	 * @param note             该字符串显示在分页列表页脚上面，分页数据下面
	 * @return 该列表所占用的行数
	 */
	public static int showTable(ChannelHandlerContext ctx, PageModel pageModel, String[] tableHeader, String[] colNames, boolean isShowSerialno, boolean isAutoChangeLine, String note) {
		int[] colWidth = getColWidth(tableHeader);//数字代表对应的字段的宽度（字节，一个汉字两个字节）
		HandlerUtil.printTableHeader(ctx, tableHeader, colWidth);//菜单头
		List<List<String>> list = new ArrayList<List<String>>();
		List datas = pageModel.getDatas();
		int serialnoOffSet = (pageModel.getPageNum() - 1) * pageModel.getPageSize() + 1;
		for (int i = 0; i < datas.size(); i++) {
			int serialno = HIDDEN_SERIALNO;//-1时不显示序号
			if (isShowSerialno) {//显示序号
				serialno = i + serialnoOffSet;
			}
			List<List<String>> tempList = new ArrayList<List<String>>();//临时存放字段超列宽，被截取下来的字符串对应的List
			List<String> stringValues = new ArrayList<String>();
			for (int j = 0; j < colNames.length; j++) {
				String value = getValueByColName(datas.get(i), colNames[j]);
				if (isAutoChangeLine) {
					int width;
					if (serialno == HIDDEN_SERIALNO) {//不显示序号
						width = colWidth[j];
					} else {
						width = colWidth[j + 1];
					}
					tempList = processAutoLine(tempList, value, j, width, serialno);//处理自动换行
				} else {
					if (serialno == HIDDEN_SERIALNO) {//不显示序号
						stringValues.add(value);
					} else {
						if (j == 0) {
							stringValues.add(serialno + "");
						}
						stringValues.add(value);
					}
				}
			}
			if (isAutoChangeLine) {
				list.addAll(tempList);
			} else {
				list.add(stringValues);
			}
		}
		printTable(ctx, list, colWidth, note);//输出表内容
		int countLines = 0;
		if (StringUtils.isNotBlank(note)) {
			countLines = note.split(Constants.BREAK_LINE).length;
		}
		String pageInfo = Constants.PAGE_TURING_TIP + RFUtil.getPageInfo(pageModel.getPageNum(), pageModel.getTotalPageNum(), pageModel.getTotalCount()) + Constants.BREAK_LINE;//输出页脚
		HandlerUtil.write(ctx, pageInfo);
		return list.size() + countLines;//当前页，所显示的数据行数
	}

	/**
	 * 显示分页列表
	 * （pn-1）*pageSize+index+1==serialno
	 * pn:页码，pageSize：一页显示的数据数。index 当前页面数据集合的下标。serialno 序号
	 *
	 * @param ctx
	 * @param pageModel        页：总共多少页，第几页，一页显示多少数据，总共多少数据，数据集合
	 * @param tableHeader      列表头名字
	 * @param colNames         表头对应的显示字段
	 * @param colNamesNeedCut  需要截取的字段，根据列宽自动截取
	 * @param isShowSerialno   是否显示序号
	 * @param isAutoChangeLine 是否自动换行
	 * @param note             该字符串显示在分页列表页脚上面，分页数据下面
	 * @return 该列表所占用的行数
	 */
	public static int showTableCanSetCutColName(ChannelHandlerContext ctx, PageModel pageModel, String[] tableHeader, String[] colNames, Set<String> colNamesNeedCut, boolean isShowSerialno, boolean isAutoChangeLine, String note) {
		int[] colWidth = getColWidth(tableHeader);//数字代表对应的字段的宽度（字节，一个汉字两个字节）
		HandlerUtil.printTableHeader(ctx, tableHeader, colWidth);//菜单头
		List<List<String>> list = new ArrayList<List<String>>();
		List datas = pageModel.getDatas();
		int serialnoOffSet = (pageModel.getPageNum() - 1) * pageModel.getPageSize() + 1;
		for (int i = 0; i < datas.size(); i++) {
			int serialno = HIDDEN_SERIALNO;//-1时不显示序号
			if (isShowSerialno) {//显示序号
				serialno = i + serialnoOffSet;
			}
			List<List<String>> tempList = new ArrayList<List<String>>();//临时存放字段超列宽，被截取下来的字符串对应的List
			List<String> stringValues = new ArrayList<String>();
			for (int j = 0; j < colNames.length; j++) {
				String value = getValueByColName(datas.get(i), colNames[j]);
				int width;
				if (serialno == HIDDEN_SERIALNO) {//不显示序号
					width = colWidth[j];
				} else {
					width = colWidth[j + 1];
				}
				if (colNamesNeedCut.contains(colNames[j])) {//set集合中给定的需要截取的字段
					int len = 0;
					int index = 0;
					boolean flag = false;
					for (Character character : value.toCharArray()) {
						if (isChinese(character)) {
							len += 2;
						} else {
							len++;
						}
						if (flag = len > width - 4) {
							break;
						} else {
							index++;
						}
					}
					if (flag && index > 2) {
						value = value.substring(0, index) + Constants.PAGE_COLNAME_CUT_SUF;
					}
				}
				if (isAutoChangeLine) {
					tempList = processAutoLine(tempList, value, j, width, serialno);//处理自动换行
				} else {
					if (serialno == HIDDEN_SERIALNO) {//不显示序号
						stringValues.add(value);
					} else {
						if (j == 0) {
							stringValues.add(serialno + "");
						}
						stringValues.add(value);
					}
				}
			}
			if (isAutoChangeLine) {
				list.addAll(tempList);
			} else {
				list.add(stringValues);
			}
		}
		printTable(ctx, list, colWidth, note);//输出表内容
		int countLines = 0;
		if (StringUtils.isNotBlank(note)) {
			countLines = note.split(Constants.BREAK_LINE).length;
		}
		String pageInfo = Constants.PAGE_TURING_TIP + RFUtil.getPageInfo(pageModel.getPageNum(), pageModel.getTotalPageNum(), pageModel.getTotalCount()) + Constants.BREAK_LINE;//输出页脚
		HandlerUtil.write(ctx, pageInfo);
		return list.size() + countLines;//当前页，所显示的数据行数
	}

	/**
	 * 根据对象属性名获得对象值
	 *
	 * @param object 对象
	 * @param param  属性名
	 * @return 属性值
	 */
	private static String getValueByColName(Object object, String param) {
		Object obj = Reflections.getFieldValue(object, param);
		if (obj == null) {
			return "";
		}
		if (obj instanceof Date) {
			return DateTimeUtil.getStringWithSeparator((Date) obj).toString();
		}
		if (param.equals(SKU_STATUS)) {
			return CheckReasonEnum.getNameByValue((Integer) obj);
		}
		return obj.toString();
	}

	/**
	 * 打印表格内容
	 *
	 * @param ctx
	 * @param list     该集合中的每个元素为一个行List
	 * @param colWidth 表头名称对应的字符串长度
	 * @param note     数据列表最下面的提示语
	 */
	private static void printTable(ChannelHandlerContext ctx, List<List<String>> list, int[] colWidth, String note) {
		Channel channel = ctx.channel();
		for (int j = 0; j < list.size(); j++) {
			int iStart = 0;
			for (int i = 0; i < list.get(j).size(); i++) {
				String str = list.get(j).get(i);
				HandlerUtil.drawString(channel, str);
				int wordLenght = RFUtil.getWordCount(str);//字段对应的长度
				HandlerUtil.moveCursor(ctx, colWidth[i + iStart] - wordLenght + 1);
				HandlerUtil.drawString(channel, "|");
			}
			HandlerUtil.drawString(channel, Constants.BREAK_LINE);//一条记录结束，回车换行
		}
		if (StringUtils.isNotBlank(note)) {
			HandlerUtil.drawString(channel, note);
			HandlerUtil.changeRow(ctx);
		}
		HandlerUtil.drawString(channel, Constants.SPLIT);//分割线
		HandlerUtil.drawString(channel, Constants.BREAK_LINE);
		channel.flush();
	}

	/**
	 * 处理自动换行
	 *
	 * @param tempPageList 存放行List
	 * @param str          字段值
	 * @param columnNo     字段在该行中第几个显示（0,1,2...)
	 * @param maxLength    字段允许最大长度
	 * @param serialno     -1时不显示序号，否则显示序号
	 * @return 存放行元素的List
	 */
	private static List<List<String>> processAutoLine(List<List<String>> tempPageList, String str, int columnNo, int maxLength, int serialno) {
		int endIndex;
		int pageIndex = 0;
		while ((endIndex = endIndexForCutString(str, maxLength)) != NO_BEYOND_LENGTH) {
			String subFormarStr = str.substring(0, endIndex);
			pageIndex = setSubStringToList(tempPageList, pageIndex, subFormarStr, columnNo, serialno);
			str = str.substring(endIndex);
		}
		setSubStringToList(tempPageList, pageIndex, str, columnNo, serialno);
		return tempPageList;
	}

	/**
	 * @param tempPageList 存放行元素（List）的临时List
	 * @param pageIndex    行元素在临时List中的下标
	 * @param subFormarStr 行List对应的字段值
	 * @param columnNo     该字段在行中从左到右的序号（0,1,2...）
	 * @return 要操作的行List在临时List中的下标
	 */
	private static int setSubStringToList(List<List<String>> tempPageList, int pageIndex, String subFormarStr, int columnNo, int serialno) {
		List<String> strs;
		if (pageIndex >= tempPageList.size()) {
			strs = new ArrayList<String>();
			if (columnNo == 0 && pageIndex == 0 && serialno != HIDDEN_SERIALNO) {//序号
				strs.add("" + serialno);
			} else if (serialno != HIDDEN_SERIALNO) {//换行时，序号处补空
				strs.add("");
			}
			while (columnNo > 0) {
				strs.add("");//换行，前面字段补空
				columnNo--;
			}
			tempPageList.add(strs);
		} else {
			strs = tempPageList.get(pageIndex);
		}
		strs.add(subFormarStr);
		return ++pageIndex;
	}

	/**
	 * 如果字段超过列宽，返回截取字符串的结束下标
	 *
	 * @param string 字符串
	 * @param width  字符串允许的长度，页面字段列宽
	 * @return 字符串不超列宽返回-1，超过列宽返回对应的下标
	 */
	private static int endIndexForCutString(String string, int width) {
		int endIndex = 0;
		int charLength = 0;
		for (Character character : string.toCharArray()) {
			endIndex++;
			if (isChinese(character)) {
				charLength += 2;
			} else {
				charLength++;
			}
			if (charLength > width) {
				return endIndex - 1;
			}
		}
		return NO_BEYOND_LENGTH;
	}

	/**
	 * 判断字符是否为中文
	 *
	 * @param c 字符
	 * @return 中文true否则false
	 */
	private static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION)
		{
			return true;
		}
		return false;
	}

	/**
	 * 根据表头名字获得对应的名字长度
	 *
	 * @param tableHeader 表头名字
	 * @return 名字对应的长度
	 */
	private static int[] getColWidth(String[] tableHeader) {
		int[] colWidth = new int[tableHeader.length];
		for (int i = 0; i < tableHeader.length; i++) {
			colWidth[i] = RFUtil.getWordCount(tableHeader[i]);
		}
		return colWidth;
	}

	/**
	 * 根据列表选择号，获得对应的行数据对象
	 *
	 * @param pageModle      分页查询PageModel
	 * @param reasonSerialno 选择号 不小于0，最多十位数
	 * @return 根据行号，对应的对象
	 */
	public static Object getDataBySerialno(PageModel pageModle, String reasonSerialno) {
		List list = pageModle.getDatas();
		int maxIndex = list.size() - 1;
		int index = Integer.parseInt(reasonSerialno) - 1 - (pageModle.getPageNum() - 1) * pageModle.getPageSize();//(pn-1)*size+index+1==serialno
		if (index > maxIndex || index < 0) {
			return null;
		} else {
			return pageModle.getDatas().get(index);
		}
	}

	/**
	 * 重新设定分页的页码
	 *
	 * @param accepterMap
	 * @param paraMapKey  分页查询条件Map中的key
	 * @param pageOffSet  相对于原来页码的偏移量，1 下一页。-1 上一页
	 */
	public static void resetPageNumInParaMap(Map<String, Object> accepterMap, String paraMapKey, int pageOffSet) {
		Map<String, Object> paraMap = (Map<String, Object>) accepterMap.get(PARA_PAGE_MAP);//获得缓存的分页查询条件
		Object obj = paraMap.get(paraMapKey);
		PageModel pageModel = (PageModel) accepterMap.get(PAGE_MODEL);
		int pageNum = pageModel.getPageNum() + pageOffSet;
		pageNum = pageNum < 1 ? 1 : pageNum;
		int maxPageNum = pageModel.getTotalPageNum();
		pageNum = pageNum > maxPageNum ? maxPageNum : pageNum;
		Reflections.invokeSetter(obj, "page", pageNum);
	}

	/**
	 * 重新设定查询条件中的页码加1，清除当前展示的页
	 *
	 * @param ctx
	 * @param accepterMap  线程中存放接收参数的Map
	 * @param paraMapKey   分页查询条件map中的key
	 * @param pageSizeCurr 当前页的数据条数
	 */
	public static void changePageNext(ChannelHandlerContext ctx, Map<String, Object> accepterMap, String paraMapKey, int pageSizeCurr) {
		resetPageNumInParaMap(accepterMap, paraMapKey, Constants.PAGE_OFFSET_NEXT);
		clearFormalPage(ctx, pageSizeCurr);
	}

	/**
	 * 重新设定查询条件中的页码减1，清除当前展示的页
	 *
	 * @param ctx
	 * @param accepterMap  线程中存放接收参数的Map
	 * @param paraMapKey   分页查询条件map中的key
	 * @param pageSizeCurr 当前页的数据条数
	 */
	public static void changePageUp(ChannelHandlerContext ctx, Map<String, Object> accepterMap, String paraMapKey, int pageSizeCurr) {
		resetPageNumInParaMap(accepterMap, paraMapKey, Constants.PAGE_OFFSET_PREV);
		clearFormalPage(ctx, pageSizeCurr);
	}

	/**
	 * 清除当前页
	 *
	 * @param ctx
	 * @param pageSizeCurr 当前页数据条数
	 */
	private static void clearFormalPage(ChannelHandlerContext ctx, int pageSizeCurr) {
		//清除原来的展示
		HandlerUtil.moveUpN(ctx, ROW_FIXED + pageSizeCurr);
		HandlerUtil.changeRow(ctx);//回车
		HandlerUtil.removeRightDown(ctx);
		HandlerUtil.moveUpN(ctx, 1);
	}

	/**
	 * 获得下标
	 *
	 * @param serialno  序号
	 * @param pageModle 第几页，一页显示多少数据
	 * @return 序号对应的该数据，在页模型中的List的下标
	 */
	public static int getIndexFromSerialnoAndPageModle(String serialno, PageModel pageModle) {
		return Integer.parseInt(serialno) - 1 - (pageModle.getPageNum() - 1) * pageModle.getPageSize();
	}
}
