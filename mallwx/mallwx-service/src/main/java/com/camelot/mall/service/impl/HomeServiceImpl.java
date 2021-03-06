package com.camelot.mall.service.impl;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.camelot.goodscenter.dto.ItemDTO;
import com.camelot.goodscenter.service.ItemExportService;
import com.camelot.mall.service.HomeService;
import com.camelot.mall.service.ItemInfoService;
import com.camelot.openplatform.common.DataGrid;
import com.camelot.openplatform.common.ExecuteResult;
import com.camelot.openplatform.common.Pager;
import com.camelot.sellercenter.mallRec.dto.MallRecDTO;
import com.camelot.sellercenter.mallRec.service.MallRecExportService;
import com.camelot.sellercenter.mallrecattr.dto.MallRecAttrDTO;
import com.camelot.sellercenter.mallrecattr.dto.MallRecAttrQueryDTO;
import com.camelot.sellercenter.mallrecattr.service.MallRecAttrExportService;
@Service
public class HomeServiceImpl implements HomeService{
	private Logger LOG = Logger.getLogger(this.getClass());
	@Resource
	private MallRecExportService mallRecService;
	@Resource
	private MallRecAttrExportService mallRecAttrService;
	@Resource
	private ItemInfoService itemInfoService;
	@Resource
	private ItemExportService itemService;
	/**
	 * <p>Discription:[方法功能中文描述]</p>
	 * Created on 2015年2月6日
	 * @return
	 * @author:[Goma 郭茂茂]
	 */
	@Override
	public JSONObject getFloor(String num) {
		
		MallRecDTO mallRec = mallRecService.getMallRecById(Long.valueOf(num));
		JSONObject floor = null;
		if( mallRec != null ){
			floor = JSON.parseObject(JSON.toJSONString(mallRec));

			@SuppressWarnings("rawtypes")
			Pager page = new Pager();
			page.setPage(1);
			page.setRows(100);
			//楼层左侧广告词
			MallRecAttrQueryDTO mallRecAttrQueryDTO = new MallRecAttrQueryDTO();
			mallRecAttrQueryDTO.setRecId(mallRec.getIdDTO());
			mallRecAttrQueryDTO.setRecType(4);
			mallRecAttrQueryDTO.setStatus(1);
//			page.setRows(10);
			// TODO GOMA 暂时注释
//			DataGrid<MallRecAttrDTO> mallRecWords = this.mallRecAttrService.queryMallRecAttrList(page, mallRecAttrQueryDTO);
//			floor.put("words", mallRecWords.getRows());
			
			//楼层左侧广告
			mallRecAttrQueryDTO.setRecType(2);
			page.setRows(1);
			DataGrid<MallRecAttrDTO> mallRecSales = this.mallRecAttrService.queryMallRecAttrList(page, mallRecAttrQueryDTO);
			floor.put("sales", mallRecSales.getRows());
			
			//楼层头部广告
			mallRecAttrQueryDTO.setRecType(3);
			page.setRows(3);
			DataGrid<MallRecAttrDTO> topBunner = this.mallRecAttrService.queryMallRecAttrList(page, mallRecAttrQueryDTO);
			floor.put("topBunner", topBunner.getRows());
			
			//楼层底部广告
			mallRecAttrQueryDTO.setRecType(4);
			page.setRows(10);
			DataGrid<MallRecAttrDTO> bottomBunner = this.mallRecAttrService.queryMallRecAttrList(page, mallRecAttrQueryDTO);
			floor.put("btmBunner", bottomBunner.getRows());
			
			//楼层商品图片
			page.setRows(10);
			mallRecAttrQueryDTO.setRecType(1);
			DataGrid<MallRecAttrDTO> mallRecGoods = this.mallRecAttrService.queryMallRecAttrList(page, mallRecAttrQueryDTO);
			if (mallRecGoods != null && mallRecGoods.getRows() != null) {
				JSONArray items = new JSONArray();
				for (MallRecAttrDTO rec : mallRecGoods.getRows()) {
					JSONObject item = JSON.parseObject(JSON.toJSONString(rec));
					//LOG.debug("GET FLOOR GOODS ITEM_ID:" + rec.getItemId());
					ExecuteResult<ItemDTO> erItem = this.itemService.getItemById(rec.getItemId());
					if( erItem != null && erItem.getResult() != null ){
						item.put("guidePrice", erItem.getResult().getGuidePrice());
					    item.put("hasPrice", erItem.getResult().getHasPrice());
						//LOG.debug("CREATE GOODS:" + item.toJSONString());
						items.add(item);
					}
				}
				floor.put("goods", items);
			}
		}
		return floor;
	}
}
