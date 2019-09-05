package solr.exe;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 增删改都需要提交事务,solrj连接solrCloud没有什么区别，连得是zookeeper
 * @author lxt
 * @date 2019-09-04
 */
public class SolrCloudTest {

    private final static String ZK_SERVER ="192.168.43.184:2181,192.168.43.184:2182,192.168.43.184:2183";

    /**
     * 增改
     * @throws IOException
     * @throws SolrServerException
     */
    @Test
    public void testAdd() throws IOException, SolrServerException {

        CloudSolrServer client = new CloudSolrServer(ZK_SERVER);
        client.setDefaultCollection("collection1");
        SolrInputDocument doc=new SolrInputDocument();
        doc.addField("title_ik1","腾讯de音乐");
        doc.addField("title_ik","百度云音乐");
        doc.addField("id","001");
        client.add(doc);
        client.commit();
    }

    /**
     * 删除
     * @throws IOException
     * @throws SolrServerException
     */
    @Test
    public void testDelete() throws IOException, SolrServerException {

        CloudSolrServer client = new CloudSolrServer(ZK_SERVER);
        client.setDefaultCollection("collection1");
        client.deleteById("002");
        //client.deleteByQuery("*:*");
        client.commit();
    }

    /**
     * 查询
     * @throws IOException
     * @throws SolrServerException
     */
    @Test
    public  void  testQuery() throws IOException, SolrServerException {
        CloudSolrServer client = new CloudSolrServer(ZK_SERVER);
        client.setDefaultCollection("collection1");
        //查询条件
        SolrQuery query=new SolrQuery();
         //q
         query.setQuery("title_ik:音乐");
         //分页
         query.setStart(0);
         query.setRows(10);
         query.setHighlight(true);
         final String highLightField="title_ik";
         query.addHighlightField(highLightField);
         //前缀便签，后缀标签
         query.setHighlightSimplePost("<span style='color:red'>");
         query.setHighlightSimplePre("</span>");
        QueryResponse response = client.query(query);
        //response
        SolrDocumentList results = response.getResults();
        //highlighting     highlighting和response和responseHeader都是同一数据级别
        Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
        //solr 8支持lambda
        for (SolrDocument x : results) {
            System.out.println(x.getFieldValue("title_ik"));
            System.out.println(x.getFieldValue("title_ik1"));
            //第一个是key是id的值
            Map<String, List<String>> stringListMap = highlighting.get(x.getFieldValue("id"));
            if (!stringListMap.isEmpty()){
                List<String> strings = stringListMap.get(highLightField);
                //正常业务逻辑是要返回实体类的，没有该字段的值就应该返回未高亮的

                    System.out.println(strings.get(0));

            }



        }


    }


}
