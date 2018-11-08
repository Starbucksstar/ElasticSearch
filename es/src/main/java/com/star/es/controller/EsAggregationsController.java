package com.star.es.controller;

import com.alibaba.fastjson.JSONObject;
import com.star.es.dto.output.CommonOutPutDto;
import com.star.es.service.tool.ElasticsearchToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/aggr")
public class EsAggregationsController
{
	@Autowired
	private ElasticsearchToolService elasticsearchToolService;

	@RequestMapping(value = "/gtgj/queryBySql", method = RequestMethod.POST)
	public Object queryBySql(@RequestBody @Valid JSONObject jsonObject)
	{
		CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
		commonOutPutDto.setData(elasticsearchToolService.queryList(jsonObject.getString("sql")));
		commonOutPutDto.setMsg("es-sql聚合查询命中集成功");
		return commonOutPutDto;
	}

	@RequestMapping(value = "/gtgj/queryResultByAggregations", method = RequestMethod.POST)
	public Object queryAggregationsBySql(@RequestBody @Valid JSONObject jsonObject)
	{
		CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
		commonOutPutDto.setData(elasticsearchToolService.queryAggregations(jsonObject.getString("sql"),jsonObject.getString("type")));
		commonOutPutDto.setMsg("es-sql聚合查询聚合结果成功");
		return commonOutPutDto;
	}

	@RequestMapping(value = "/gtgj/queryGroupCount", method = RequestMethod.POST)
	public Object queryGroupByCount(@RequestBody @Valid JSONObject jsonObject)
	{
		CommonOutPutDto commonOutPutDto = new CommonOutPutDto();
		commonOutPutDto.setData(elasticsearchToolService.queryCountBySql(jsonObject.getString("sql")));
		commonOutPutDto.setMsg("es-sql聚合查询数量成功");
		return commonOutPutDto;
	}
}
