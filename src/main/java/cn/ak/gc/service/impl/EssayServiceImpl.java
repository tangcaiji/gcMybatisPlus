package cn.ak.gc.service.impl;

import cn.ak.gc.commen.utils.CommonDAO;
import cn.ak.gc.domain.entities.Comment;
import cn.ak.gc.domain.entities.Essay;
import cn.ak.gc.domain.entities.Praise;
import cn.ak.gc.domain.repository.EssayRepository;
import cn.ak.gc.service.EssayService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EssayServiceImpl implements EssayService {
    @Autowired
    CommonDAO<Essay> commonDAO;
    @Autowired
    CommonDAO<Praise> praiseCommonDAO;
    @Autowired
    CommonDAO<Comment> commentCommonDAO;
    @Autowired
    EssayRepository essayRepository;

    @Override
    public void saveEssay(Essay essay) {
        String id = UUID.randomUUID().toString();
        essay.setPk_blog(id);
        essay.setCreationTime(new Date());
        Jedis jedis = new Jedis();
        jedis.lpush("essayInfo", JSON.toJSONString(essay));
        Essay newEssay = new Essay();
        newEssay.setPk_blog(id);
        commonDAO.insertVOWithPK(newEssay);
    }

    @Override
    public JSONArray getEssays(JSONObject json) {
        long begin = System.currentTimeMillis();
        Jedis jedis = new Jedis();
        String userName = json.getString("userName");
        List<Map<String, Object>> list = essayRepository.getEssays(userName);
        List<String> essayInfo = jedis.lrange("essayInfo", 0, -1);
        long redis = System.currentTimeMillis();
        System.out.println("redis耗费时间：" + (redis - begin));
        JSONArray array = new JSONArray();
        essayInfo.forEach(essay -> {
            JSONObject jsonEssay = JSONObject.parseObject(essay);
            for (Map<String, Object> aList : list) {
                if (jsonEssay.getString("pk_blog").equals(aList.get("pk_blog"))) {
                    jsonEssay.put("commentNum", aList.get("commentNum"));
                    jsonEssay.put("praiseNum", aList.get("praiseNum"));
                    jsonEssay.put("isPraised", aList.get("isPraised"));
                    break;
                }
            }
            array.add(jsonEssay);
        });
        long end = System.currentTimeMillis();
        System.out.println("遍历耗费时间：" + (end - redis));
        System.out.println("总共耗费时间：" + (end - begin));
        return array;
    }

    @Override
    public void savePraise(Praise praise) {
        praise.setPk_praise(UUID.randomUUID().toString());
        praise.setCreationTime(new Date());
        praiseCommonDAO.insertVOWithPK(praise);
    }

    @Override
    public List<Comment> getComments(Comment comment) {
        JSONObject json = new JSONObject();
        json.put("pk_blog", comment.getPk_blog());
        return commentCommonDAO.getEntities(comment, json);
    }

    @Override
    public void saveComment(Comment comment) {
        comment.setPk_comment(UUID.randomUUID().toString());
        comment.setCmTime(new Date());
        comment.setCreationTime(new Date());
        commentCommonDAO.insertVOWithPK(comment);
    }
}
