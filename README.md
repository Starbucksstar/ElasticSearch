# ES接口文档

> 注：如果存储的数据不需要模糊查询或分词，直接调用添加单条数据或者多条数据接口，不需要手动创建索引和type.

>注：update数据直接调用添加单条数据或者多条数据接口,确保主键没变即可.

>1.部署相关：
部署方式：单机单例（单机双例内存暂不够），未来可横向扩展
ES-head可视化插件请求地址：http://47.93.57.237:9100/
Es位置：/usr/elasticsearch/elasticsearch-6.4.0-1
Es-head位置:/usr/elasticsearch/elasticsearch-head
Es工程位置：/usr/elasticsearch/gtgj-elasticsearch
2.启动命令
Es启动命令：./elasticsearch -d (注：es启动不能使用root用户，使用elsearch用户启动，启动完成su root)
Es-head启动命令：nohup grunt server &
Es工程启动命令：nohup $JAVA_HOME/bin/java -jar Es-Client-1.0.0.jar -Djava.ext.dirs=$JAVA_HOME/lib >./log/eslog.txt &
> 注意:[1.每次升级修改pom版本号] [2.删除目录下eslog.txt ]
Es启动顺序：Es-->Es-head-->Es工程
3.查找进程命令
Es服务： ps -ef | grep elasticsearch-6.4.0-1
Es-head插件：lsof -i:9100
Es工程：lsof -i:8080
Kibana:netstat -tunlp|grep 5601


## ES相关接口文档

##### 1.创建IK分词类型的索引和type接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/ikCreate   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| ikNames  | 需要ik分词的对象属性名  | true  | 可指定多个用英文逗号，隔开  |
| value  | 对象名和类型  | true  | 例如 "value":{"name": "text","idno": "long","time":"date"} 左-字段名 右-类型  |

#### 请求出参
    {
        "code": "success",
        "msg": "创建ik分词成功",
        "timeStamp": "1537511443604"
    }

##### 点评业务示例:
```json
{
	   "index":"trainsocial",
	   "indexType":"comment",
       "ikNames":"commentMsg,trainNo,userInfo",
       "value":{
			"commentId": "long",
			"trainNo": "text",
			"commentMsg": "text",
			"smallPic":"text",
			"userId":"long",
			"userInfo":"text",
			"scheduleId": "text",
			"departDate": "text",
			"sta": "text",
			"level":"text",
			"createTime":"long",
			"updateTime":"long",
			"name":"text"
			}
	
}
```

------------



##### 2.添加单条数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/addJson   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| primaryKey  | 唯一主键 | true  | 对应表中主键--ES指定id防止数据重复 |
| value  | 属性和数据  | true  | 例如 "value":{"name": "小明","idno": "4125612124","time":"2018-09-21"}  |

#### 请求出参
    {
    	"code": "success",
    	"msg": "添加单条数据成功",
    	"timeStamp": "1537511810677"
	}


------------

##### 2.1添加单条数据接口 [不需要主键,自动生成id]
###### 请求地址：http://192.168.2.58:8080/es/gtgj/addData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| value  | 属性和数据  | true  | 例如 "value":{"name": "小明","idno": "4125612124","time":"2018-09-21"}  |

#### 请求出参
    {
    	"code": "success",
    	"msg": "添加单条数据成功",
    	"timeStamp": "1537511810677"
	}


------------

##### 2.添加多条数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/addListJson   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| primaryKey  | 唯一主键 | true  | 对应表中主键--ES指定id防止数据重复 |
| value  | 属性和数据  | true  | 例如 "value":[{"name": "小明","idno": "4125612124","time":"2018-09-21"},{},{}]  |

#### 请求出参
    {
    	"code": "success",
    	"msg": "添加多条数据成功",
    	"timeStamp": "1537511810677"
	}


------------

##### 3.通过id查询数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| id  |主键值  | true  | 定义主键对应的值 |


#### 请求出参
    {
    	"code": "success",
    	"msg": "查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data": {
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:30Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	}
	}


------------

##### 4.查询多条数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryListData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| size  |返回数  | true  |  |


#### 请求出参
    {
    	"code": "success",
    	"msg": "查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:30Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 5.单条件模糊查询数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryFuzzyData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| qb|  查询条件    |  true  |示例："qb":{"key":"content","value":"*背影*"}|
| size  |返回数  | true  |  |


#### 请求出参
    {
    	"code": "success",
    	"msg": "单条件模糊查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:30Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 6.多条件模糊查询数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryMoreFuzzyData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| qb|  查询条件   |  true  |JSONArray格式 示例："qb":[{"key":"content","value":"*背影*"},{},{}]|
| size  |返回数  | true  |  |


#### 请求出参
    {
    	"code": "success",
    	"msg": "多条件模糊查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:30Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 7.多条件模糊查询+排序接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryFuzzySortData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| qb|  查询条件   |  true  |JSONArray格式 示例："qb":[{"key":"content","value":"*背影*"},{},{}]|
|sortName|按字段排序|true| |
|sortType|排序方式|true| 两种：desc,asc|
| size  |返回数  | true  |  |


#### 请求出参
    {
    	"code": "success",
    	"msg": "多条件模糊查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:32Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 8.多条件模糊查询+排序+分页接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryFuzzySortPageData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| qb|  查询条件   |  true  |JSONArray格式 示例："qb":[{"key":"content","value":"*背影*"},{},{}]|
|sortName|按字段排序|true| |
|sortType|排序方式|true| 两种：desc,asc|
|pageNow|当前页| true| 当前页数|
| size  |返回数  | true  |  |


#### 请求出参
    {
    	"code": "success",
    	"msg": "多条件模糊查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:32Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 9.组合查询接口（分页+模糊+范围+精确+排序）
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryAssembleData   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index  | 索引名  | true  |    |
| indexType  | 索引类型名  | true  |   |
| pageNow|当前页数 |true| |
| pageSize|每页大小|true| |
| wildQb|  模糊查询条件   |  false  |JSONArray格式 示例："wildQb":[{"key":"content","value":"*背影*"},{},{}]|
| rangeQb|  范围查询条件   |  false  |JSONObject格式 示例："rangeQb": {"rangeName":"updateTime","start":"1537714541000","end": "1537769447000"}|
| termQb|  精确查询条件   |  false  |JSONArray格式 示例："termQb":[{"key":"sta","value":"1"},{},{}]|
| sortQb|  排序查询条件   |  false  |JSONArray格式 示例："sortQb":[{"sortName":"updateTime","sortType":"desc"(两种：desc,asc)},{},{}]|

#### 请求出参[默认返回数据按时间倒序]
    {
    	"code": "success",
    	"msg": "组合查询Es数据成功",
  	    "timeStamp": "1537512146426",
    	"data":[{
        	"password": "asdasdsadasd",
            "city": "上海3",
        	"phone": "1545545423",
        	"name": "哈哈3",
        	"time": "2015-01-01T12:10:32Z",
        	"idno": "88885365454512",
        	"desc": "长得丑就要多读书，脸不好看"
    	},{},{}]
	}


------------

##### 10.查询IK分词结果接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/queryIKAnalyze   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| indexName | 索引名  | true  |    |
| text  | 分词文本   | true  |   |
| type  | IK分词类型(int)|  true   | 1--ik_smart 最细粒度  2--ik_max_word  最粗粒度  |


#### 请求出参
	{
        "code": "success",
   	    "msg": "成功",
   	    "timeStamp": "1537513478945",
    	"data": [
        	"当我",
       	    "想你",
        	"的",
        	"时候",
        	"可",
        	"现在",
       	    "我会",
       	    "莫名",
       	    "的",
       	    "哭泣"
   	    ]
	}

------------

##### 11.删除索引接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/   [POST] [application/json]
#### 想删没那么容易，去找开发人员~

------------

##### 12.批量删除数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/deleteDataByBulk   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index | 索引名  | true  |    |
| indexType  | 索引类型   | true  |   |
|size 		 | int类型    |true   |   |


#### 请求出参
    {
        "code": "success",
        "msg": "批量删除数据成功",
        "timeStamp": "1537511443604"
    }

------------

##### 13.批量删除数据接口
###### 请求地址：http://192.168.2.58:8080/es/gtgj/deleteDataById   [POST] [application/json]
#### 请求入参

| key  | value  | 必传  | 注释  |
| ------------ | ------------ | ------------ | ------------ |
| index | 索引名  | true  |    |
| indexType  | 索引类型   | true  |   |
|	id | 删除数据的主键    |true   |   |


#### 请求出参
    {
        "code": "success",
        "msg": "删除数据成功",
        "timeStamp": "1537511443604"
    }

------------


