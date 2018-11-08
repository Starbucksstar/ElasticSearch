1.如何设置某字段fileddata = true，设置字段独立存储可以使用ES聚合统计分析等功能？

    【复合查询】请求url：  http://localhost:9200/social/_mapping/comment/   [post]
        {
    	"properties": {
    		"trainNo": {------//字段名 
    			"type": "text",
    			"fielddata": true
    			}
    		}
    	}

2.如何使用Es的sql功能，像使用mysql一样使用Es?
	 
	 调用接口/aggr/gtgj/queryBySql,会返回命中集

3.如何在Es-head中分组查询索引数据？
	
	【复合查询】请求url:http://localhost:9200/social/comment/_search [post]
	{
  	"size": 0,
  	"query": {
    	"match": {----//模糊查询 
        "commentMsg": "晚点"-----模糊查询对应字段名 
    	}
  	},
  	"aggs": {
   	 "group_by_trainNo": {
      	"terms": {----//分组 
        "field": "trainNo"---//分组字段 
      	}
    	}
	  }
	}

4.如何处理ik分词自定义词典后，历史数据不生效的问题？
	
	http://47.93.57.237:9200/social(索引名)/_update_by_query?conflicts=proceed  【post】
	以上命令运行可以更新历史数据分词结果，可以使用通配符*，匹配多个索引 

------------

	