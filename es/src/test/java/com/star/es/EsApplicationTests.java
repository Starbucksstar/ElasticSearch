package com.star.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.star.es.service.bean.Social;
import com.star.es.service.tool.ElasticsearchToolService;
import com.star.es.bean.ESConfigBean;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsApplicationTests {

    @Autowired
    private ElasticsearchToolService elasticsearchToolService;

    @Test
    public void contextLoads() {
        //删除索引
		/*try {
			elasticsearchToolService.deleteIndex("social");
		} catch (Exception e) {
			e.printStackTrace();
		}


        //批量删除
		try {
			elasticsearchToolService.deleteBulkData("social","comment",1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
*/


        //通过index和type查询所有document数据
        try {
            QueryBuilder queryBuilder = new MatchAllQueryBuilder();
            //List<JSONObject> list = elasticsearchToolService.queryPointsListBySortQb("social","comment",queryBuilder,100,"commentId", SortOrder.DESC);
            //List<JSONObject> list = elasticsearchToolService.queryListJsonByQb("social","comment",queryBuilder,100);
			/*Map<String,Object> map = elasticsearchToolService.queryListPage("social","comment",1,100,queryBuilder);
			for(Map.Entry m:map.entrySet())
			{
				System.out.println(m.getValue());
			}*/

/*
            WildcardQueryBuilder wildcardQueryBuilder = QueryBuilders.wildcardQuery("content", "*冬天*");
            List<JSONObject> list = elasticsearchToolService.queryListJsonByQb("social", "comment", wildcardQueryBuilder, 100);
            for (JSONObject jsonObject : list) {
                System.out.println(JSON.toJSONString(jsonObject));
            }*/
            /*Map<String, String> map = new HashMap<String, String>();
            map.put("content", "*冬天*");
            map.put("commentId", "100*");

            List<JSONObject> list = elasticsearchToolService.queryListJsonByMoreQb("social", "comment", 100, map);
            for (JSONObject jsonObject : list) {
                System.out.println(JSON.toJSONString(jsonObject));
            }*/

	/*		Social social = new Social();
			social.setTrainNo("G1123456789");
			social.setCommentId("11111111");
			social.setContent("I am es");
			social.setScheduleId("90989898989");
			social.setDepartDate("2018-09-13");
			social.setDepartName("武汉");
			social.setArriveName("西安");
			social.setImgs("www.baidu.com");
			social.setCreateTime("2018-09-12 12:00:00");
			elasticsearchToolService.addJson("social","comment","commentId", JSON.parseObject(JSON.toJSONString(social)));
			System.out.println(elasticsearchToolService.deleteById("social","comment","1055"));
			System.out.println(elasticsearchToolService.checkEsStatus());*/
        } catch (Exception e) {
            e.printStackTrace();
        }


        //批量添加模拟数据
       /* List<Social> list = new ArrayList<Social>();
        int x = 1000;
        long time = System.currentTimeMillis();
        for (int i = 0; i < 3000; i++) {
            Social social = new Social();
            social.setTrainNo("G1" + i);
            social.setCommentId(String.valueOf(x + i));

            social.setContent(getTest(i % 100));
            social.setScheduleId(String.valueOf(x + i));
            social.setDepartDate("2018-09-13");
            social.setDepartName("武汉");
            social.setArriveName("西安");
            social.setImgs("www.baidu.com");
            social.setCreateTime(String.valueOf(time + i));
            list.add(social);
        }
        try {
            elasticsearchToolService.addList("social", "comment", "commentId", list);
            System.out.println("添加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }


    private static String getTest(int i) {
        String str = "我与父亲不相见已二年余了，我最不能忘记的是他的背影 。那年冬天，祖母死了，父憨穿封费莩渡凤杀脯辑亲的差使也交卸了，正是祸不单行的日子。我从北京到徐州，打算跟着父亲奔丧回家。到徐州见着父亲，看见满院狼藉的东西，又想起祖母，不禁簌簌地流下眼泪。父亲说：“事已如此，不必难过，好在天无绝人之路！”回家变卖典质，父亲还了亏空；又借钱办了丧事。这些日子，家中光景很是惨淡，一半因为丧事，一半因为父亲赋闲。丧事完毕，父亲要到南京谋事，我也要回北京念书，我们便同行。到南京时，有朋友约去游逛，勾留了一日；第二日上午便须渡江到浦口，下午上车北去。父亲因为事忙，本已说定不送我，叫旅馆里一个熟识的茶房陪我同去。他再三嘱咐茶房，甚是仔细。但他终于不放心，怕茶房不妥帖；颇踌躇了一会。其实我那年已二十岁，北京已来往过两三次，是没有甚么要紧的了。他踌躇了一会，终于决定还是自己送我去。我两三回劝他不必去；他只说：“不要紧，他们去不好！”我们过了江，进了车站。我买票，他忙着照看行李。行李太多了，得向脚夫行些小费，才可过去。他便又忙着和他们讲价钱。我那时真是聪明过分，总觉他说话不大漂亮，非自己插嘴不可。但他终于讲定了价钱；就送我上车。他给我拣定了靠车门的一张椅子；我将他给我做的紫毛大衣铺好座位。他嘱我路上小心，夜里要警醒些，不要受凉，又嘱托茶房好好照应我。我心里暗笑他的迂；他们只认得钱，托他们直是白托！而且我这样大年纪的人，难道还不能料理自己么？唉，我现在想想，那时真是太聪明了！我说道：“爸爸，你走吧。”他往车外看了看说：“我买几个橘子去。你就在此地，不要走动。”我看那边月台的栅栏外有几个卖东西的等着顾客。走到那边月台，须穿过铁道，须跳下去又爬上去。父亲是一个胖子，走过去自然要费事些。我本来要去的，他不肯，只好让他去。我看见他戴着黑布小帽，穿着黑布大马褂，深青布棉袍，蹒跚地走到铁道边，慢慢探身下去，尚不大难。可是他穿过铁道，要爬上那边月台，就不容易了。他用两手攀着上面，两脚再向上缩；他肥胖的身子向左微倾，显出努力的样子。这时我看见他的背影，我的泪很快地流下来了。我赶紧拭干了泪。怕他看见，也怕别人看见。我再向外看时，他已抱了朱红的桔子往回走了。过铁道时，他先将桔子散放在地上，自己慢慢爬下，再抱起桔子走。到这边时，我赶紧去搀他。他和我走到车上，将桔子一股脑儿放在我的皮大衣上。于是扑扑衣上的泥土，心里很轻松似的。过一会儿说：“我走了，到那边来信！”我望着他走出去。他走了几步，回过头看见我，说：“进去吧，里边没人。”等他的背影混入来来往往的人里，再找不着了，我便进来坐了，我的眼泪又来了。近几年来，父亲和我都是东奔西走，家中光景是一日不如一日。他少年出外谋生，独立支持，做了许多大事。哪知老境却如此颓唐！他触目伤怀，自然情不能自已。情郁于中，自然要发之于外；家庭琐屑便往往触他之怒。他待我渐渐不同往日。但最近两年不见，他终于忘却我的不好，只是惦记着我，惦记着我的儿子。我北来后，他写了一信给我，信中说道：“我身体平安，惟膀子疼痛厉害，举箸提笔，诸多不便，大约大去之期不远矣。”我读到此处，在晶莹的泪光中，又看见那肥胖的、青布棉袍黑布马褂的背影。唉！我不知何时再能与他相见！";
        Random random = new Random();
        int n = random.nextInt(1300);
        if (i < n) {
            return str.substring(i, n);
        } else {
            return str.substring(n, i);
        }
    }




    @Autowired
    private ESConfigBean esConfigBean;

    @Test
    public void tt() {
        System.out.println(esConfigBean.toString());
    }


}
